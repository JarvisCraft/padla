package ru.progrm_jarvis.javacommons.cache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Factory used for creation of generic {@link Cache caches}.
 */
public interface CacheFactory {

    /**
     * Creates a new cache whose keys will be stored {@link java.lang.ref.WeakReference weakly}.
     *
     * @param <K> type of cache keys
     * @param <V> type of cached values
     * @return weak keys cache
     */
    <K, V> @NotNull Cache<K, V> weakKeysCache();

    /**
     * Creates a new cache whose values will be stored {@link java.lang.ref.WeakReference weakly}.
     *
     * @param <K> type of cache keys
     * @param <V> type of cached values
     * @return weak values cache
     */
    <K, V> @NotNull Cache<K, V> weakValuesCache();

    /**
     * Creates a new cache whose values will be stored {@link java.lang.ref.SoftReference softly}.
     *
     * @param <K> type of cache keys
     * @param <V> type of cached values
     * @return soft values cache
     */
    <K, V> @NotNull Cache<K, V> softValuesCache();

    /**
     * Creates a cache factory which always creates {@link Cache#never() no-op caches}.
     *
     * @return cache factory which always supplies {@link Cache#never() no-op caches}
     */
    static @NotNull CacheFactory never() {
        return NeverCacheFactory.INSTANCE;
    }

    /**
     * {@link CacheFactory} which always creates {@link Cache#never() no-op caches}.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class NeverCacheFactory implements CacheFactory {

        /**
         * Singleton instance of this cache factory.
         */
        private static final @NotNull CacheFactory INSTANCE = new NeverCacheFactory();

        @Override
        public @NotNull <K, V> Cache<K, V> weakKeysCache() {
            return Cache.never();
        }

        @Override
        public <K, V> @NotNull Cache<K, V> weakValuesCache() {
            return Cache.never();
        }

        @Override
        public @NotNull <K, V> Cache<K, V> softValuesCache() {
            return Cache.never();
        }
    }
}
