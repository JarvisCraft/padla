package ru.progrm_jarvis.reflector.wrapper.invoke;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.util.function.ThrowingBiFunction;
import ru.progrm_jarvis.reflector.invoke.InvokeUtil;
import ru.progrm_jarvis.reflector.wrapper.AbstractMethodWrapper;
import ru.progrm_jarvis.reflector.wrapper.DynamicMethodWrapper;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class InvokeDynamicMethodWrapper<T, R>
        extends AbstractMethodWrapper<T, R> implements DynamicMethodWrapper<T, R> {

    /**
     * Name of the property responsible for concurrency level of {@link #CACHE}
     */
    @NonNull protected static final String CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME
            = InvokeDynamicMethodWrapper.class.getCanonicalName() + ".cache-concurrency-level";

    @NonNull BiFunction<T, Object[], R> invoker;

    /**
     * Weak cache of allocated instance of this constructor wrapper
     */
    private static final Cache<Method, InvokeDynamicMethodWrapper<?, ?>> CACHE
            = CacheBuilder.newBuilder()
            .weakValues()
            .concurrencyLevel(Math.max(1, Integer.getInteger(CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME, 4)))
            .build();

    protected InvokeDynamicMethodWrapper(@NonNull final Class<? extends T> containingClass,
                                         @NonNull final Method wrapped,
                                         @NonNull final BiFunction<T, Object[], R> invoker) {
        super(containingClass, wrapped);
        this.invoker = invoker;
    }

    @Override
    public R invoke(@NotNull final T target, @NotNull final Object... parameters) {
        return invoker.apply(target, parameters);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows(ExecutionException.class)
    public static <T, R> InvokeDynamicMethodWrapper<T, R> from(@NonNull final Method method) {
        return (InvokeDynamicMethodWrapper<T, R>) CACHE.get(method, () -> {
            switch (method.getParameterCount()) {
                case 0: {
                    // handle void specifically as it can't be cast to Object
                    if (method.getReturnType() == void.class) {
                        val consumer = InvokeUtil.<Consumer<Object>, T>invokeFactory()
                                .implementing(Consumer.class)
                                .via(method)
                                .createUnsafely();

                        return new InvokeDynamicMethodWrapper<>(
                                (Class<T>) method.getDeclaringClass(), method, (target, parameters) -> {
                            if (parameters.length != 0) throw new IllegalArgumentException(
                                    "This method requires no parameters"
                            );
                            consumer.accept(target);

                            return null;
                        });
                    }

                    val function = InvokeUtil.<Function<Object, R>, T>invokeFactory()
                            .implementing(Function.class)
                            .via(method)
                            .createUnsafely();

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
                        val consumer = InvokeUtil.<BiConsumer<Object, Object>, T>invokeFactory()
                                .implementing(BiConsumer.class)
                                .via(method)
                                .createUnsafely();

                        return new InvokeDynamicMethodWrapper<>(
                                (Class<T>) method.getDeclaringClass(), method, (target, parameters) -> {
                            if (parameters.length != 1) throw new IllegalArgumentException(
                                    "This method requires 1 parameter"
                            );
                            consumer.accept(target, parameters[0]);

                            return null;
                        });
                    }

                    val biFunction = InvokeUtil.<BiFunction<Object, Object, R>, T>invokeFactory()
                            .implementing(BiFunction.class)
                            .via(method)
                            .createUnsafely();

                    return new InvokeDynamicMethodWrapper<>(
                            (Class<T>) method.getDeclaringClass(), method, (target, parameters) -> {
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
}
