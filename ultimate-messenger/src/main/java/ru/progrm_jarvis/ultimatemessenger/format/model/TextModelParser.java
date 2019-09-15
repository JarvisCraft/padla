package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Parser of a {@link TextModel text model}.
 *
 * @param <T> type of object according to which the text model is formatted
 */
@FunctionalInterface
public interface TextModelParser<T> {

    /**
     * Parses the {@link TextModel} from the gicen text.
     *
     * @param factory factory to use for creation of text model
     * @param text text to parse
     * @return parsed text model
     */
    @NotNull TextModel<T> parse(@NonNull TextModelFactory<T> factory, @NonNull String text);
}
