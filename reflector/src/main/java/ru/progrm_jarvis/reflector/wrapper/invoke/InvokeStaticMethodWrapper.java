package ru.progrm_jarvis.reflector.wrapper.invoke;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.pair.Pair;
import ru.progrm_jarvis.javacommons.pair.SimplePair;
import ru.progrm_jarvis.javacommons.util.function.ThrowingFunction;
import ru.progrm_jarvis.javacommons.invoke.InvokeUtil;
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
public class InvokeStaticMethodWrapper<T, R> extends AbstractMethodWrapper<T, R> implements StaticMethodWrapper<T, R> {

    /**
     * Name of the property responsible for concurrency level of {@link #STATIC_WRAPPER_CACHE}
     */
    @NonNull public static final String STATIC_WRAPPER_CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME
            = InvokeStaticMethodWrapper.class.getCanonicalName() + ".static-wrapper-cache-concurrency-level",
    /**
     * Name of the property responsible for concurrency level of {@link #BOUND_WRAPPER_CACHE}
     */
    BOUND_WRAPPER_CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME
            = InvokeStaticMethodWrapper.class.getCanonicalName() + ".bound-wrapper-cache-concurrency-level";
    /**
     * Weak cache of allocated instance of this static method wrappers of static methods
     */
    protected static final Cache<Method, InvokeStaticMethodWrapper<?, ?>> STATIC_WRAPPER_CACHE
            = CacheBuilder.newBuilder()
            .weakValues()
            .concurrencyLevel(
                    Math.max(1, Integer.getInteger(STATIC_WRAPPER_CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME, 4))
            )
            .build();
    /**
     * Weak cache of allocated instance of this static method wrappers of non-static bound methods
     */
    protected static final Cache<Pair<Method, ?>, InvokeStaticMethodWrapper<?, ?>> BOUND_WRAPPER_CACHE
            = CacheBuilder.newBuilder()
            .weakValues()
            .concurrencyLevel(
                    Math.max(1, Integer.getInteger(BOUND_WRAPPER_CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME, 4))
            )
            .build();
    /**
     * Function performing the method invocation
     */
    @NonNull Function<Object[], R> invoker;

    /**
     * Creates a new static method wrapper.
     *
     * @param containingClass class containing the wrapped object
     * @param wrapped wrapped object
     * @param invoker function performing the method invocation
     */
    protected InvokeStaticMethodWrapper(@NonNull final Class<? extends T> containingClass,
                                        @NonNull final Method wrapped,
                                        @NonNull final Function<Object[], R> invoker) {
        super(containingClass, wrapped);
        this.invoker = invoker;
    }

    @Override
    public R invoke(@NotNull final Object... parameters) {
        return invoker.apply(parameters);
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
    public static <T, R> InvokeStaticMethodWrapper<T, R> from(@NonNull final Method method) {
        return (InvokeStaticMethodWrapper<T, R>) STATIC_WRAPPER_CACHE.get(method, () -> {
            checkArgument(Modifier.isStatic(method.getModifiers()), "method should be static");

            switch (method.getParameterCount()) {
                case 0: {
                    // handle void specifically as it can't be cast to Object
                    if (method.getReturnType() == void.class) {
                        val runnable = InvokeUtil.<Runnable, T>invokeFactory()
                                .implementing(Runnable.class)
                                .via(method)
                                .createUnsafely();

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
                    val supplier = InvokeUtil.<Supplier<R>, T>invokeFactory()
                            .implementing(Supplier.class)
                            .via(method)
                            .createUnsafely();

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
                        val consumer = InvokeUtil.<Consumer<Object>, T>invokeFactory()
                                .implementing(Consumer.class)
                                .via(method)
                                .createUnsafely();

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
                    val function = InvokeUtil.<Function<Object, R>, T>invokeFactory()
                            .implementing(Function.class)
                            .via(method)
                            .createUnsafely();

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
                        val biConsumer = InvokeUtil.<BiConsumer<Object, Object>, T>invokeFactory()
                                .implementing(BiConsumer.class)
                                .via(method)
                                .createUnsafely();

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
                    val biFunction = InvokeUtil.<BiFunction<Object, Object, R>, T>invokeFactory()
                            .implementing(BiFunction.class)
                            .via(method)
                            .createUnsafely();

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
                            (ThrowingFunction) parameters -> (T) methodHandle.invokeWithArguments((Object[]) parameters)
                    );
                }
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
    public static <T, R> InvokeStaticMethodWrapper<T, R> from(@NonNull final Method method,
                                                              @NonNull final T target) {
        return (InvokeStaticMethodWrapper<T, R>) BOUND_WRAPPER_CACHE.get(SimplePair.of(method, target), () -> {
            checkArgument(!Modifier.isStatic(method.getModifiers()), "method should be non-static");

            switch (method.getParameterCount()) {
                case 0: {
                    // handle void specifically as it can't be cast to Object
                    if (method.getReturnType() == void.class) {
                        val runnable = InvokeUtil.<Runnable, T>invokeFactory()
                                .implementing(Runnable.class)
                                .via(method)
                                .boundTo(target)
                                .createUnsafely();

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
                    val supplier = InvokeUtil.<Supplier<R>, T>invokeFactory()
                            .implementing(Supplier.class)
                            .via(method)
                            .boundTo(target)
                            .createUnsafely();

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
                        val consumer = InvokeUtil.<Consumer<Object>, T>invokeFactory()
                                .implementing(Consumer.class)
                                .via(method)
                                .boundTo(target)
                                .createUnsafely();

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
                    val function = InvokeUtil.<Function<Object, R>, T>invokeFactory()
                            .implementing(Function.class)
                            .via(method)
                            .boundTo(target)
                            .createUnsafely();

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
                        val biConsumer = InvokeUtil.<BiConsumer<Object, Object>, T>invokeFactory()
                                .implementing(BiConsumer.class)
                                .via(method)
                                .boundTo(target)
                                .createUnsafely();

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
                    val biFunction = InvokeUtil.<BiFunction<Object, Object, R>, T>invokeFactory()
                            .implementing(BiFunction.class)
                            .via(method)
                            .boundTo(target)
                            .createUnsafely();

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
                            (ThrowingFunction) parameters -> (T) methodHandle.invokeWithArguments((Object[]) parameters)
                    );
                }
            }
        });
    }
}
