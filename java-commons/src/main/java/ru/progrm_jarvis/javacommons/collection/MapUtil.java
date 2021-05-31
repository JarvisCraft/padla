package ru.progrm_jarvis.javacommons.collection;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import ru.progrm_jarvis.javacommons.object.Pair;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Utilities related to {@link Map}.
 */
@UtilityClass
public class MapUtil {

    /**
     * Fills the map not performing any checks.
     *
     * @param map map to fill
     * @param keyValuePairs key-value pairs to put to the map ordered as <i>key1, value1, key2, value2...</i>
     */
    @SuppressWarnings("unchecked")
    private void fillMapNoChecks(@SuppressWarnings("rawtypes") final @NonNull Map map,
                                 final @NonNull Object... keyValuePairs) {
        @SuppressWarnings("BooleanVariableAlwaysNegated") var value = true; // will get reverted for the first value

        Object key = null; // requires to be initialized for some reason :)
        for (final Object keyValuePair : keyValuePairs) if (value = !value) map.put(key, keyValuePair);
        else key = keyValuePair;
    }

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
    public <M extends Map<?, ?>> M fillMap(final @NonNull M map, final @NonNull Object... keyValuePairs) {
        val length = keyValuePairs.length;
        if (length == 0) return map;
        if (length % 2 != 0) throw new IllegalArgumentException(
                "Key-Value pairs array should have an even number of elements"
        );

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
    public <K, V, M extends Map<K, V>> M fillMap(final @NonNull M map, final K firstValueKey, final V firstValue,
                                                 final @NonNull Object... keyValuePairs) {
        val length = keyValuePairs.length;
        if(length % 2 != 0) throw new IllegalArgumentException(
                "Key-Value pairs array should have an even number of elements"
        );

        map.put(firstValueKey, firstValue);
        fillMapNoChecks(map, keyValuePairs);

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
    @SafeVarargs
    public <K, V, M extends Map<K, V>> M fillMap(final @NonNull M map, final @NonNull Pair<K, V>... entries) {
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
    public <K, V, M extends Map<K, V>> M fillMap(final @NonNull M map,
                                                 final @NonNull Iterator<? extends Pair<K, V>> entries) {
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
    public <K, V, M extends Map<K, V>> M fillMap(final @NonNull M map,
                                                 final @NonNull Iterable<? extends Pair<K, V>> entries) {
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
    public <K, V, M extends Map<K, V>> M fillMap(final @NonNull M map,
                                                 final @NonNull Stream<? extends Pair<K, V>> entries) {
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
    public <K, V, M extends Map<K, V>> M fillMapOrdered(final @NonNull M map,
                                                        final @NonNull Stream<? extends Pair<K, V>> entries) {
        entries.forEachOrdered(entry -> map.put(entry.getFirst(), entry.getSecond()));

        return map;
    }

    public <K, V> V getOrDefault(final @NonNull Map<K, V> map, final K key, final Supplier<V> defaultValueSupplier) {
        // the value is got from map, non-null value is surely a present one, but null may have different meanings
        val value = map.get(key);
        return value == null ? map.containsKey(key) ? null : defaultValueSupplier.get() : value;
    }

    public <K, V, R> R getOrDefault(final @NonNull Map<K, V> map, final K key, final Function<V, R> valueTransformer,
                                    final R defaultValue) {
        // the value is got from map, non-null value is surely a present one, but null may have different meanings
        val value = map.get(key);
        return value == null
                ? map.containsKey(key) ? valueTransformer.apply(null) : defaultValue
                : valueTransformer.apply(value);
    }

    public <K, V, R> R getOrDefault(final @NonNull Map<K, V> map, final K key, final Function<V, R> valueTransformer,
                                    final Supplier<R> defaultValueSupplier) {
        // the value is got from map, non-null value is surely a present one, but null may have different meanings
        val value = map.get(key);
        return value == null
                ? map.containsKey(key) ? valueTransformer.apply(null) : defaultValueSupplier.get()
                : valueTransformer.apply(value);
    }
}
