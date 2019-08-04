package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link NestingTextModel} which delegates its {@link List list} methods to the inner {@link List}/
 *
 * @param <T>
 */
@Data
@RequiredArgsConstructor(access = AccessLevel.PROTECTED) // allow extension
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class DelegatingNestingTextModel<T> implements NestingTextModel<T> {

    /**
     * Content of this {@link NestingTextModel nesting text model}
     */
    @Delegate @NonNull List<TextModel<T>> elements;

    /**
     * Creates a new {@link DelegatingNestingTextModel} using the given collection fot its backend.
     *
     * @param elements elements to use as this {@link NestingTextModel nesting text model's} content
     * @param <T> type of object according to which the text model is formatted
     * @return created {@link DelegatingNestingTextModel}
     */
    public static <T> DelegatingNestingTextModel<T> from(@NonNull final List<TextModel<T>> elements) {
        return new DelegatingNestingTextModel<>(elements);
    }

    /**
     * Creates a new {@link DelegatingNestingTextModel} using the copy of the given collection fot its backend.
     *
     * @param elements elements copied to the new collection which will be used
     * as this {@link NestingTextModel nesting text model's} content
     * @param <T> type of object according to which the text model is formatted
     * @return created {@link DelegatingNestingTextModel}
     */
    public static <T> DelegatingNestingTextModel<T> fromCopyOf(@NonNull final List<TextModel<T>> elements) {
        return new DelegatingNestingTextModel<>(new ArrayList<>(elements));
    }
}
