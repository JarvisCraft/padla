package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.OptionalInt;

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
     * @param target object according to which the text models gets formatted,
     * if the model is not {@link #isDynamic() dynamic} then it should return the same result for any {@code target}
     * including {@code null}
     * @return text formatted for the given target
     *
     * @throws NullPointerException if the target is {@code null} but this text model is {@link #isDynamic() dynamic}
     */
    @NotNull String getText(T target);

    /**
     * Retrieves whether this text model is dynamic.
     *
     * @return {@code true} if this text model may produce different values
     * and {@code false} if it produces equal values for all calls to {@link #getText(Object)}
     */
    @Contract(pure = true)
    default boolean isDynamic() {
        return true;
    }

    /**
     * Gets the minimal length of the value returned by {@link #getText(Object)}.
     *
     * @return minimal possible length of the value returned by {@link #getText(Object)}
     */
    default @Range(from = 0, to = Integer.MAX_VALUE) int getMinLength() {
        return 0;
    }

    /**
     * Gets the maximal length of the value returned by {@link #getText(Object)}.
     *
     * @return maximal possible length of the value returned by {@link #getText(Object)}
     * or {@link Integer#MAX_VALUE} if it is unknown or may be bigger than {@link Integer#MAX_VALUE}
     */
    default @Range(from = 0, to = Integer.MAX_VALUE) int getMaxLength() {
        return Integer.MAX_VALUE;
    }

    /**
     * Returns an empty static text model.
     *
     * @param <T> type of object according to which the text model is formatted
     * @return singleton of an empty static text model
     */
    @SuppressWarnings("unchecked")
    static <T> @NotNull TextModel<T> empty() {
        return (TextModel<T>) EmptyTextModel.INSTANCE;
    }

    /**
     * Creates a text model of the given static text.
     *
     * @param text static text of the text model
     * @param <T> type of object according to which the text model is formatted (effectively unused)
     * @return text model of the given static text
     */
    static @NotNull <T> TextModel<T> of(final @NonNull String text) {
        return new StaticTextModel<>(text);
    }

    /**
     * Empty static text model.
     */
    @SuppressWarnings("rawtypes") // behaviour is independent of the generic type
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    final class EmptyTextModel implements TextModel {

        /**
         * Hash-code of an empty value.
         */
        private static final int EMPTY_HASHCODE = 0;

        /**
         * Singleton instance of this text model
         */
        private static final TextModel<?> INSTANCE = new EmptyTextModel();

        @Override
        @Contract(pure = true)
        public @NotNull String getText(final @Nullable Object target) {
            return ""; // thanks to JVM magic this is always the same object (got using LDC)
        }

        @Override
        @Contract(pure = true)
        public @Range(from = 0, to = Integer.MAX_VALUE) int getMinLength() {
            return 0;
        }

        @Override
        @Contract(pure = true)
        public @Range(from = 0, to = Integer.MAX_VALUE) int getMaxLength() {
            return 0;
        }

        @Override
        @Contract(pure = true)
        public boolean isDynamic() {
            return false;
        }

        @Override
        @Contract(pure = true)
        public boolean equals(final @Nullable Object object) {
            if (object == this) return true;
            if (object instanceof TextModel) {
                final TextModel<?> textModel;
                return !(textModel = (TextModel<?>) object).isDynamic()
                        && textModel.hashCode() == EMPTY_HASHCODE
                        && textModel.getText(null).isEmpty();
            }
            return false;
        }

        @Override
        @Contract(pure = true)
        public int hashCode() {
            return EMPTY_HASHCODE;
        }

        @Override
        @Contract(pure = true)
        public String toString() {
            return "TextModel{\"\"}";
        }
    }

    /**
     * Text model representing a block of static text.
     *
     * @param <T> type of object according to which the text model is formatted (actually, not used)
     */
    @Value
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class StaticTextModel<T> implements TextModel<T> {

        /**
         * Text of this text model
         */
        @NonNull String text;

        @Override
        @Contract(pure = true)
        public boolean isDynamic() {
            return false;
        }

        @Override
        @Contract(pure = true)
        public @Range(from = 0, to = Integer.MAX_VALUE) int getMinLength() {
            return text.length();
        }

        @Override
        @Contract(pure = true)
        public @Range(from = 0, to = Integer.MAX_VALUE) int getMaxLength() {
            return text.length();
        }

        @Override
        @Contract(pure = true)
        public @NotNull String getText(final @Nullable T target) {
            return text;
        }

        @Override
        @Contract(pure = true)
        public boolean equals(final @Nullable Object object) {
            if (object == this) return true;
            if (object instanceof TextModel) {
                final TextModel<?> textModel;
                return !(textModel = (TextModel<?>) object).isDynamic() // cheapest check
                        && textModel.hashCode() == hashCode() // `Object#equals(Object)` contract
                        && textModel.getText(null).equals(text);
            }
            return false;
        }

        @Override
        @Contract(pure = true)
        public int hashCode() {
            return text.hashCode();
        }
    }
}
