package ru.progrm_jarvis.javacommons.object.valuestorage;

import lombok.NonNull;

/**
 * Storage of values which is made for sharing values which is unavailable via direct standard APIs
 * such as initializing static final fields of generated classes.
 *
 * @param <K> type of keys used for identifying values
 * @param <V> type of values stored
 */
public interface ValueStorage<K, V> {

    /**
     * Stores the given non-null value returning its unique key.
     *
     * @param value non-null value to store
     * @return unique key by which the stored value may be retrieved
     */
    K storeValue(@NonNull V value);

    /**
     * Retrieves (gets and removes) the value stored by the given key.
     *
     * @param key unique key by which the value should be found
     * @return value which was stored by the given key or {@code null} if there was no value stored
     */
    V retrieveValue(K key);
}
