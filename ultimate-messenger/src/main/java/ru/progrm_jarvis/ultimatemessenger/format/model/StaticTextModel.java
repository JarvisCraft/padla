package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

/**
 * Text model representing a block of static text.
 *
 * @param <T> type of object according to which the text model is formatted (actually, not used)
 */
@Value(staticConstructor = "of")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaticTextModel<T> implements TextModel<T> {

    @NonNull String text;

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    @NotNull public String getText(@NotNull final T target) {
        return text;
    }
}
