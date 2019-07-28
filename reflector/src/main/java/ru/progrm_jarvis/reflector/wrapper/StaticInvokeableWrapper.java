package ru.progrm_jarvis.reflector.wrapper;

import org.jetbrains.annotations.NotNull;

/**
 * An invokable wrapper not requiring a target object.
 *
 * @param <T> type of the target (in case of binding) object containing the wrapped object
 * @param <W> type of the wrapped object
 * @param <R> return-type of the invocation
 */
public interface StaticInvokeableWrapper<T, W, R> extends ReflectorWrapper<T, W> {

    /**
     * Performs the invocation.
     *
     * @param parameters parameters of the invocation
     * @return result of the invocation
     */
    R invoke(@NotNull Object... parameters);
}
