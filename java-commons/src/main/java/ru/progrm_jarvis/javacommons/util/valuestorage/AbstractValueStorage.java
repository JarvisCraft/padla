package ru.progrm_jarvis.javacommons.util.valuestorage;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

/**
 * Abstract base for {@link ValueStorage}.
 *
 * @param <K> type of keys used for identifying values
 * @param <V> type of values stored
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AbstractValueStorage<K, V> implements ValueStorage<K, V> {

    /**
     * Values stored by their unique keys
     */
    @NonNull Map<K, V> values;

    /**
     * Generates a new key for storing the unique value.
     *
     * @return newly generated unique key
     */
    protected abstract K generateNewKey();

    @Override
    public K storeValue(final @NonNull V value) {
        val key = generateNewKey();
        values.put(key, value);

        return key;
    }

    @Override
    public V retrieveValue(final K key) {
        return values.get(key);
    }
}
