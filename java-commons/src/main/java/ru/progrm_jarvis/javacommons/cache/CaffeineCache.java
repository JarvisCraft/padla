package ru.progrm_jarvis.javacommons.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utilities for creation of <a href="https://github.com/ben-manes/caffeine">Caffeine</a>-based
 * {@link CacheFactory cache factories}.
 */
@UtilityClass
public class CaffeineCache {

    /**
     * Flag indicating whether {@link Caffeine} is available
     */
    private final @Nullable CacheFactory CACHE_FACTORY;

    static {
        CacheFactory cacheFactory;
        try { // check if Caffeine is available
            cacheFactory = new CaffeineCacheFactory(Caffeine::newBuilder);
        } catch (final Throwable ignored) {
            cacheFactory = null;
        }
        CACHE_FACTORY = cacheFactory;
    }

    /**
     * Creates a Caffeine Cache factory.
     *
     * @return created Caffeine Cache factory
     *
     * @throws IllegalStateException if Caffeine is not available
     */
    public @NotNull CacheFactory createFactory() {
        if (CACHE_FACTORY == null) throw new IllegalStateException("Caffeine Cache is not available");

        return CACHE_FACTORY;
    }

    /**
     * Attempts to create a Caffeine Cache factory.
     *
     * @return created Caffeine Cache factory or {@code null} if it is unavailable
     */
    public @Nullable CacheFactory tryCreateFactory() {
        return CACHE_FACTORY;
    }

    /**
     * {@link CacheFactory} backed by Caffeine Cache.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private final class CaffeineCacheFactory implements CacheFactory {

        /**
         * Factory used for creation of {@link Caffeine} builder
         */
        private @NotNull CaffeineBuilderFactory factory;

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
            return wrap(factory.newBuilder().weakKeys().build());
        }

        @Override
        public <K, V> @NotNull Cache<K, V> weakValuesCache() {
            return wrap(factory.newBuilder().weakValues().build());
        }

        @Override
        public <K, V> @NotNull Cache<K, V> softValuesCache() {
            return wrap(factory.newBuilder().softValues().build());
        }

        /**
         * Factory responsible for creation of {@link Caffeine} builder.
         */
        public interface CaffeineBuilderFactory {

            /**
             * Creates a {@link Caffeine} builder.
             *
             * @return Caffeine builder
             */
            Caffeine<Object, Object> newBuilder();
        }
    }
}
