package ru.progrm_jarvis.reflector.wrapper;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * {@link MethodWrapper} not requiring any target object (static method or a bound non-static method).
 *
 * @param <T> type of the object containing the wrapped method
 * @param <R> type of the method's return-value
 */
public interface StaticMethodWrapper<T, R> extends MethodWrapper<T, R>, StaticInvokeableWrapper<T, Method, R> {

    R invoke(@NotNull Object... parameters);
}
