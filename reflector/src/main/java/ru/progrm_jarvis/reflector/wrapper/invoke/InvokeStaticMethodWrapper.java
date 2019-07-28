package ru.progrm_jarvis.reflector.wrapper.invoke;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.util.function.ThrowingFunction;
import ru.progrm_jarvis.reflector.invoke.InvokeUtil;
import ru.progrm_jarvis.reflector.wrapper.AbstractMethodWrapper;
import ru.progrm_jarvis.reflector.wrapper.StaticMethodWrapper;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutionException;
import java.util.function.*;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class InvokeStaticMethodWrapper<T, R> extends AbstractMethodWrapper<T, R> implements StaticMethodWrapper<T, R> {

    /**
     * Name of the property responsible for concurrency level of {@link #CACHE}
     */
    @NonNull public static final String CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME
            = InvokeStaticMethodWrapper.class.getCanonicalName() + ".cache-concurrency-level";

    @NonNull Function<Object[], R> invoker;

    /**
     * Weak cache of allocated instance of this constructor wrapper
     */
    protected static final Cache<Method, InvokeStaticMethodWrapper<?, ?>> CACHE
            = CacheBuilder.newBuilder()
            .weakValues()
            .concurrencyLevel(Math.max(1, Integer.getInteger(CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME, 4)))
            .build();

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

    @SuppressWarnings("unchecked")
    @SneakyThrows(ExecutionException.class)
    public static <T, R> InvokeStaticMethodWrapper<T, R> from(@NonNull final Method method) {
        return (InvokeStaticMethodWrapper<T, R>) CACHE.get(method, () -> {
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

    @SuppressWarnings("unchecked")
    @SneakyThrows(IllegalAccessException.class)
    public static <T, R> InvokeStaticMethodWrapper<T, R> from(@NonNull final Method method,
                                                              @NonNull final T target) {
        Preconditions.checkArgument(!Modifier.isStatic(method.getModifiers()), "method should be non-static");

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
                            (Class<T>) method.getDeclaringClass(), method,
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
                        (Class<T>) method.getDeclaringClass(), method,
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
                            (Class<T>) method.getDeclaringClass(), method,
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
                        (Class<T>) method.getDeclaringClass(), method,
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
                            (Class<T>) method.getDeclaringClass(), method,
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
                        (Class<T>) method.getDeclaringClass(), method,
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
    }
}
