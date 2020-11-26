package ru.progrm_jarvis.javacommons.invoke;

import lombok.NonNull;

import java.lang.invoke.MethodHandle;

/**
 * Mode of {@link java.lang.invoke.MethodHandle} invocation.
 */
public enum MethodHandleInvocationMode {

    /**
     * Invocation using {@link MethodHandle#invokeExact(Object...)}
     */
    EXACT {
        @Override
        public Object invoke(final @NonNull MethodHandle methodHandle,
                             final @NonNull Object... parameters) throws Throwable {
            return methodHandle.invokeExact(methodHandle, parameters);
        }
    },
    /**
     * Invocation using {@link MethodHandle#invoke(Object[])}
     */
    MIXED {
        @Override
        public Object invoke(final @NonNull MethodHandle methodHandle,
                             final @NonNull Object... parameters) throws Throwable {
            return methodHandle.invoke(methodHandle, parameters);
        }
    },
    /**
     * Invocation using {@link MethodHandle#invokeWithArguments(Object[])}
     */
    VARARG {
        @Override
        public Object invoke(final @NonNull MethodHandle methodHandle,
                             final @NonNull Object... parameters) throws Throwable {
            return methodHandle.invokeWithArguments(methodHandle, parameters);
        }
    };

    /**
     * <b>Theoretically</b> performs an invocation using the given method handle.
     *
     * @param methodHandle method handle to use for invocation
     * @param parameters method parameters
     * @return result of method invocation
     * @throws Throwable if an exception occurs while invoking
     *
     * @deprecated The called method is marked as {@code @PolymorphicSignature} and so directly depends on compile
     * type of the caller which here is always array of {@link Object Objects}
     */
    @Deprecated
    public abstract Object invoke(@NonNull MethodHandle methodHandle,
                                  @NonNull Object... parameters) throws Throwable;
}
