package ru.progrm_jarvis.reflector.wrapper;

import org.jetbrains.annotations.NotNull;

/**
 * An invokable wrapper requiring a target object.
 *
 * @param <T> type of the target object containing the wrapped object
 * @param <W> type of the wrapped object
 * @param <R> return-type of the invocation
 */
public interface DynamicInvokeableWrapper<@NotNull T, @NotNull W, R> extends ReflectorWrapper<T, W> {

    /**
     * Performs the invocation.
     *
     * @param target target on which the invocation should happen
     * @param parameters parameters of the invocation
     * @return result of the invocation
     */
    R invoke(@NotNull T target, Object @NotNull ... parameters);
}
