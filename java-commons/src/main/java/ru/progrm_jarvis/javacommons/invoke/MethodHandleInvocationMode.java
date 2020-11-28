package ru.progrm_jarvis.javacommons.invoke;

import java.lang.invoke.MethodHandle;

/**
 * Mode of {@link java.lang.invoke.MethodHandle} invocation.
 */
public enum MethodHandleInvocationMode {

    /**
     * Invocation using {@link MethodHandle#invokeExact(Object...)}
     */
    EXACT,
    /**
     * Invocation using {@link MethodHandle#invoke(Object[])}
     */
    MIXED,
    /**
     * Invocation using {@link MethodHandle#invokeWithArguments(Object[])}
     */
    VARARG
}
