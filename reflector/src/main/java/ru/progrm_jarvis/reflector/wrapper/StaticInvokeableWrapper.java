package ru.progrm_jarvis.reflector.wrapper;

public interface StaticInvokeableWrapper<T, W, R> extends ReflectorWrapper<T, W> {

    R invoke(Object... parameters);
}
