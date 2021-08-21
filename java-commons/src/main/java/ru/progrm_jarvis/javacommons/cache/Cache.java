package ru.progrm_jarvis.javacommons.cache;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Simple cache interface.
 * {@link #never() No-op implementation} (the one which never performs any caching) is considered correct.
 *
 * @param <K> type of cache keys
 * @param <V> type of cached values
 *
 * @apiNote currently <b>java-commons</b> does not implement its own functional cache,
 * it simply provides a universal interface and wrappers for known implementations
 */
@FunctionalInterface
public interface Cache<K, V> {

    /**
     * Gets the value from the cache computing it on demand using the provided function.
     *
     * @param key key by which ti get the value from cache
     * @param mappingFunction function used to create the value if there is no cached one
     * @return the current value associated with the specified key
     *
     * @throws NullPointerException if {@code key} is {@code null}
     * @throws NullPointerException if {@code mappingFunction} is {@code null}
     */
    V get(@NonNull K key, @NonNull Function<? super K, ? extends V> mappingFunction);

    /**
     * Creates a cache which actually never performs caching.
     *
     * @param <K> type of cache keys
     * @param <V> type of cached values
     * @return cache which actually never performs caching
     */
    static @NotNull <K, V> Cache<K, V> never() {
        return (key, mappingFunction) -> mappingFunction.apply(key);
    }
}
