package ru.progrm_jarvis.javacommons.random;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility for getting random values.
 */
@UtilityClass
public class RandomUtil {

    /**
     * Provides {@code 1} or {@code -1} randomly.
     *
     * @return {@code 1} or {@code -1}
     */
    public int randomSign() {
        return ThreadLocalRandom.current().nextBoolean() ? 1 : -1;
    }

    /**
     * Gets a random value from the map specified considering chances.
     *
     * @param chancedValues values from which to get a random one
     * the keys of the map are the actual values, the values of the map are the chances
     * @param <T> type of value randomly got
     * @return got random value
     *
     * @throws IllegalArgumentException if {@code values is empty}
     */
    public <T> T getRandom(final @NonNull Map<T, Integer> chancedValues) {
        {
            val size = chancedValues.size();

            if (size <= 0) throw new IllegalArgumentException("There should be at least one chanced value");
            if (size == 1) return chancedValues.keySet().iterator().next();
        }

        long chancesSum = 0; // sum of all chances
        for (val chance : chancedValues.values()) {
            if (chance <= 0) throw new IllegalArgumentException("All chances should be positive");
            chancesSum += chance;
        }
        // the chance should be up to chancesSum (exclusive)
        val chance = ThreadLocalRandom.current().nextLong(chancesSum);
        for (val entry : chancedValues.entrySet()) if ((chancesSum -= entry.getValue()) <= chance) return
                entry.getKey();

        throw new IllegalStateException("Could not get any chanced value");
    }

    /**
     * Gets a random value from the list specified.
     *
     * @param values values from which to get a random one
     * @param <T> type of value randomly got
     * @return got random value
     *
     * @throws IllegalArgumentException if {@code values is empty}
     */
    public <T> T getRandom(final @NonNull List<T> values) {
        final int size;
        if ((size = values.size()) < 1) throw new IllegalArgumentException(
                "There should be at least one chanced value"
        );
        if (size == 1) return values.get(0);

        return values.get(ThreadLocalRandom.current().nextInt(size));
    }

    /**
     * Gets a random value from the collection specified.
     *
     * @param values values from which to get a random one
     * @param <T> type of value randomly got
     * @return got random value
     *
     * @throws IllegalArgumentException if {@code values is empty}
     */
    public <T> T getRandom(final @NonNull Collection<T> values) {
        int index;
        {
            final int size;
            if ((size = values.size()) < 1) throw new IllegalArgumentException(
                    "There should be at least one chanced value"
            );
            if (size == 1) return values.iterator().next();

            index = ThreadLocalRandom.current().nextInt(size);
        }

        for (val value : values) if (index-- == 0) return value;

        throw new IllegalStateException("Could not get any value");
    }
}
