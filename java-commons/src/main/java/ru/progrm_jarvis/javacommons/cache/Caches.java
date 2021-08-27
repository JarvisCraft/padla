package ru.progrm_jarvis.javacommons.cache;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * Utility for creating commonly used caches depending on runtime capabilities.
 * If no cache is available then {@link Cache#never() no-op cache} is used.
 */
@UtilityClass
public class Caches {

    /**
     * Factory used for creation of {@link Cache caches}.
     * This will try to be the best implementation available
     * but will fallback to {@link CacheFactory#never() no-op} if none is available.
     */
    private final @NotNull CacheFactory DEFAULT_FACTORY;

    static {
        final CacheFactory factory;
        DEFAULT_FACTORY = (factory = CaffeineCache.tryCreateFactory()) == null ? CacheFactory.never() : factory;
    }

    /**
     * Gets the default cache factory.
     *
     * @return default cache factory
     */
    public @NotNull CacheFactory defaultFactory() {
        return DEFAULT_FACTORY;
    }

    /**
     * Creates a new cache whose keys will be stored {@link java.lang.ref.WeakReference weakly}.
     *
     * @param <K> type of cache keys
     * @param <V> type of cached values
     * @return weak keys cache
     */
    public <K, V> @NotNull Cache<K, V> weakKeysCache() {
        return DEFAULT_FACTORY.weakKeysCache();
    }

    /**
     * Creates a new cache whose values will be stored {@link java.lang.ref.WeakReference weakly}.
     *
     * @param <K> type of cache keys
     * @param <V> type of cached values
     * @return weak values cache
     */
    public <K, V> @NotNull Cache<K, V> weakValuesCache() {
        return DEFAULT_FACTORY.weakValuesCache();
    }

    /**
     * Creates a new cache whose values will be stored {@link java.lang.ref.WeakReference softly}.
     *
     * @param <K> type of cache keys
     * @param <V> type of cached values
     * @return soft values cache
     */
    public <K, V> @NotNull Cache<K, V> softValuesCache() {
        return DEFAULT_FACTORY.softValuesCache();
    }
}
