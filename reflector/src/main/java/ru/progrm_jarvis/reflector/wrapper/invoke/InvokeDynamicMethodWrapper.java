package ru.progrm_jarvis.reflector.wrapper.invoke;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.invoke.InvokeUtil;
import ru.progrm_jarvis.javacommons.util.function.ThrowingBiFunction;
import ru.progrm_jarvis.reflector.wrapper.AbstractMethodWrapper;
import ru.progrm_jarvis.reflector.wrapper.DynamicMethodWrapper;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * {@link DynamicMethodWrapper} based on {@link java.lang.invoke Invoke API}.
 *
 * @param <T> type of the object containing the wrapped method
 * @param <R> type of the method's return-value
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class InvokeDynamicMethodWrapper<@NotNull T, R>
        extends AbstractMethodWrapper<T, R> implements DynamicMethodWrapper<T, R> {

    /**
     * Name of the property responsible for concurrency level of {@link #CACHE}
     */
    protected static final @NotNull String CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME
            = InvokeDynamicMethodWrapper.class.getCanonicalName() + ".cache-concurrency-level";
    /**
     * Weak cache of allocated instance of this constructor wrapper
     */
    private static final @NotNull Cache<@NotNull Method, @NotNull InvokeDynamicMethodWrapper<?, ?>> CACHE
            = CacheBuilder.newBuilder()
            .weakValues()
            .concurrencyLevel(Math.max(1, Integer.getInteger(CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME, 4)))
            .build();

    /**
     * Bi-function performing the method invocation
     */
    @NonNull BiFunction<T, Object @NotNull [], R> invoker;

    /**
     * Creates a new dynamic method wrapper.
     *
     * @param containingClass class containing the wrapped object
     * @param wrapped wrapped object
     * @param invoker bi-function performing the method invocation
     */
    protected InvokeDynamicMethodWrapper(final @NotNull Class<? extends T> containingClass,
                                         final @NotNull Method wrapped,
                                         final @NotNull BiFunction<@NotNull T, Object @NotNull [], R> invoker) {
        super(containingClass, wrapped);
        this.invoker = invoker;
    }

    /**
     * Creates a new cached dynamic method wrapper for the given non-static method.
     *
     * @param method method to wrap
     * @param <T> type of the object containing the method
     * @param <R> type of the method's return-value
     * @return cached dynamic method wrapper for the given constructor
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows(ExecutionException.class)
    public static <@NotNull T, R> DynamicMethodWrapper<T, R> from(
            final @NonNull Method method
    ) {
        return (DynamicMethodWrapper<T, R>) CACHE.get(method, () -> {
            checkArgument(!Modifier.isStatic(method.getModifiers()), "method should be non-static");

            switch (method.getParameterCount()) {
                case 0: {
                    // handle void specifically as it can't be cast to Object
                    if (method.getReturnType() == void.class) {
                        final Consumer<Object> consumer
                                = generateImplementation(Consumer.class, method);

                        return new InvokeDynamicMethodWrapper<>(
                                method.getDeclaringClass(), method, (target, parameters) -> {
                            if (parameters.length != 0) throw new IllegalArgumentException(
                                    "This method requires no parameters"
                            );
                            consumer.accept(target);

                            return null;
                        });
                    }

                    final Function<Object, R> function
                            = generateImplementation(Function.class, method);

                    return new InvokeDynamicMethodWrapper<>(
                            (Class<T>) method.getDeclaringClass(), method, (target, parameters) -> {
                        if (parameters.length != 0) throw new IllegalArgumentException(
                                "This method requires no parameters"
                        );
                        return function.apply(target);
                    });
                }
                case 1: {
                    // handle void specifically as it can't be cast to Object
                    if (method.getReturnType() == void.class) {
                        final BiConsumer<Object, Object> consumer
                                = generateImplementation(BiConsumer.class, method);

                        return new InvokeDynamicMethodWrapper<>(
                                method.getDeclaringClass(), method, (target, parameters) -> {
                            if (parameters.length != 1) throw new IllegalArgumentException(
                                    "This method requires 1 parameter"
                            );
                            consumer.accept(target, parameters[0]);

                            return null;
                        });
                    }

                    final BiFunction<Object, Object, R> biFunction
                            = generateImplementation(BiFunction.class, method);

                    return new InvokeDynamicMethodWrapper<>(
                            method.getDeclaringClass(), method, (target, parameters) -> {
                        if (parameters.length != 1) throw new IllegalArgumentException(
                                "This method requires 1 parameter"
                        );

                        return biFunction.apply(target, parameters[0]);
                    });
                }
                default: {
                    // initialized here not to do it inside lambda body
                    val methodHandle = InvokeUtil.lookup(method.getDeclaringClass()).unreflect(method);

                    return new InvokeDynamicMethodWrapper<>(
                            (Class<T>) method.getDeclaringClass(), method,
                            (ThrowingBiFunction<T, Object[], R, Throwable>) (target, parameters) -> {
                                val length = parameters.length;
                                val arguments = new Object[length + 1];
                                arguments[0] = target;
                                System.arraycopy(parameters, 0, arguments, 1, length);
                                return (R) methodHandle.invokeWithArguments(arguments);
                            }
                    );
                }
            }
        });
    }

    private static <@NotNull F, @NotNull T> @NotNull F generateImplementation(
            final @NotNull Class<? super F> functionalType,
            final @NotNull Method method
    ) {
        return InvokeUtil.<F, T>invokeFactory()
                .implementing(functionalType)
                .via(method)
                .createUnsafely();
    }

    @Override
    public R invoke(final @NotNull T target, final Object @NotNull ... parameters) {
        return invoker.apply(target, parameters);
    }
}
