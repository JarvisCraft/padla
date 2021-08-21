package ru.progrm_jarvis.reflector.wrapper.invoke;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.cache.Cache;
import ru.progrm_jarvis.javacommons.cache.Caches;
import ru.progrm_jarvis.javacommons.invoke.InvokeUtil;
import ru.progrm_jarvis.javacommons.object.Pair;
import ru.progrm_jarvis.javacommons.util.function.ThrowingFunction;
import ru.progrm_jarvis.reflector.wrapper.AbstractMethodWrapper;
import ru.progrm_jarvis.reflector.wrapper.ReflectorWrappers;
import ru.progrm_jarvis.reflector.wrapper.StaticMethodWrapper;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.*;

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
     * Weak cache of allocated instance of this static method wrappers of static methods
     */
    protected static final @NotNull Cache<@NotNull Method, @NotNull StaticMethodWrapper<?, ?>> STATIC_WRAPPER_CACHE
            = Caches.weakValuesCache();
    /**
     * Weak cache of allocated instance of this static method wrappers of non-static bound methods
     */
    protected static final @NotNull Cache<
            @NotNull Pair<@NotNull Method, @NotNull ?>, @NotNull StaticMethodWrapper<?, ?>
            > BOUND_WRAPPER_CACHE
            = Caches.weakValuesCache();
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
    public static <@NonNull T, R> @NotNull StaticMethodWrapper<T, R> from(final @NonNull Method method) {
        if (!Modifier.isStatic(method.getModifiers())) throw new IllegalArgumentException("Method should be static");

        return (StaticMethodWrapper<T, R>) STATIC_WRAPPER_CACHE.get(method, checkedMethod -> {
            val noReturn = checkedMethod.getReturnType() == void.class;
            switch (checkedMethod.getParameterCount()) {
                case 0: return noReturn ? from(
                        checkedMethod, generateFrom(Runnable.class, checkedMethod)
                ) : from(
                        checkedMethod, (Supplier<T>) generateFrom(Supplier.class, checkedMethod)
                );
                case 1: return noReturn ? from(
                        checkedMethod, (Consumer<Object>) generateFrom(Consumer.class, checkedMethod)
                ) : from(
                        checkedMethod, (Function<Object, R>) generateFrom(Function.class, checkedMethod)
                );
                case 2: return noReturn ? from(
                        checkedMethod, (BiConsumer<Object, Object>) generateFrom(BiConsumer.class, checkedMethod)
                ) : from(
                        checkedMethod, (BiFunction<Object, Object, R>) generateFrom(BiFunction.class, checkedMethod)
                );
                default: {
                    final MethodHandle methodHandle;
                    {
                        val lookup = InvokeUtil.lookup(method.getDeclaringClass());
                        try {
                            methodHandle = lookup.unreflect(checkedMethod);
                        } catch (final IllegalAccessException e) {
                            throw new RuntimeException(
                                    "Cannot create MethodHandle for constructor " + checkedMethod, e
                            );
                        }
                    }
                    return from(checkedMethod, methodHandle);
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
    public static <@NonNull T, R> @NotNull StaticMethodWrapper<T, R> from(
            final @NonNull Method method,
            final @NonNull T target
    ) {
        if (Modifier.isStatic(method.getModifiers())) throw new IllegalArgumentException("Method should be non-static");

        return (StaticMethodWrapper<T, R>) BOUND_WRAPPER_CACHE.get(Pair.of(method, target), pair -> {
            val checkedMethod = pair.getFirst();
            val noReturn = checkedMethod.getReturnType() == void.class;
            switch (checkedMethod.getParameterCount()) {
                case 0: return noReturn
                        ? from(checkedMethod, generateFrom(Runnable.class, checkedMethod, target))
                        : from(checkedMethod, generateFrom(Supplier.class, checkedMethod, target));
                case 1: return noReturn
                        ? from(checkedMethod, generateFrom(Consumer.class, checkedMethod, target))
                        : from(checkedMethod, generateFrom(Function.class, checkedMethod, target));
                case 2: return noReturn
                        ? from(checkedMethod, generateFrom(BiConsumer.class, checkedMethod, target))
                        : from(checkedMethod, generateFrom(BiFunction.class, checkedMethod, target));
                default: {
                    final MethodHandle methodHandle;
                    {
                        val lookup = InvokeUtil.lookup(method.getDeclaringClass());
                        try {
                            methodHandle = lookup.unreflect(checkedMethod);
                        } catch (final IllegalAccessException e) {
                            throw new RuntimeException(
                                    "Cannot create MethodHandle for constructor " + pair, e
                            );
                        }
                    }
                    return from(checkedMethod, methodHandle.bindTo(target));
                }
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
                    ReflectorWrappers.validateParameterCount(0, parameters);

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
                    ReflectorWrappers.validateParameterCount(1, parameters);

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
                    ReflectorWrappers.validateParameterCount(2, parameters);

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
                    ReflectorWrappers.validateParameterCount(0, parameters);

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
                    ReflectorWrappers.validateParameterCount(1, parameters);

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
                    ReflectorWrappers.validateParameterCount(2, parameters);

                    return generatedBiFunction.apply(parameters[0], parameters[1]);
                }
        );
    }

    @SuppressWarnings("unchecked")
    private static <@NotNull T, R> @NotNull StaticMethodWrapper<T, R> from(
            final @NotNull Method method,
            final @NotNull MethodHandle methodHandle
    ) {
        val parameterCount = method.getParameterCount();

        return new InvokeStaticMethodWrapper<>(
                (Class<? extends T>) method.getDeclaringClass(), method,
                (ThrowingFunction<Object[], R, ?>) parameters -> {
                    ReflectorWrappers.validateParameterCount(parameterCount, parameters);

                    return (R) methodHandle.invokeWithArguments(parameters);
                }
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
