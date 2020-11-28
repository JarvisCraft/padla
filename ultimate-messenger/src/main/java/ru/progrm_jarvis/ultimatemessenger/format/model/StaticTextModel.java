package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

/**
 * Text model representing a block of static text.
 *
 * @param <T> type of object according to which the text model is formatted (actually, not used)
 */
@Value
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaticTextModel<T> implements TextModel<T> {

    /**
     * Text of this text model
     */
    @NonNull String text;
    /**
     * Length of ths text model's {@link #text text} wrapped in {@link OptionalInt}
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") @NonNull OptionalInt length;
    /**
     * {@link Object#hashCode() Hash-code} of this static text model
     * (effectively {@link String#hashCode()} of {@link #text})
     */
    int hashCode;

    /**
     * Instantiates a new static text model of the given text.
     *
     * @param text text of this text model
     *
     * @implNote this is not generated via Lombok because there is field dependency
     * ({@link #length} and {@link #hashCode}are based on {@link #text})
     */
    public StaticTextModel(final @NotNull String text) {
        this.text = text;
        length = OptionalInt.of(text.length());
        hashCode = text.hashCode();
    }

    @Override
    @Contract(pure = true)
    public boolean isDynamic() {
        return false;
    }

    @Override
    @Contract(pure = true)
    public @NotNull OptionalInt getMinLength() {
        return length;
    }

    @Override
    @Contract(pure = true)
    public @NotNull OptionalInt getMaxLength() {
        return length;
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
            val textModel = (TextModel<?>) object;
            return !textModel.isDynamic() && textModel.hashCode() == hashCode && textModel.getText(null).equals(text);
        }
        return false;
    }

    @Override
    @Contract(pure = true)
    public int hashCode() {
        return hashCode;
    }

    /**
     * Gets a static text model of the given text.
     *
     * @param text text of the static text model
     * @param <T> type of object according to which the text model is formatted (actually, not used)
     * @return text model of the given text
     */
    public static <T> TextModel<T> of(final @NonNull String text) {
        return text.isEmpty() ? TextModel.empty() : new StaticTextModel<>(text);
    }
}
