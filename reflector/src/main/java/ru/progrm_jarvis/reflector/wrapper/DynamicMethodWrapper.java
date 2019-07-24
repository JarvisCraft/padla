package ru.progrm_jarvis.reflector.wrapper;

import org.jetbrains.annotations.NotNull;

public interface DynamicMethodWrapper<T, R> extends MethodWrapper<T, R> {

    R invoke(@NotNull T target, @NotNull Object... parameters);
}
