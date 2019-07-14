package ru.progrm_jarvis.javacommons.pair;

/**
 * Mutable {@link Pair}.
 *
 * @param <F> type of the first value
 * @param <S> type of the second value
 */
public interface MutablePair<F, S> extends Pair<F, S> {

    /**
     * Sets the first value to the one given.
     *
     * @param first the value to set as the first
     */
    void setFirst(F first);

    /**
     * Sets the second value to the one given.
     *
     * @param second the value to set as the second
     */
    void setSecond(S second);
}
