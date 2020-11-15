package ru.progrm_jarvis.reflector.wrapper;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * {@link MethodWrapper} requiring a target object (non-bound non-static method).
 *
 * @param <T> type of the object containing the wrapped method
 * @param <R> type of the method's return-value
 */
public interface DynamicMethodWrapper<@NotNull T, R>
        extends MethodWrapper<T, R>, DynamicInvokeableWrapper<T, Method, R> {}
