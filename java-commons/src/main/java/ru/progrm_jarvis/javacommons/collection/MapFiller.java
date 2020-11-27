package ru.progrm_jarvis.javacommons.collection;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.object.Pair;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

/**
 * A helper-object to fill the map implementing the chain pattern.
 * This may be useful when initializing class fields.
 *
 * @param <K> type of map's key
 * @param <V> type of map's value
 */
public interface MapFiller<K, V> {

    /**
     * Gets the filled map.
     *
     * @return the filled map
     */
    @NotNull Map<K, V> map();

    /**
     * Puts the specified value by the specified key to the map.
     *
     * @param key key of the value to put
     * @param value value to put by the key
     * @return this map filler for chaining
     */
    @NotNull MapFiller<K, V> put(final K key, final V value);

    /**
     * Fills the map with the values of specified array.
     *
     * @param entries entries which will be put to the map
     * @return this map filler for chaining
     */
    @SuppressWarnings("unchecked")
    @NotNull MapFiller<K, V> fill(final @NotNull Pair<K, V> @NonNull ... entries);

    /**
     * Fills the map based on the specified {@link Iterator}.
     *
     * @param entries iterator of the entries whose elements will be put to the map
     * @return this map filler for chaining
     */
    @NotNull MapFiller<K, V> fill(final @NonNull Iterator<@NotNull ? extends Pair<K, V>> entries);

    /**
     * Fills the map with the values of specified {@link Iterable}.
     *
     * @param entries entries which will be put to the map
     * @return this map filler for chaining
     */
    @NotNull MapFiller<K, V> fill(final @NonNull Iterable<@NotNull ? extends Pair<K, V>> entries);

    /**
     * Fills the map based on the specified {@link Stream}.
     *
     * @param entries stream of the entries whose elements will be put to the map
     * @return this map filler for chaining
     */
    @NotNull MapFiller<K, V> fill(final @NonNull Stream<@NotNull ? extends Pair<K, V>> entries);

    /**
     * Fills the map based on the specified {@link Stream} keeping order.
     *
     * @param entries stream of the entries whose elements will be put to the map
     * @return this map filler for chaining
     */
    @NotNull MapFiller<K, V> fillOrdered(final @NonNull Stream<@NotNull ? extends Pair<K, V>> entries);


    /**
     * Creates new map filler from the map specified.
     *
     * @param map map for which to create the filler
     * @param <K> type of keys
     * @param <V> type of values
     * @return map filler created for the specified map
     *
     * @see #from(Map, Object, Object)
     */
    static <K, V> MapFiller<K, V> from(final @NonNull Map<K, V> map) {
        return new SimpleMapFiller<>(map);
    }

    /**
     * Creates new map filler from the map specified initialized with the value specified.
     *
     * @param map map for which to create the filler
     * @param firstKey the key of the first value
     * @param firstValue the first value to be put to the map
     * @param <K> type of keys
     * @param <V> type of values
     * @return map filler created for the specified map with initial value put
     *
     * @see #from(Map)
     */
    static <K, V> MapFiller<K, V> from(final @NonNull Map<K, V> map, K firstKey, final V firstValue) {
        return new SimpleMapFiller<>(map).put(firstKey, firstValue);
    }

    /**
     * An utility-object to fill the map following the chain pattern which may useful when initializing class fields.
     *
     * @param <K> type of map's key
     * @param <V> type of map's value
     */
    @ToString
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    final class SimpleMapFiller<K, V> implements MapFiller<K, V> {

        /**
         * Map being filled
         */
        @Getter private final @NonNull Map<K, V> map;

        /**
         * Puts the specified value by the specified key to the map.
         *
         * @param key key of the value to put
         * @param value value to put by the key
         * @return this map filler for chaining
         */
        @Override
        public final @NotNull MapFiller<K, V> put(final K key, final V value) {
            map.put(key, value);

            return this;
        }

        /**
         * Fills the map with the values of specified array.
         *
         * @param entries entries which will be put to the map
         * @return this map filler for chaining
         */
        @Override
        @SafeVarargs
        public final @NotNull MapFiller<K, V> fill(
                final @NotNull Pair<K, V> @NonNull ... entries
        ) {
            for (val entry : entries) map.put(entry.getFirst(), entry.getSecond());

            return this;
        }

        /**
         * Fills the map based on the specified {@link Iterator}.
         *
         * @param entries iterator of the entries whose elements will be put to the map
         * @return this map filler for chaining
         */
        @Override
        public final @NotNull MapFiller<K, V> fill(
                final @NonNull Iterator<@NotNull ? extends Pair<K, V>> entries
        ) {
            while (entries.hasNext()) {
                val entry = entries.next();
                map.put(entry.getFirst(), entry.getSecond());
            }

            return this;
        }

        /**
         * Fills the map with the values of specified {@link Iterable}.
         *
         * @param entries entries which will be put to the map
         * @return this map filler for chaining
         */
        @Override
        public final @NotNull MapFiller<K, V> fill(
                final @NonNull Iterable<@NotNull ? extends Pair<K, V>> entries
        ) {
            for (val entry : entries) map.put(entry.getFirst(), entry.getSecond());

            return this;
        }

        /**
         * Fills the map based on the specified {@link Stream}.
         *
         * @param entries stream of the entries whose elements will be put to the map
         * @return this map filler for chaining
         */
        @Override
        public final @NotNull MapFiller<K, V> fill(
                final @NonNull Stream<@NotNull ? extends Pair<K, V>> entries
        ) {
            entries.forEach(entry -> map.put(entry.getFirst(), entry.getSecond()));

            return this;
        }

        @Override
        public final @NotNull MapFiller<K, V> fillOrdered(
                final @NonNull Stream<@NotNull ? extends Pair<K, V>> entries
        ) {
            entries.forEachOrdered(entry -> map.put(entry.getFirst(), entry.getSecond()));

            return this;
        }
    }
}
