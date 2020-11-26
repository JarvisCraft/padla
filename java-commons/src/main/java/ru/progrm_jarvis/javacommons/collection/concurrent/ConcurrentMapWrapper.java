package ru.progrm_jarvis.javacommons.collection.concurrent;


import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class ConcurrentMapWrapper<K, V, W extends Map<K, V>>
        extends AbstractConcurrentSizedCollectionWrapper<W> implements Map<K, V> {

    protected ConcurrentMapWrapper(final @NotNull W wrapped,
                                   final @NotNull Lock readLock,
                                   final @NotNull Lock writeLock) {
        super(wrapped, readLock, writeLock);
    }

    public static <K, V> @NotNull Map<K, V> create(final @NonNull Map<K, V> wrapped) {
        final ReadWriteLock lock;

        return new ConcurrentMapWrapper<>(
                wrapped, (lock = new ReentrantReadWriteLock()).readLock(), lock.writeLock()
        );
    }

    @Override
    protected int internalSize() {
        return wrapped.size();
    }

    @Override
    protected boolean internalIsEmpty() {
        return wrapped.isEmpty();
    }

    @Override
    protected void internalClear() {
        wrapped.clear();
    }

    @Override
    public boolean containsKey(final Object key) {
        readLock.lock();
        try {
            return wrapped.containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean containsValue(final Object value) {
        readLock.lock();
        try {
            return wrapped.containsValue(value);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public V get(final Object key) {
        readLock.lock();
        try {
            return wrapped.get(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public V put(final K key, final V value) {
        writeLock.lock();
        try {
            return wrapped.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V remove(final Object key) {
        writeLock.lock();
        try {
            return wrapped.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        writeLock.lock();
        try {
            return wrapped.remove(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void putAll(final @NonNull Map<? extends K, ? extends V> elements) {
        writeLock.lock();
        try {
            wrapped.putAll(elements);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public@Nonnull
     Set<K> keySet() {
        readLock.lock();
        try {
            return wrapped.keySet();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public @NotNull Collection<V> values() {
        readLock.lock();
        try {
            return wrapped.values();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public@Nonnull
     Set<Entry<K, V>> entrySet() {
        readLock.lock();
        try {
            return wrapped.entrySet();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public V getOrDefault(final Object key, final V defaultValue) {
        readLock.lock();
        try {
            return wrapped.getOrDefault(key, defaultValue);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void forEach(final @NonNull BiConsumer<? super K, ? super V> action) {
        readLock.lock();
        try {
            wrapped.forEach(action);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void replaceAll(final @NonNull BiFunction<? super K, ? super V, ? extends V> function) {
        writeLock.lock();
        try {
            wrapped.replaceAll(function);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V putIfAbsent(final K key, final V value) {
        writeLock.lock();
        try {
            return wrapped.putIfAbsent(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        writeLock.lock();
        try {
            return wrapped.replace(key, oldValue, newValue);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V replace(final K key, final V value) {
        writeLock.lock();
        try {
            return wrapped.replace(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V computeIfAbsent(final K key, final @NonNull Function<? super K, ? extends V> mappingFunction) {
        writeLock.lock();
        try {
            return wrapped.computeIfAbsent(key, mappingFunction);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V computeIfPresent(final K key,
                              final @NonNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        writeLock.lock();
        try {
            return wrapped.computeIfPresent(key, remappingFunction);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V compute(final K key, final @NonNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        writeLock.lock();
        try {
            return wrapped.compute(key, remappingFunction);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V merge(final K key, final @NonNull V value,
                   final @NonNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        writeLock.lock();
        try {
            return wrapped.merge(key, value, remappingFunction);
        } finally {
            writeLock.unlock();
        }
    }
}
