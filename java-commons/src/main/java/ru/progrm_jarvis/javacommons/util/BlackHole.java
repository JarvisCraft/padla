package ru.progrm_jarvis.javacommons.util;

import lombok.experimental.UtilityClass;

/**
 * Black hole. Helper class used to {@link #consume(Object)} values without any visible side effects.
 */
@UtilityClass
public class BlackHole {

    /**
     * Consumes the provided value without any visible side effects.
     * This may be used for scenarios when an arbitrary expression has to be treated as a statement,
     * for example{@code String.class}</pre> is an expression but not a statement
     * but <pre>{@code BlackHole.consume(String.class)}</pre> is.
     *
     * @param value consumed value
     * @param <T> type of consumed value
     */
    public <T> void consume(@SuppressWarnings("unused") final T value) {}
}
