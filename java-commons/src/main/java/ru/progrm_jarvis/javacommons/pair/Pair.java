package ru.progrm_jarvis.javacommons.pair;

/**
 * A container holding two values.
 *
 * @param <F> type of the first value
 * @param <S> type of the second value
 */
public interface Pair<F, S> {

    /**
     * Gets the first value.
     *
     * @return the first value
     */
    F getFirst();

    /**
     * Gets the second value.
     *
     * @return the second value
     */
    S getSecond();
}
