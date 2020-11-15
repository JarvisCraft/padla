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
import ru.progrm_jarvis.reflector.wrapper.ReflectorWrappers;

import java.lang.invoke.MethodHandle;
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
    private static final @NotNull Cache<@NotNull Method, @NotNull DynamicMethodWrapper<?, ?>> CACHE
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

            val noReturn = method.getReturnType() == void.class;
            switch (method.getParameterCount()) {
                case 0: return noReturn
                        ? from(method, (Consumer<Object>) generateImplementation(Consumer.class, method))
                        : from(method, (Function<Object, R>) generateImplementation(Function.class, method));
                case 1: return noReturn
                        ? from(method, (BiConsumer<Object, Object>) generateImplementation(BiConsumer.class, method))
                        : from(method, (BiFunction<Object, Object, R>) generateImplementation(BiFunction.class, method)
                );
                default: return from(method, InvokeUtil.lookup(method.getDeclaringClass()).unreflect(method));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <@NotNull T, R> @NotNull DynamicMethodWrapper<T, R> from(
            final @NotNull Method method,
            final @NotNull Consumer<Object> generatedConsumer
    ) {
        return new InvokeDynamicMethodWrapper<>(
                (Class<? extends T>) method.getDeclaringClass(), method,
                (target, parameters) -> {
                    ReflectorWrappers.validateParameterCount(0, parameters);

                    generatedConsumer.accept(target);

                    return null;
                }
        );
    }

    @SuppressWarnings("unchecked")
    private static <@NotNull T, R> @NotNull DynamicMethodWrapper<T, R> from(
            final @NotNull Method method,
            final @NotNull BiConsumer<Object, Object> generatedBiConsumer
    ) {
        return new InvokeDynamicMethodWrapper<>(
                (Class<? extends T>) method.getDeclaringClass(), method,
                (target, parameters) -> {
                    ReflectorWrappers.validateParameterCount(1, parameters);

                    generatedBiConsumer.accept(target, parameters[0]);

                    return null;
                }
        );
    }

    @SuppressWarnings("unchecked")
    private static <@NotNull T, R> @NotNull DynamicMethodWrapper<T, R> from(
            final @NotNull Method method,
            final @NotNull Function<Object, R> generatedFunction
    ) {
        return new InvokeDynamicMethodWrapper<>(
                (Class<? extends T>) method.getDeclaringClass(), method,
                (target, parameters) -> {
                    ReflectorWrappers.validateParameterCount(0, parameters);

                    return generatedFunction.apply(target);
                }
        );
    }

    @SuppressWarnings("unchecked")
    private static <@NotNull T, R> @NotNull DynamicMethodWrapper<T, R> from(
            final @NotNull Method method,
            final @NotNull BiFunction<Object, Object, R> generatedBiFunction
    ) {
        return new InvokeDynamicMethodWrapper<>(
                (Class<? extends T>) method.getDeclaringClass(), method,
                (target, parameters) -> {
                    ReflectorWrappers.validateParameterCount(1, parameters);

                    return generatedBiFunction.apply(target, parameters[0]);
                }
        );
    }

    @SuppressWarnings("unchecked")
    private static <@NotNull T, R> @NotNull DynamicMethodWrapper<T, R> from(
            final @NotNull Method method,
            final @NotNull MethodHandle methodHandle
    ) {
        val parameterCount = method.getParameterCount();

        return new InvokeDynamicMethodWrapper<>(
                (Class<? extends T>) method.getDeclaringClass(), method,
                (ThrowingBiFunction<T, Object[], R, Throwable>) (target, parameters) -> {
                    final int parametersLength;
                    ReflectorWrappers.validateParameterCount(parameterCount, parametersLength = parameters.length);

                    final int length;
                    final Object[] arguments;
                    (arguments = new Object[(length = parametersLength) + 1])[0] = target;
                    System.arraycopy(parameters, 0, arguments, 1, length);

                    return (R) methodHandle.invokeWithArguments(arguments);
                }
        );
    }

    @Override
    public R invoke(final @NotNull T target, final Object @NotNull ... parameters) {
        return invoker.apply(target, parameters);
    }

    private static <@NotNull F, @NotNull T> @NotNull F generateImplementation(
            final @NotNull Class<F> functionalType,
            final @NotNull Method method
    ) {
        return InvokeUtil.<F, T>invokeFactory()
                .implementing(functionalType)
                .via(method)
                .createUnsafely();
    }
}
