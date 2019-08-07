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

    /**
     * Gets the text formatted for the given target.
     *
     * @param target object according to which the text models gets formatted
     * @return text formatted for the given target
     */
    @NotNull String getText(@NotNull T target);

    /**
     * Retrieves whether this {@link TextModel text model} is dynamic.
     *
     * @return {@code true} if this {@link TextModel text model} may produce different values
     * and {@code false} if it produces equal values for all calls to {@link #getText(Object)}
     */
    @Contract(pure = true)
    default boolean isDynamic() {
        return true;
    }

    /**
     * Returns an empty static {@link TextModel text model}.
     *
     * @param <T> type of object according to which the text model is formatted
     * @return singleton of an empty static {@link TextModel text model}
     */
    @SuppressWarnings("unchecked")
    static <T> TextModel<T> empty() {
        return EmptyTextModel.INSTANCE;
    }

    /**
     * Empty static {@link TextModel text model}.
     */
    final class EmptyTextModel implements TextModel {

        /**
         * Singleton instance of this {@link TextModel text model}
         */
        private static EmptyTextModel INSTANCE = new EmptyTextModel();

        @Override
        @NotNull public String getText(@NotNull final Object target) {
            return "";
        }

        @Override
        public boolean isDynamic() {
            return false;
        }
    }
}
