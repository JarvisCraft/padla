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

            switch (method.getParameterCount()) {
                case 0: {
                    // handle void specifically as it can't be cast to Object
                    if (method.getReturnType() == void.class) {
                        val runnable
                                = generateImplementation(Runnable.class, method);

                        return new InvokeStaticMethodWrapper<>(
                                method.getDeclaringClass(), method,
                                parameters -> {
                                    if (parameters.length != 0) throw new IllegalArgumentException(
                                            "This static method requires no parameters"
                                    );
                                    runnable.run();

                                    return null;
                                });
                    }
                    final Supplier<R> supplier
                            = generateImplementation(Supplier.class, method);

                    return new InvokeStaticMethodWrapper<>(
                            method.getDeclaringClass(), method,
                            parameters -> {
                                if (parameters.length != 0) throw new IllegalArgumentException(
                                        "This static method requires no parameters"
                                );

                                return supplier.get();
                            });
                }
                case 1: {
                    // handle void specifically as it can't be cast to Object
                    if (method.getReturnType() == void.class) {
                        final Consumer<Object> consumer
                                = generateImplementation(Consumer.class, method);

                        return new InvokeStaticMethodWrapper<>(
                                method.getDeclaringClass(), method,
                                parameters -> {
                                    if (parameters.length != 1) throw new IllegalArgumentException(
                                            "This static method requires 1 parameter"
                                    );
                                    consumer.accept(parameters[0]);

                                    return null;
                                });
                    }
                    final Function<Object, R> function
                            = generateImplementation(Function.class, method);

                    return new InvokeStaticMethodWrapper<>(
                            method.getDeclaringClass(), method,
                            parameters -> {
                                if (parameters.length != 1) throw new IllegalArgumentException(
                                        "This static method requires 1 parameter"
                                );

                                return function.apply(parameters[0]);
                            });
                }
                case 2: {
                    // handle void specifically as it can't be cast to Object
                    if (method.getReturnType() == void.class) {
                        final BiConsumer<Object, Object> biConsumer
                                = generateImplementation(BiConsumer.class, method);

                        return new InvokeStaticMethodWrapper<>(
                                method.getDeclaringClass(), method,
                                parameters -> {
                                    if (parameters.length != 2) throw new IllegalArgumentException(
                                            "This static method requires 2 parameters"
                                    );
                                    biConsumer.accept(parameters[0], parameters[1]);

                                    return null;
                                });
                    }
                    final BiFunction<Object, Object, R> biFunction
                            = generateImplementation(BiFunction.class, method);

                    return new InvokeStaticMethodWrapper<>(
                            method.getDeclaringClass(), method,
                            parameters -> {
                                if (parameters.length != 2) throw new IllegalArgumentException(
                                        "This static method requires 2 parameters"
                                );

                                return biFunction.apply(parameters[0], parameters[1]);
                            });
                }
                default: {
                    val declaringClass = method.getDeclaringClass();
                    // initialized here not to do it inside lambda body
                    val methodHandle = InvokeUtil.lookup(declaringClass).unreflect(method);
                    return new InvokeStaticMethodWrapper<>(
                            method.getDeclaringClass(), method,
                            (ThrowingFunction<Object[], T, ?>) parameters -> (T) methodHandle
                                    .invokeWithArguments(parameters)
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

            switch (method.getParameterCount()) {
                case 0: {
                    // handle void specifically as it can't be cast to Object
                    if (method.getReturnType() == void.class) {
                        val runnable
                                = generateImplementation(Runnable.class, method, target);

                        return new InvokeStaticMethodWrapper<>(
                                method.getDeclaringClass(), method,
                                parameters -> {
                                    if (parameters.length != 0) throw new IllegalArgumentException(
                                            "This static method requires no parameters"
                                    );
                                    runnable.run();

                                    return null;
                                });
                    }
                    final Supplier<R> supplier
                            = generateImplementation(Supplier.class, method, target);

                    return new InvokeStaticMethodWrapper<>(
                            method.getDeclaringClass(), method,
                            parameters -> {
                                if (parameters.length != 0) throw new IllegalArgumentException(
                                        "This static method requires no parameters"
                                );

                                return supplier.get();
                            });
                }
                case 1: {
                    // handle void specifically as it can't be cast to Object
                    if (method.getReturnType() == void.class) {
                        final Consumer<Object> consumer
                                = generateImplementation(Consumer.class, method, target);

                        return new InvokeStaticMethodWrapper<>(
                                method.getDeclaringClass(), method,
                                parameters -> {
                                    if (parameters.length != 1) throw new IllegalArgumentException(
                                            "This static method requires 1 parameter"
                                    );
                                    consumer.accept(parameters[0]);

                                    return null;
                                });
                    }
                    final Function<Object, R> function
                            = generateImplementation(Function.class, method, target);

                    return new InvokeStaticMethodWrapper<>(
                            method.getDeclaringClass(), method,
                            parameters -> {
                                if (parameters.length != 1) throw new IllegalArgumentException(
                                        "This static method requires 1 parameter"
                                );

                                return function.apply(parameters[0]);
                            });
                }
                case 2: {
                    // handle void specifically as it can't be cast to Object
                    if (method.getReturnType() == void.class) {
                        final BiConsumer<Object, Object> biConsumer
                                = generateImplementation(BiConsumer.class, method, target);

                        return new InvokeStaticMethodWrapper<>(
                                method.getDeclaringClass(), method,
                                parameters -> {
                                    if (parameters.length != 2) throw new IllegalArgumentException(
                                            "This static method requires 2 parameters"
                                    );
                                    biConsumer.accept(parameters[0], parameters[1]);

                                    return null;
                                });
                    }
                    final BiFunction<Object, Object, R> biFunction
                            = generateImplementation(BiFunction.class, method, target);

                    return new InvokeStaticMethodWrapper<>(
                            method.getDeclaringClass(), method,
                            parameters -> {
                                if (parameters.length != 2) throw new IllegalArgumentException(
                                        "This static method requires 2 parameters"
                                );

                                return biFunction.apply(parameters[0], parameters[1]);
                            });
                }
                default: {
                    // initialized here not to do it inside lambda body
                    val methodHandle = InvokeUtil.lookup(method.getDeclaringClass()).unreflect(method).bindTo(target);
                    return new InvokeStaticMethodWrapper<>(
                            method.getDeclaringClass(), method,
                            (ThrowingFunction<Object[], T, ?>) parameters -> (T) methodHandle
                                    .invokeWithArguments(parameters)
                    );
                }
            }
        });
    }

    private static <@NotNull F, @NotNull T> @NotNull F generateImplementation(
            final @NotNull Class<? super F> functionalType,
            final @NotNull Method method, final T target
    ) {
        return InvokeUtil.<F, T>invokeFactory()
                .implementing(functionalType)
                .via(method)
                .boundTo(target)
                .createUnsafely();
    }

    @Override
    public R invoke(final Object @NotNull ... parameters) {
        return invoker.apply(parameters);
    }
}
