package ru.progrm_jarvis.ultimatemessenger.format.model;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link TextModel Text model} consisting of other {@link TextModel text models}.
 *
 * @param <T> type of object according to which the text model is formatted
 */
public interface NestingTextModel<T> extends TextModel<T>, List<TextModel<T>> {

    @Override
    @NotNull default String getText(@NotNull T target) {
        return stream()
                .map(element -> element.getText(target))
                .collect(Collectors.joining());
    }
}
