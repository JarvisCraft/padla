package ru.progrm_jarvis.javacommons.collection.concurrent;


import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class ConcurrentMapWrapper<K, V, T extends Map<K, V>>
        extends ConcurrentWrapper<T> implements Map<K, V> {

    public ConcurrentMapWrapper(@NonNull final T wrapped) {
        super(wrapped);
    }

    @Override
    public int size() {
        readLock.lock();
        try {
            return wrapped.size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        readLock.lock();
        try {
            return wrapped.isEmpty();
        } finally {
            readLock.unlock();
        }
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
    public void putAll(@NonNull final Map<? extends K, ? extends V> m) {
        writeLock.lock();
        try {
            wrapped.putAll(m);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            wrapped.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    @Nonnull
    public Set<K> keySet() {
        readLock.lock();
        try {
            return wrapped.keySet();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    @Nonnull public Collection<V> values() {
        readLock.lock();
        try {
            return wrapped.values();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    @Nonnull  public Set<Entry<K, V>> entrySet() {
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
    public void forEach(@NonNull final BiConsumer<? super K, ? super V> action) {
        readLock.lock();
        try {
            wrapped.forEach(action);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void replaceAll(@NonNull final BiFunction<? super K, ? super V, ? extends V> function) {
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
    public boolean remove(final Object key, final Object value) {
        writeLock.lock();
        try {
            return wrapped.remove(key, value);
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
    public V computeIfAbsent(final K key, @NonNull final Function<? super K, ? extends V> mappingFunction) {
        writeLock.lock();
        try {
            return wrapped.computeIfAbsent(key, mappingFunction);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V computeIfPresent(final K key,
                              @NonNull final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        writeLock.lock();
        try {
            return wrapped.computeIfPresent(key, remappingFunction);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V compute(final K key, @NonNull final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        writeLock.lock();
        try {
            return wrapped.compute(key, remappingFunction);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V merge(final K key, @NonNull final V value,
                   @NonNull final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        writeLock.lock();
        try {
            return wrapped.merge(key, value, remappingFunction);
        } finally {
            writeLock.unlock();
        }
    }
}
