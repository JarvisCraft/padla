package ru.progrm_jarvis.javacommons.delegate;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
    public <T> @NotNull T createWrapper(final @NonNull Class<T> targetType, final @NonNull Supplier<T> supplier) {
        return CachingGeneratingDelegateFactory.<T>uncheckedDelegateWrapperFactoryCast(
                factories.get(targetType, actualTargetType -> {
                    // validation happens only on creation of the wrapper
                    DelegateFactory.verifyTargetType(actualTargetType);

                    return createFactory(actualTargetType);
                })
        ).create(supplier);
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

    /**
     * Casts the given delegate wrapper factory into the specific one.
     *
     * @param type raw-typed delegate wrapper factory
     * @param <T> exact wanted type of delegate wrapper factory
     * @return the provided delegate wrapper factory with its type case to the specific one
     *
     * @apiNote this is effectively no-op
     */
    // note: no nullability annotations are present on parameter and return type as cast of `null` is also safe
    @Contract("_ -> param1")
    @SuppressWarnings("unchecked")
    private static <T> DelegateWrapperFactory<T> uncheckedDelegateWrapperFactoryCast(
            final DelegateWrapperFactory<?> type
    ) {
        return (DelegateWrapperFactory<T>) type;
    }
}
