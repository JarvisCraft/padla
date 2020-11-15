package ru.progrm_jarvis.reflector.wrapper.invoke;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.invoke.InvokeUtil;
import ru.progrm_jarvis.javacommons.pair.Pair;
import ru.progrm_jarvis.javacommons.pair.SimplePair;
import ru.progrm_jarvis.javacommons.util.function.ThrowingFunction;
import ru.progrm_jarvis.reflector.wrapper.AbstractMethodWrapper;
import ru.progrm_jarvis.reflector.wrapper.StaticMethodWrapper;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutionException;
import java.util.function.*;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * {@link StaticMethodWrapper} based on {@link java.lang.invoke Invoke API}.
 *
 * @param <T> type of the object containing the wrapped method
 * @param <R> type of the method's return-value
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class InvokeStaticMethodWrapper<@NotNull T, R>
        extends AbstractMethodWrapper<T, R> implements StaticMethodWrapper<T, R> {

    /**
     * Name of the property responsible for concurrency level of {@link #STATIC_WRAPPER_CACHE}
     */
    public static final @NotNull String STATIC_WRAPPER_CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME
            = InvokeStaticMethodWrapper.class.getCanonicalName() + ".static-wrapper-cache-concurrency-level",
    /**
     * Name of the property responsible for concurrency level of {@link #BOUND_WRAPPER_CACHE}
     */
    BOUND_WRAPPER_CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME
            = InvokeStaticMethodWrapper.class.getCanonicalName() + ".bound-wrapper-cache-concurrency-level";
    /**
     * Weak cache of allocated instance of this static method wrappers of static methods
     */
    protected static final @NotNull Cache<@NotNull Method, @NotNull StaticMethodWrapper<?, ?>> STATIC_WRAPPER_CACHE
            = CacheBuilder.newBuilder()
            .weakValues()
            .concurrencyLevel(
                    Math.max(1, Integer.getInteger(STATIC_WRAPPER_CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME, 4))
            )
            .build();
    /**
     * Weak cache of allocated instance of this static method wrappers of non-static bound methods
     */
    protected static final @NotNull Cache<
            @NotNull Pair<@NotNull Method, @NotNull ?>, @NotNull StaticMethodWrapper<?, ?>
            > BOUND_WRAPPER_CACHE = CacheBuilder.newBuilder()
            .weakValues()
            .concurrencyLevel(
                    Math.max(1, Integer.getInteger(BOUND_WRAPPER_CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME, 4))
            )
            .build();
    /**
     * Function performing the method invocation
     */
    @NonNull Function<Object @NotNull [], R> invoker;

    /**
     * Creates a new static method wrapper.
     *
     * @param containingClass class containing the wrapped object
     * @param wrapped wrapped object
     * @param invoker function performing the method invocation
     */
    protected InvokeStaticMethodWrapper(final @NotNull Class<? extends T> containingClass,
                                        final @NotNull Method wrapped,
                                        final @NotNull Function<Object @NotNull [], R> invoker) {
        super(containingClass, wrapped);
        this.invoker = invoker;
    }

    /**
     * Creates a new cached static method wrapper for the given static method.
     *
     * @param method method to wrap
     * @param <T> type of the object containing the method
     * @param <R> type of the method's return-value
     * @return cached static method wrapper for the given constructor
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows(ExecutionException.class)
    public static <@NonNull T, R> @NotNull StaticMethodWrapper<T, R> from(final @NonNull Method method) {
        return (StaticMethodWrapper<T, R>) STATIC_WRAPPER_CACHE.get(method, () -> {
            checkArgument(Modifier.isStatic(method.getModifiers()), "method should be static");

            val noReturn = method.getReturnType() == void.class;
            switch (method.getParameterCount()) {
                case 0: return noReturn ? from(
                        method, generateFrom(Runnable.class, method)
                ) : from(
                        method, (Supplier<T>) generateFrom(Supplier.class, method)
                );
                case 1: return noReturn ? from(
                        method, (Consumer<Object>) generateFrom(Consumer.class, method)
                ) : from(
                        method, (Function<Object, R>) generateFrom(Function.class, method)
                );
                case 2: return noReturn ? from(
                        method, (BiConsumer<Object, Object>) generateFrom(BiConsumer.class, method)
                ) : from(
                        method, (BiFunction<Object, Object, R>) generateFrom(BiFunction.class, method)
                );
                default: return from(method, InvokeUtil.lookup(method.getDeclaringClass()).unreflect(method));
            }
        });
    }

    /**
     * Creates a new cached static method wrapper for the given non-static method bound to the object.
     *
     * @param method method to wrap
     * @param target target object to whom the wrapper should be bound
     * @param <T> type of the object containing the method
     * @param <R> type of the method's return-value
     * @return cached static method wrapper for the given constructor
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows(ExecutionException.class)
    public static <@NonNull T, R> @NotNull StaticMethodWrapper<T, R> from(
            final @NonNull Method method,
            final @NonNull T target
    ) {
        return (StaticMethodWrapper<T, R>) BOUND_WRAPPER_CACHE.get(SimplePair.of(method, target), () -> {
            checkArgument(!Modifier.isStatic(method.getModifiers()), "method should be non-static");

            val noReturn = method.getReturnType() == void.class;
            switch (method.getParameterCount()) {
                case 0: return noReturn
                        ? from(method, generateFrom(Runnable.class, method, target))
                        : from(method, (Supplier<R>) generateFrom(Supplier.class, method, target));
                case 1: return noReturn
                        ? from(method, (Consumer<Object>) generateFrom(Consumer.class, method, target))
                        : from(method, (Function<Object, R>) generateFrom(Function.class, method, target));
                case 2: return noReturn
                        ? from(method, (BiConsumer<Object, Object>) generateFrom(BiConsumer.class, method, target))
                        : from(method, (BiFunction<Object, Object, R>) generateFrom(BiFunction.class, method, target));
                default: return from(
                        method, InvokeUtil.lookup(method.getDeclaringClass()).unreflect(method).bindTo(target)
                );
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <@NotNull T, R> @NotNull StaticMethodWrapper<T, R> from(
            final @NotNull Method method,
            final @NotNull Runnable generatedRunnable
    ) {
        return new InvokeStaticMethodWrapper<>(
                (Class<? extends T>) method.getDeclaringClass(), method,
                parameters -> {
                    if (parameters.length != 0) throw new IllegalArgumentException(
                            "This static method requires no parameters"
                    );
                    generatedRunnable.run();

                    return null;
                }
        );
    }

    @SuppressWarnings("unchecked")
    private static <@NotNull T, R> @NotNull StaticMethodWrapper<T, R> from(
            final @NotNull Method method,
            final @NotNull Consumer<Object> generatedConsumer
    ) {
        return new InvokeStaticMethodWrapper<>(
                (Class<? extends T>) method.getDeclaringClass(), method,
                parameters -> {
                    if (parameters.length != 1) throw new IllegalArgumentException(
                            "This static method requires 1 parameter"
                    );
                    generatedConsumer.accept(parameters[0]);

                    return null;
                }
        );
    }

    @SuppressWarnings("unchecked")
    private static <@NotNull T, R> @NotNull StaticMethodWrapper<T, R> from(
            final @NotNull Method method,
            final @NotNull BiConsumer<Object, Object> generatedBiConsumer
    ) {
        return new InvokeStaticMethodWrapper<>(
                (Class<? extends T>) method.getDeclaringClass(), method,
                parameters -> {
                    if (parameters.length != 2) throw new IllegalArgumentException(
                            "This static method requires 2 parameters"
                    );
                    generatedBiConsumer.accept(parameters[0], parameters[1]);

                    return null;
                }
        );
    }

    @SuppressWarnings("unchecked")
    private static <@NotNull T, R> @NotNull StaticMethodWrapper<T, R> from(
            final @NotNull Method method,
            final @NotNull Supplier<R> generatedSupplier
    ) {
        return new InvokeStaticMethodWrapper<>(
                (Class<? extends T>) method.getDeclaringClass(), method,
                parameters -> {
                    if (parameters.length != 0) throw new IllegalArgumentException(
                            "This static method requires no parameters"
                    );

                    return generatedSupplier.get();
                }
        );
    }

    @SuppressWarnings("unchecked")
    private static <@NotNull T, R> @NotNull StaticMethodWrapper<T, R> from(
            final @NotNull Method method,
            final @NotNull Function<Object, R> generatedFunction
    ) {
        return new InvokeStaticMethodWrapper<>(
                (Class<? extends T>) method.getDeclaringClass(), method,
                parameters -> {
                    if (parameters.length != 1) throw new IllegalArgumentException(
                            "This static method requires 1 parameter"
                    );

                    return generatedFunction.apply(parameters[0]);
                }
        );
    }

    @SuppressWarnings("unchecked")
    private static <@NotNull T, R> @NotNull StaticMethodWrapper<T, R> from(
            final @NotNull Method method,
            final @NotNull BiFunction<Object, Object, R> generatedBiFunction
    ) {
        return new InvokeStaticMethodWrapper<>(
                (Class<? extends T>) method.getDeclaringClass(), method,
                parameters -> {
                    if (parameters.length != 2) throw new IllegalArgumentException(
                            "This static method requires 2 parameters"
                    );

                    return generatedBiFunction.apply(parameters[0], parameters[1]);
                }
        );
    }

    @SuppressWarnings("unchecked")
    private static <@NotNull T, R> @NotNull StaticMethodWrapper<T, R> from(
            final @NotNull Method method,
            final @NotNull MethodHandle methodHandle
    ) {
        return new InvokeStaticMethodWrapper<>(
                (Class<? extends T>) method.getDeclaringClass(), method,
                (ThrowingFunction<Object[], R, ?>) parameters -> (R) methodHandle.invokeWithArguments(parameters)
        );
    }

    @Override
    public R invoke(final Object @NotNull ... parameters) {
        return invoker.apply(parameters);
    }

    private static <@NotNull F, @NotNull T> @NotNull F generateFrom(
            final @NotNull Class<F> functionalType,
            final @NotNull Method method
    ) {
        return InvokeUtil.<F, T>invokeFactory()
                .implementing(functionalType)
                .via(method)
                .createUnsafely();
    }

    private static <@NotNull F, @NotNull T> @NotNull F generateFrom(
            final @NotNull Class<F> functionalType,
            final @NotNull Method method, final T target
    ) {
        return InvokeUtil.<F, T>invokeFactory()
                .implementing(functionalType)
                .via(method)
                .boundTo(target)
                .createUnsafely();
    }
}
