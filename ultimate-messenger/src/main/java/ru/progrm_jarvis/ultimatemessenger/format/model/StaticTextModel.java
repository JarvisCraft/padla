package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.FieldDefaults;
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
     * Instantiates a new static text model of the given text.
     *
     * @param text text of this text model
     *
     * @implNote this is not generated via Lombok because there is field dependency
     * ({@code length} is based on {@code text})
     */
    public StaticTextModel(@NotNull final String text) {
        this.text = text;
        this.length = OptionalInt.of(text.length());
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    @NotNull public OptionalInt getMinLength() {
        return length;
    }

    @Override
    @NotNull public OptionalInt getMaxLength() {
        return length;
    }

    @Override
    @Contract(pure = true)
    @NotNull public String getText(@Nullable final T target) {
        return text;
    }

    /**
     * Gets a static text model of the gicen text.
     *
     * @param text text of the static text model
     * @param <T> type of object according to which the text model is formatted (actually, not used)
     * @return text model of the given text
     */
    public static <T> TextModel<T> of(@NonNull final String text) {
        return text.isEmpty() ? TextModel.empty() : new StaticTextModel<>(text);
    }
}
