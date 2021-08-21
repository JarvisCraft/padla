package ru.progrm_jarvis.javacommons.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.javacommons.util.BlackHole;

/**
 * Utilities for creation of <a href="https://github.com/ben-manes/caffeine">Caffeine</a>-based
 * {@link CacheFactory cache factories}.
 */
@UtilityClass
public class CaffeineCache {

    /**
     * Flag indicating whether {@link Caffeine} is available
     */
    private static final boolean AVAILABLE;

    static {
        boolean available = true;
        try { // check if Caffeine class is available
            BlackHole.consume(Caffeine.class);
        } catch (final Throwable ignored) {
            available = false;
        }
        AVAILABLE = available;
    }

    /**
     * Creates a Caffeine Cache factory.
     *
     * @return created Caffeine Cache factory
     *
     * @throws IllegalStateException if Caffeine is not available
     */
    public static @NotNull CacheFactory createFactory() {
        if (AVAILABLE) return new CaffeineCacheFactory();

        throw new IllegalStateException("Caffeine Cache is not available");
    }

    /**
     * Attempts to create a Caffeine Cache factory.
     *
     * @return created Caffeine Cache factory or {@code null} if it is unavailable
     */
    public static @Nullable CacheFactory tryCreateFactory() {
        return AVAILABLE ? new CaffeineCacheFactory() : null;
    }

    /**
     * {@link CacheFactory} backed by Caffeine Cache.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class CaffeineCacheFactory implements CacheFactory {

        /**
         * Singleton instance of this {@link CacheFactory} implementation
         */
        private static final @NotNull CacheFactory INSTANCE = new CaffeineCacheFactory();

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
