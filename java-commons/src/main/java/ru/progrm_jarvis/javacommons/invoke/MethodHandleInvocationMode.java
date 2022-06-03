package ru.progrm_jarvis.javacommons.invoke;

import ru.progrm_jarvis.padla.annotation.EnumHelper;

import java.lang.invoke.MethodHandle;

/**
 * Mode of {@link MethodHandle} invocation.
 */
@EnumHelper
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
