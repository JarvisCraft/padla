package ru.progrm_jarvis.reflector.wrapper;

import org.jetbrains.annotations.NotNull;

public interface StaticInvokeableWrapper<T, W, R> extends ReflectorWrapper<T, W> {

    R invoke(@NotNull Object... parameters);
}
