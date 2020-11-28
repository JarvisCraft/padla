package ru.progrm_jarvis.javacommons.object;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

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

    /**
     * Creates a new pair of the given values.
     *
     * @param first first value of the pair
     * @param second second value of the pair
     * @param <F> type of the first value of the pair
     * @param <S> type of the second value of the pair
     * @return created pair of the given values
     */
    static <F, S> @NotNull Pair<F, S> of(final F first, final S second) {
        return new SimplePair<>(first, second);
    }

    /**
     * Simple implementation of {@link Pair}.
     *
     * @param <F> type of the first value
     * @param <S> type of the second value
     */
    @Value
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class SimplePair<F, S> implements Pair<F, S> {

        /**
         * The first value of this pair.
         */
        F first;

        /**
         * The second value of this pair.
         */
        S second;
    }
}
