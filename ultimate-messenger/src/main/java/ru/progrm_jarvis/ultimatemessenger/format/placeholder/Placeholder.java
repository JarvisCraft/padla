package ru.progrm_jarvis.ultimatemessenger.format.placeholder;

import ru.progrm_jarvis.ultimatemessenger.format.StringFormatter;

/**
 * A {@link StringFormatter} which is made to replace special tokens in the source text with dynamic values.
 *
 * @param <T> type of the object according to which the string should be formatted
 */
@FunctionalInterface
public interface Placeholder<T> extends StringFormatter<T> {
}
