package ru.progrm_jarvis.javacommons.util.valuestorage;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
public final class SimpleValueStorage<V> extends AbstractValueStorage<String, V> {

    /**
     * Counter used for generating new keys
     */
    @NonNull AtomicReference<BigInteger> counter;

    private SimpleValueStorage(final @NotNull Map<String, V> values,
                               final @NotNull AtomicReference<BigInteger> counter) {
        super(values);
        this.counter = counter;
    }

    /**
     * Creates a new simple value storage.
     */
    public static <V> @NotNull SimpleValueStorage<V> create() {
        return new SimpleValueStorage<>(new ConcurrentHashMap<>(), new AtomicReference<>(BigInteger.ZERO));
    }

    @Override
    protected String generateNewKey() {
        return counter.updateAndGet(value -> value.add(BigInteger.ONE)).toString(Character.MAX_RADIX);
    }
}
