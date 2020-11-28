package ru.progrm_jarvis.reflector.wrapper;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

/**
 * Wrapper of the {@link Constructor}.
 *
 * @param <T> type of object instantiated by the wrapped constructor
 */
public interface ConstructorWrapper<@NotNull T>
        extends StaticInvokeableWrapper<T, Constructor<? extends T>, @NotNull T> {}
