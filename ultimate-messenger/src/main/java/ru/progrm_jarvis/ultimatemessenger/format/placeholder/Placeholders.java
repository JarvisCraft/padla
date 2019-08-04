package ru.progrm_jarvis.ultimatemessenger.format.placeholder;

import lombok.NonNull;
import ru.progrm_jarvis.ultimatemessenger.format.StringFormatter;
import ru.progrm_jarvis.ultimatemessenger.format.model.TextModelParser;

import java.util.Optional;

/**
 * A group of formatters recognized by the similar placeholder pattern.
 * <p>
 * For example, a placeholder may be the value surrounded by curly brackets with the value inside being
 * the identifier and additional data which would be used to compute the value, e.g., having a formatter
 * by name "name" which, for data "current" computes the current name of the target would lead to
 * "Hello, {name:current}!" {@link String} being converted to "Hello, John!" for the target currently named "John".
 *
 * @param <T> type of the object according to which the string should be formatted
 */
public interface Placeholders<T> extends StringFormatter<T>, TextModelParser<T> {

    /**
     * Adds a new formatter to be used for placeholder replacement by the given name.
     *
     * @param name name by which this formatter should be recognized
     * @param formatter formatter used to replace the placeholder
     *
     * @throws IllegalArgumentException if the name contains illegal characters
     */
    void add(@NonNull String name, @NonNull StringFormatter<T> formatter);

    /**
     * Gets the formatter by the given name.
     *
     * @param name identifier of the formatter
     * @return optional formatter by the given name
     */
    Optional<StringFormatter<T>> get(@NonNull String name);

    /**
     * Removes the formatter by the given name.
     *
     * @param name identifier of the formatter
     * @return optional formatter removed by the given name being empty if there was no formatter by this name
     */
    Optional<StringFormatter<T>> remove(@NonNull String name);
}
