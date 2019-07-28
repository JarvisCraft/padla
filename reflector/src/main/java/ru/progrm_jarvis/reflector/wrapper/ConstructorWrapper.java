package ru.progrm_jarvis.reflector.wrapper;

import java.lang.reflect.Constructor;

/**
 * Wrapper of the {@link Constructor}.
 *
 * @param <T> type of object instantiated by the wrapped constructor
 */
public interface ConstructorWrapper<T> extends StaticInvokeableWrapper<T, Constructor<? extends T>, T> {}
