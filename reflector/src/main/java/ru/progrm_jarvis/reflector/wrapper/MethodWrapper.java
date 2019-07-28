package ru.progrm_jarvis.reflector.wrapper;

import java.lang.reflect.Method;

/**
 * Wrapper of the {@link Method}.
 *
 * @param <T> type of the object containing the wrapped method
 * @param <R> type of the method's return-value
 */
public interface MethodWrapper<T, R> extends ReflectorWrapper<T, Method> {}
