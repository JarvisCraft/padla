package ru.progrm_jarvis.javacommons.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.util.BlackHole;

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
    private final @NotNull CacheFactory FACTORY;

    static {
        CacheFactory factory = null;

        caffeineCache: {
            try { // triggering java.lang.ClassNotFoundException
                BlackHole.consume(Caffeine.class);
            } catch (final Throwable ignored) {
                break caffeineCache;
            }
            factory = new CaffeineCacheFactory();
        }

        if (factory == null) factory = CacheFactory.never();

        FACTORY = factory;
    }

    /**
     * Creates a new cache whose keys will be stored {@link java.lang.ref.WeakReference weakly}.
     *
     * @param <K> type of cache keys
     * @param <V> type of cached values
     * @return weak keys cache
     */
    public <K, V> @NotNull Cache<K, V> weakKeysCache() {
        return FACTORY.weakKeysCache();
    }

    /**
     * Creates a new cache whose values will be stored {@link java.lang.ref.WeakReference weakly}.
     *
     * @param <K> type of cache keys
     * @param <V> type of cached values
     * @return weak values cache
     */
    public <K, V> @NotNull Cache<K, V> weakValuesCache() {
        return FACTORY.weakValuesCache();
    }

    /**
     * Creates a new cache whose values will be stored {@link java.lang.ref.WeakReference softly}.
     *
     * @param <K> type of cache keys
     * @param <V> type of cached values
     * @return soft values cache
     */
    public <K, V> @NotNull Cache<K, V> softValuesCache() {
        return FACTORY.softValuesCache();
    }

    /**
     * {@link Cache}-wrapper of <a href="https://github.com/ben-manes/caffeine">Caffeine</a> Cache.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class CaffeineCacheFactory implements CacheFactory {

        /**
         * Wraps the provided {@link com.github.benmanes.caffeine.cache.Cache Caffeine Cache} into {@link Cache}.
         *
         * @param caffeineCache Caffeine Cache to be wrapped
         * @param <K> type of cache keys
         * @param <V> type of cached values
         * @return wrapped Caffeine Cache
         */
        private static <K, V> @NotNull Cache<K, V> wrap(
                final @NotNull com.github.benmanes.caffeine.cache.Cache<K, V> caffeineCache
        ) {
            return caffeineCache::get;
        }

        @Override
        public <K, V> @NotNull Cache<K, V> weakKeysCache() {
            return wrap(Caffeine.newBuilder().weakKeys().build());
        }

        @Override
        public <K, V> @NotNull Cache<K, V> weakValuesCache() {
            return wrap(Caffeine.newBuilder().weakValues().build());
        }

        @Override
        public <K, V> @NotNull Cache<K, V> softValuesCache() {
            return wrap(Caffeine.newBuilder().softValues().build());
        }
    }
}
