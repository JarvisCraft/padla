package ru.progrm_jarvis.javacommons.object;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

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

    /**
     * Creates a new mutable pair of the given values.
     *
     * @param first first value of the pair
     * @param second second value of the pair
     * @param <F> type of the first value of the pair
     * @param <S> type of the second value of the pair
     * @return created mutable pair of the given values
     */
    static <F, S> @NotNull MutablePair<F, S> of(final F first, final S second) {
        return new SimpleMutablePair<>(first, second);
    }

    /**
     * Simple implementation of {@link MutablePair}.
     *
     * @param <F> type of the first value
     * @param <S> type of the second value
     */
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    final class SimpleMutablePair<F, S> implements MutablePair<F, S> {

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
