package ru.progrm_jarvis.reflector.wrapper.invoke;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.util.function.ThrowingFunction;
import ru.progrm_jarvis.reflector.invoke.InvokeUtil;
import ru.progrm_jarvis.reflector.wrapper.AbstractConstructorWrapper;

import java.lang.reflect.Constructor;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class InvokeConstructorWrapper<T> extends AbstractConstructorWrapper<T> {

    /**
     * Name of the property responsible for concurrency level of {@link #CACHE}
     */
    @NonNull public static final String CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME
            = InvokeConstructorWrapper.class.getCanonicalName() + ".cache-concurrency-level";

    @NonNull Function<Object[], T> invoker;

    /**
     * Weak cache of allocated instance of this constructor wrapper
     */
    @SuppressWarnings("UnstableApiUsage") protected static final Cache<Constructor<?>, InvokeConstructorWrapper<?>> CACHE
            = CacheBuilder.newBuilder()
            .weakValues()
            .concurrencyLevel(Math.max(1, Integer.getInteger(CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME, 4)))
            .build();

    protected InvokeConstructorWrapper(@NonNull final Class<? extends T> containingClass,
                                       @NonNull final Constructor<? extends T> wrapped,
                                       @NonNull final Function<Object[], T> invoker) {
        super(containingClass, wrapped);
        this.invoker = invoker;
    }

    @Override
    public T invoke(@NotNull final Object... parameters) {
        return invoker.apply(parameters);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows(ExecutionException.class)
    public static <T> InvokeConstructorWrapper<T> from(@NonNull final Constructor<? extends T> constructor) {
        return (InvokeConstructorWrapper<T>) CACHE.get(constructor, () -> {
            switch (constructor.getParameterCount()) {
                case 0: {
                    final Supplier<T> supplier;
                    try {
                        supplier = InvokeUtil.<Supplier<T>, Object>invokeFactory()
                                .implementing(Supplier.class)
                                .via(constructor)
                                .create();
                    } catch (final Throwable x) {
                        throw new RuntimeException("Unable to create a Supplier from constructor " + constructor, x);
                    }

                    return new InvokeConstructorWrapper<>(
                            constructor.getDeclaringClass(), constructor,
                            parameters -> {
                                if (parameters.length != 0) throw new IllegalArgumentException(
                                        "This constructor requires no parameters"
                                );

                                return supplier.get();
                            });
                }
                case 1: {
                    final Function<Object, T> function;
                    try {
                        function = InvokeUtil.<Function<Object, T>, Object>invokeFactory()
                                .implementing(Function.class)
                                .via(constructor)
                                .create();
                    } catch (final Throwable x) {
                        throw new RuntimeException("Unable to create a Function from constructor " + constructor, x);
                    }

                    return new InvokeConstructorWrapper<>(
                            constructor.getDeclaringClass(), constructor,
                            parameters -> {
                                if (parameters.length != 1) throw new IllegalArgumentException(
                                        "This constructor requires 1 parameter"
                                );

                                return function.apply(parameters[0]);
                            });
                }
                case 2: {
                    final BiFunction<Object, Object, T> function;
                    try {
                        function = InvokeUtil.<BiFunction<Object, Object, T>, Object>invokeFactory()
                                .implementing(BiFunction.class)
                                .via(constructor)
                                .create();
                    } catch (final Throwable x) {
                        throw new RuntimeException("Unable to create a BiFunction from constructor " + constructor, x);
                    }

                    return new InvokeConstructorWrapper<>(
                            constructor.getDeclaringClass(), constructor,
                            parameters -> {
                                if (parameters.length != 2) throw new IllegalArgumentException(
                                        "This constructor requires 2 parameter"
                                );

                                return function.apply(parameters[0], parameters[1]);
                            });
                }
                default: {
                    val declaringClass = constructor.getDeclaringClass();
                    // initialized here not to do it inside lambda body
                    val methodHandle = InvokeUtil.lookup(declaringClass).unreflectConstructor(constructor);
                    return new InvokeConstructorWrapper<>(
                            constructor.getDeclaringClass(), constructor,
                            (ThrowingFunction) parameters -> (T) methodHandle.invokeWithArguments((Object[]) parameters)
                    );
                }
            }
        });
    }
}
