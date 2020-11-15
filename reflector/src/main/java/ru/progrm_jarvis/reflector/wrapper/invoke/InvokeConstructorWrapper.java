package ru.progrm_jarvis.reflector.wrapper.invoke;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.invoke.InvokeUtil;
import ru.progrm_jarvis.javacommons.util.function.ThrowingFunction;
import ru.progrm_jarvis.reflector.wrapper.AbstractConstructorWrapper;
import ru.progrm_jarvis.reflector.wrapper.ConstructorWrapper;
import ru.progrm_jarvis.reflector.wrapper.ReflectorWrappers;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * {@link ru.progrm_jarvis.reflector.wrapper.ConstructorWrapper} based on {@link java.lang.invoke Invoke API}.
 *
 * @param <T> type of the object instantiated by the wrapped constructor
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class InvokeConstructorWrapper<@NotNull T>
        extends AbstractConstructorWrapper<T> {

    /**
     * Name of the property responsible for concurrency level of {@link #WRAPPER_CACHE}
     */
    public static final @NotNull String WRAPPER_CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME
            = InvokeConstructorWrapper.class.getCanonicalName() + ".wrapper-cache-concurrency-level";
    /**
     * Weak cache of allocated instance of this constructor wrapper
     */
    protected static final @NotNull Cache<@NotNull Constructor<?>, @NotNull ConstructorWrapper<?>> WRAPPER_CACHE
            = CacheBuilder.newBuilder()
            .weakValues()
            .concurrencyLevel(Math.max(1, Integer.getInteger(WRAPPER_CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME, 4)))
            .build();

    /**
     * Function performing the constructor invocation
     */
    @NotNull Function<Object @NotNull [], @NotNull T> invoker;

    /**
     * Creates a new constructor wrapper.
     *
     * @param containingClass class containing the wrapped object
     * @param wrapped wrapped object
     * @param invoker function performing the constructor invocation
     */
    protected InvokeConstructorWrapper(final @NotNull Class<? extends T> containingClass,
                                       final @NotNull Constructor<? extends T> wrapped,
                                       final @NotNull Function<@NotNull Object[], @NotNull T> invoker) {
        super(containingClass, wrapped);
        this.invoker = invoker;
    }

    /**
     * Creates a new cached constructor wrapper for the given constructor.
     *
     * @param constructor constructor to wrap
     * @param <T> type of the object instantiated by the constructor
     * @return cached constructor wrapper for the given constructor
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows(ExecutionException.class)
    public static <@NotNull T> @NotNull ConstructorWrapper<T> from(
            final @NonNull Constructor<? extends T> constructor
    ) {
        return (ConstructorWrapper<T>) WRAPPER_CACHE.get(constructor, () -> {
            switch (constructor.getParameterCount()) {
                case 0: return from(
                        constructor, (Supplier<T>) generateFrom(Supplier.class, constructor)
                );
                case 1: return from(
                        constructor, (Function<Object, T>) generateFrom(Function.class, constructor)
                );
                case 2: return from(
                        constructor, (BiFunction<Object, Object, T>) generateFrom(BiFunction.class, constructor));
                default: return from(
                        constructor, InvokeUtil.lookup(constructor.getDeclaringClass())
                                .unreflectConstructor(constructor)
                );
            }
        });
    }

    private static <@NotNull T> @NotNull ConstructorWrapper<T> from(
            final @NotNull Constructor<? extends T> constructor,
            final @NotNull Supplier<T> generatedSupplier
    ) {
        return new InvokeConstructorWrapper<>(
                constructor.getDeclaringClass(), constructor,
                parameters -> {
                    ReflectorWrappers.validateParameterCount(0, parameters);

                    return generatedSupplier.get();
                }
        );
    }

    private static <@NotNull T> @NotNull ConstructorWrapper<T> from(
            final @NotNull Constructor<? extends T> constructor,
            final @NotNull Function<Object, T> generatedFunction
    ) {
        return new InvokeConstructorWrapper<>(
                constructor.getDeclaringClass(), constructor,
                parameters -> {
                    ReflectorWrappers.validateParameterCount(1, parameters);

                    return generatedFunction.apply(parameters[0]);
                }
        );
    }

    private static <@NotNull T> @NotNull ConstructorWrapper<T> from(
            final @NotNull Constructor<? extends T> constructor,
            final @NotNull BiFunction<Object, Object, T> generatedBiFunction
    ) {
        return new InvokeConstructorWrapper<>(
                constructor.getDeclaringClass(), constructor,
                parameters -> {
                    ReflectorWrappers.validateParameterCount(2, parameters);

                    return generatedBiFunction.apply(parameters[0], parameters[1]);
                }
        );
    }

    @SuppressWarnings("unchecked")
    private static <@NotNull T> @NotNull ConstructorWrapper<T> from(
            final @NotNull Constructor<? extends T> constructor,
            final @NotNull MethodHandle methodHandle
    ) {
        return new InvokeConstructorWrapper<>(
                constructor.getDeclaringClass(), constructor,
                (ThrowingFunction<Object[], T, ?>) parameters -> (T) methodHandle.invokeWithArguments(parameters)
        );
    }

    @Override
    public @NotNull T invoke(final Object @NotNull ... parameters) {
        return invoker.apply(parameters);
    }

    private static <@NotNull F, @NotNull T> @NotNull F generateFrom(
            final @NotNull Class<F> functionalType,
            final @NotNull Constructor<T> constructor
    ) {
        return InvokeUtil.<F, T>invokeFactory()
                .implementing(functionalType)
                .via(constructor)
                .createUnsafely();
    }
}
