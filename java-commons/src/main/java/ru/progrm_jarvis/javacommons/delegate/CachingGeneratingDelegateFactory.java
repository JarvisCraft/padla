package ru.progrm_jarvis.javacommons.delegate;

import com.google.common.cache.Cache;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * Base for implementing {@link DelegateFactory delegate factory}
 * using internal {@link DelegateWrapperFactory} cached for each unique type.
 */
@RequiredArgsConstructor
public abstract class CachingGeneratingDelegateFactory implements DelegateFactory {

    /**
     * Cache of {@link DelegateWrapperFactory delegate-wrapper factories} by wrapped types
     */
    protected final @NonNull Cache<Class<?>, DelegateWrapperFactory<?>> factories;

    /**
     * Creates a {@link DelegateWrapperFactory delegate-wrapper factory} fot the given type.
     *
     * @param targetType type of the wrapper to be created by the factory
     * @param <T> created wrapper's type
     * @return delegate-wrapper factory for the given type
     */
    protected abstract <T> @NotNull DelegateWrapperFactory<T> createFactory(@NotNull Class<T> targetType);

    @Override
    @SneakyThrows(ExecutionException.class)
    public <T> @NotNull T createWrapper(final @NonNull Class<T> targetType, final @NonNull Supplier<T> supplier) {
        //noinspection unchecked
        return ((DelegateWrapperFactory<T>) factories.get(targetType, () -> {
            // validation happens only on creation of the wrapper
            DelegateFactory.verifyTargetType(targetType);

            return createFactory(targetType);
        })).create(supplier);
    }

    /**
     * Delegate-wrapper factory responsible for creating delegate wrappers based on the specified supplier.
     *
     * @param <T> type of the wrapper which this factory produces
     */
    @FunctionalInterface
    protected interface DelegateWrapperFactory<T> {

        /**
         * Creates a new delegate-wrapper based on the given supplier.
         *
         * @param supplier supplier used to create the object
         * as specified by {@link DelegateFactory#createWrapper(Class, Supplier)}
         * @return delegate wrapper created for the given supplier
         */
        @NotNull T create(@NotNull Supplier<T> supplier);
    }
}
