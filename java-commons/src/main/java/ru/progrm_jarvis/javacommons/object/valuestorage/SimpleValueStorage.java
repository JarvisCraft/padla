package ru.progrm_jarvis.javacommons.object.valuestorage;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Simple {@link ValueStorage value storage} based on {@link AbstractValueStorage}
 * using {@link String strings} as its keys.
 *
 * @param <V> type of values stored
 */
@ThreadSafe
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class SimpleValueStorage<V> extends AbstractValueStorage<@NotNull String, V> {

    /**
     * Counter used for generating new keys
     */
    @NotNull AtomicReference<@NotNull BigInteger> counter;

    private SimpleValueStorage(final @NotNull Map<@NotNull String, V> values,
                               final @NotNull AtomicReference<@NotNull BigInteger> counter) {
        super(values);
        this.counter = counter;
    }

    /**
     * Creates a new simple value storage.
     */
    public static <V> @NotNull ValueStorage<@NotNull String, V> create() {
        return new SimpleValueStorage<>(new ConcurrentHashMap<>(), new AtomicReference<>(BigInteger.ZERO));
    }

    @Override
    protected String generateNewKey() {
        return counter.updateAndGet(value -> value.add(BigInteger.ONE)).toString(Character.MAX_RADIX);
    }
}
