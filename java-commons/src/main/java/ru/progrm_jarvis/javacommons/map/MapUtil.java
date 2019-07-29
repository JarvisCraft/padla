package ru.progrm_jarvis.javacommons.map;

import com.google.common.base.Preconditions;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import ru.progrm_jarvis.javacommons.pair.Pair;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Utilities related to {@link Map >}.
 */
@UtilityClass
public class MapUtil {

    /**
     * Fills the map specified with the values specified.
     *
     * @param map map to fill with the values
     * @param keyValuePairs pairs of keys and values in order <i>key1, value1, key2, value2, key3, value3...</i>
     * @param <M> map type
     * @return the map passed filled with key-value pairs specified
     * @throws IllegalArgumentException if {@code keyValuePairs}'s length is odd
     *
     * @see #fillMap(Map, Object, Object, Object...)
     */
    public <M extends Map<?, ?>> M fillMap(@NonNull final M map, @NonNull final Object... keyValuePairs) {
        val length = keyValuePairs.length;
        if (length == 0) return map;
        Preconditions.checkArgument(length % 2 == 0, "Key-Value pairs array should have an even number of elements");

        fillMapNoChecks(map, keyValuePairs);

        return map;
    }

    /**
     * Fills the map specified with the values specified.
     *
     * @param map map to fill with the values
     * @param firstValueKey the key of first value
     * @param firstValue first value to be put to the map
     * @param keyValuePairs pairs of keys and values in order <i>key1, value1, key2, value2, key3, value3...</i>
     * @param <K> type of keys
     * @param <V> type of values
     * @param <M> map type
     * @return the map passed filled with key-value pairs specified
     * @throws IllegalArgumentException if {@code keyValuePairs}'s length is odd
     *
     * @see #fillMap(Map, Object...)
     */
    public <K, V, M extends Map<K, V>> M fillMap(@NonNull final M map, final K firstValueKey, final V firstValue,
                                                 @NonNull final Object... keyValuePairs) {
        val length = keyValuePairs.length;
        Preconditions.checkArgument(length % 2 == 0, "Key-Value pairs array should have an even number of elements");

        map.put(firstValueKey, firstValue);
        fillMapNoChecks(map, keyValuePairs);

        return map;
    }

    /**
     * Fills the map not performing any checks.
     *
     * @param map map to fill
     * @param keyValuePairs key-value pairs to put to the map ordered as <i>key1, value1, key2, value2...</i>
     */
    @SuppressWarnings("unchecked")
    private void fillMapNoChecks(@NonNull final Map map, @NonNull final Object... keyValuePairs) {
        var value = true; // will get reverted for the first value

        Object key = null; // requires to be initialized for some reason :)
        for (final Object keyValuePair : keyValuePairs) if (value = !value) map.put(key, keyValuePair);
        else key = keyValuePair;
    }

    /**
     * Fills the map specified with the values specified.
     *
     * @param map map to fill with the values
     * @param entries entries to fill the map with
     * @param <K> type of keys
     * @param <V> type of values
     * @param <M> map type
     * @return the map passed filled with key-value pairs specified
     */
    @SafeVarargs
    public <K, V, M extends Map<K, V>> M fillMap(@NonNull final M map, @NonNull final Pair<K, V>... entries) {
        for (val entry : entries) map.put(entry.getFirst(), entry.getSecond());

        return map;
    }

    /**
     * Fills the map specified with the values specified.
     *
     * @param map map to fill with the values
     * @param entries entries to fill the map with
     * @param <K> type of keys
     * @param <V> type of values
     * @param <M> map type
     * @return the map passed filled with key-value pairs specified
     */
    public <K, V, M extends Map<K, V>> M fillMap(@NonNull final M map,
                                                 @NonNull final Iterator<? extends Pair<K, V>> entries) {
        while (entries.hasNext()) {
            val entry = entries.next();
            map.put(entry.getFirst(), entry.getSecond());
        }

        return map;
    }

    /**
     * Fills the map specified with the values specified.
     *
     * @param map map to fill with the values
     * @param entries entries to fill the map with
     * @param <K> type of keys
     * @param <V> type of values
     * @param <M> map type
     * @return the map passed filled with key-value pairs specified
     */
    public <K, V, M extends Map<K, V>> M fillMap(@NonNull final M map,
                                                 @NonNull final Iterable<? extends Pair<K, V>> entries) {
        return fillMap(map, entries.iterator());
    }

    /**
     * Fills the map specified with the values specified.
     *
     * @param map map to fill with the values
     * @param entries entries to fill the map with
     * @param <K> type of keys
     * @param <V> type of values
     * @param <M> map type
     * @return the map passed filled with key-value pairs specified
     */
    public <K, V, M extends Map<K, V>> M fillMap(@NonNull final M map,
                                                 @NonNull final Stream<? extends Pair<K, V>> entries) {
        entries.forEach(entry -> map.put(entry.getFirst(), entry.getSecond()));

        return map;
    }

    /**
     * Fills the map specified with the values specified keeping order.
     *
     * @param map map to fill with the values
     * @param entries entries to fill the map with
     * @param <K> type of keys
     * @param <V> type of values
     * @param <M> map type
     * @return the map passed filled with key-value pairs specified
     */
    public <K, V, M extends Map<K, V>> M fillMapOrdered(@NonNull final M map,
                                                        @NonNull final Stream<? extends Pair<K, V>> entries) {
        entries.forEachOrdered(entry -> map.put(entry.getFirst(), entry.getSecond()));

        return map;
    }

    /**
     * Creates new {@link MapFiller} from the map specified.
     *
     * @param map map for which to create the filler
     * @param <K> type of keys
     * @param <V> type of values
     * @return map filler created for the specified map
     *
     * @see MapFiller
     * @see #mapFiller(Map, Object, Object)
     */
    public <K, V> MapFiller<K, V> mapFiller(@NonNull final Map<K, V> map) {
        return new MapFiller<>(map);
    }

    /**
     * Creates new {@link MapFiller} from the map specified initialized with the value specified.
     *
     * @param map map for which to create the filler
     * @param firstKey the key of the first value
     * @param firstValue the first value to be put to the map
     * @param <K> type of keys
     * @param <V> type of values
     * @return map filler created for the specified map with initial value put
     *
     * @see MapFiller
     * @see #mapFiller(Map)
     */
    public <K, V> MapFiller<K, V> mapFiller(@NonNull final Map<K, V> map, K firstKey, final V firstValue) {
        return new MapFiller<>(map)
                .put(firstKey, firstValue);
    }

    public <K, V> V getOrDefault(@NonNull final Map<K, V> map, final K key, final Supplier<V> defaultValueSupplier) {
        // the value is got from map, non-null value is surely a present one, but null may have different meanings
        val value = map.get(key);
        return value == null ? map.containsKey(key) ? null : defaultValueSupplier.get() : value;
    }

    public <K, V, R> R getOrDefault(@NonNull final Map<K, V> map, final K key, final Function<V, R> valueTransformer,
                                    final R defaultValue) {
        // the value is got from map, non-null value is surely a present one, but null may have different meanings
        val value = map.get(key);
        return value == null
                ? map.containsKey(key) ? valueTransformer.apply(null) : defaultValue
                : valueTransformer.apply(value);
    }

    public <K, V, R> R getOrDefault(@NonNull final Map<K, V> map, final K key, final Function<V, R> valueTransformer,
                                    final Supplier<R> defaultValueSupplier) {
        // the value is got from map, non-null value is surely a present one, but null may have different meanings
        val value = map.get(key);
        return value == null
                ? map.containsKey(key) ? valueTransformer.apply(null) : defaultValueSupplier.get()
                : valueTransformer.apply(value);
    }

    /**
     * An utility-object to fill the map following the chain pattern which may useful when initializing class fields.
     *
     * @param <K> type of map's key
     * @param <V> type of map's value
     */
    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor
    @Accessors(fluent = true)
    public static final class MapFiller<K, V> {

        /**
         * Map being filled
         */
        @NonNull @Getter private final Map<K, V> map;

        /**
         * Puts the specified value by the specified key to the map.
         *
         * @param key key of the value to put
         * @param value value to put by the key
         * @return this map filler for chaining
         */
        public final MapFiller<K, V> put(final K key, final V value) {
            map.put(key, value);

            return this;
        }

        /**
         * Fills the map with the values of specified array.
         *
         * @param entries entries which will be put to the map
         * @return this map filler for chaining
         */
        @SafeVarargs
        public final MapFiller<K, V> fill(final Pair<K, V>... entries) {
            for (val entry : entries) map.put(entry.getFirst(), entry.getSecond());

            return this;
        }

        /**
         * Fills the map based on the specified {@link Iterator}.
         *
         * @param entries iterator of the entries whose elements will be put to the map
         * @return this map filler for chaining
         */
        public final MapFiller<K, V> fill(final Iterator<? extends Pair<K, V>> entries) {
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
        public final MapFiller<K, V> fill(final Iterable<? extends Pair<K, V>> entries) {
            for (val entry : entries) map.put(entry.getFirst(), entry.getSecond());

            return this;
        }

        /**
         * Fills the map based on the specified {@link Stream}.
         *
         * @param entries stream of the entries whose elements will be put to the map
         * @return this map filler for chaining
         */
        public final MapFiller<K, V> fill(final Stream<? extends Pair<K, V>> entries) {
            entries.forEach(entry -> map.put(entry.getFirst(), entry.getSecond()));

            return this;
        }

        /**
         * Fills the map based on the specified {@link Stream} keeping order.
         *
         * @param entries stream of the entries whose elements will be put to the map
         * @return this map filler for chaining
         */
        public final MapFiller<K, V> fillOrdered(final Stream<? extends Pair<K, V>> entries) {
            entries.forEachOrdered(entry -> map.put(entry.getFirst(), entry.getSecond()));

            return this;
        }
    }
}
