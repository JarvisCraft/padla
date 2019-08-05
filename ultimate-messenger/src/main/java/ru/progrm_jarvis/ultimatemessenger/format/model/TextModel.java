package ru.progrm_jarvis.ultimatemessenger.format.model;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Parsed model of a dynamic text.
 *
 * @param <T> type of object according to which the text model is formatted
 */
@FunctionalInterface
public interface TextModel<T> {

    @NotNull String getText(@NotNull T target);

    @Contract(pure = true)
    default boolean isDynamic() {
        return true;
    }

    static <T> TextModel<T> empty() {
        return target -> "";
    }
}
