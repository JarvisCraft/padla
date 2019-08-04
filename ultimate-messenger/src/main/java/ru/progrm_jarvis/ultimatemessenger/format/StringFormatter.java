package ru.progrm_jarvis.ultimatemessenger.format;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

/**
 * Placeholder which updates the given {@link String string} specifically for the given target.
 *
 * @param <T> type of the object according to which the string should be formatted
 */
@FunctionalInterface
public interface StringFormatter<T> extends BiFunction<String, T, String> {

    /**
     * Formats the given {@link String string} specifically for the given target.
     *
     * @param source source {@link String string} which should be formatted
     * @param target target for which the formatting should happen
     * @return formatted {@link String}
     *
     * @apiNote the method should (but is not forced to) return the source string object
     * in case there were no changes to it, however
     */
    String format(@NotNull String source, T target);

    @Override
    default String apply(final String source, final T target) {
        return format(source, target);
    }
}
