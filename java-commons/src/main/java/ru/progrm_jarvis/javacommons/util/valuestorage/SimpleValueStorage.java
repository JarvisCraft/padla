package ru.progrm_jarvis.javacommons.util.valuestorage;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Simple {@link ValueStorage value storage} based on {@link AbstractValueStorage}
 * using {@link String strings} as its keys.
 *
 * @param <V> type of values stored
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class SimpleValueStorage<V> extends AbstractValueStorage<String, V> {

    /**
     * Counter used for generating new keys
     */
    @NonNull AtomicReference<BigInteger> counter = new AtomicReference<>(BigInteger.ZERO);

    /**
     * Creates a new concurrent {@link SimpleValueStorage simple value storage}.
     */
    public SimpleValueStorage() {
        super(new ConcurrentHashMap<>());
    }

    @Override
    protected String generateNewKey() {
        return counter.updateAndGet(value -> value.add(BigInteger.ONE)).toString(Character.MAX_RADIX);
    }
}
