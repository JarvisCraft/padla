package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link TextModel Text model} consisting of other {@link TextModel text models}.
 *
 * @param <T> type of object according to which the text model is formatted
 */
public interface CompoundTextModel<T> extends TextModel<T>, List<TextModel<T>> {

    @Override
    default @NotNull String getText(@NotNull T target) {
        return stream()
                .map(element -> element.getText(target))
                .collect(Collectors.joining());
    }

    @Override
    default void write(final @NonNull DataOutputStream output, final T target) throws IOException {
        for (val element : this) element.write(output, target);
    }

    @Override
    default void write(final @NonNull Writer output, final T target) throws IOException {
        for (val element : this) element.write(output, target);
    }

    @Override
    default void write(final @NonNull PrintWriter output, final T target) {
        for (val element : this) element.write(output, target);
    }
}
