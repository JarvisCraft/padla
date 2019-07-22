package ru.progrm_jarvis.reflector.wrapper;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public interface StaticMethodWrapper<T, R> extends MethodWrapper<T, R>, StaticInvokeableWrapper<T, Method, R> {

    R invoke(@NotNull Object... parameters);
}
