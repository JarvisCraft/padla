package ru.progrm_jarvis.javacommons.object;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utilities for common object operations.
 */
@UtilityClass
public class ObjectUtil {

    /**
     * Empty array of objects.
     */
    public final @NotNull Object @NotNull @Unmodifiable [] EMPTY_ARRAY = new Object[0];

    /**
     * Returns the first nonnull value of specified variants or {@code null} if none found.
     *
     * @param variants variants which may be nonnull
     * @param <T> type of value
     * @return first nonnull value found or {@code null} if none
     */
    @SafeVarargs
    public <T> @Nullable T nonNull(final @Nullable T @NonNull ... variants) {
        for (val variant : variants) if (variant != null) return variant;

        return null;
    }

    /**
     * Returns the first nonnull value of specified variants or {@code null} if none found.
     *
     * @param variants variant suppliers whose values may be null
     * @param <T> type of value
     * @return first nonnull value found or {@code null} if none
     */
    @SafeVarargs
    public <T> @Nullable T nonNull(final Supplier<@Nullable T> @NonNull ... variants) {
        for (val variant : variants) {
            final T value;
            if ((value = variant.get()) != null) return value;
        }

        return null;
    }

    /**
     * Returns the first nonnull value of specified variants wrapped in {@link Optional} or empty if none found.
     *
     * @param variants variants which may be nonnull
     * @param <T> type of value
     * @return {@link Optional} containing first nonnull value found or empty if none
     */
    @SafeVarargs
    public <T> @NotNull Optional<T> optionalNonNull(final @Nullable T @NonNull ... variants) {
        for (val variant : variants) if (variant != null) return Optional.of(variant);

        return Optional.empty();
    }

    /**
     * Returns the first nonnull value of specified variants wrapped in {@link Optional} or empty if none found.
     *
     * @param variants variant suppliers whose values may be null
     * @param <T> type of value
     * @return {@link Optional} containing first nonnull value found or empty if none
     */
    @SafeVarargs
    public <T> @NotNull Optional<T> optionalNonNull(final @NotNull Supplier<@Nullable T> @NonNull ... variants) {
        for (val variant : variants) {
            final T value;
            if ((value = variant.get()) != null) return Optional.of(value);
        }

        return Optional.empty();
    }

    /**
     * Returns the first nonnull value of specified variants or throws {@link NullPointerException} if none found.
     *
     * @param variants variants which may be nonnull
     * @param <T> type of value
     * @return first nonnull value found
     *
     * @throws NullPointerException if none of the variants specified is nonnull
     */
    @SafeVarargs
    public <T> @NotNull T nonNullOrThrow(final @Nullable T @NonNull ... variants) {
        for (val variant : variants) if (variant != null) return variant;

        throw new NullPointerException("No nonnull value found among variants");
    }

    /**
     * Returns the first nonnull value of specified variants or throws {@link NullPointerException} if none found.
     *
     * @param variants variant suppliers whose values may be null
     * @param <T> type of value
     * @return first nonnull value found
     *
     * @throws NullPointerException if none of the variants specified is nonnull
     */
    @SafeVarargs
    public <T> @NotNull T nonNullOrThrow(final @NotNull Supplier<@Nullable T> @NonNull ... variants) {
        for (val variant : variants) {
            final T value;
            if ((value = variant.get()) != null) return value;
        }

        throw new NullPointerException("No nonnull value found among variants");
    }

    /**
     * Returns the first nonnull value of specified variants or throws {@link NullPointerException} if none found.
     *
     * @param nullHandler handler to be used to supply the value in case of all variants being {@code null}
     * @param variants variants which may be nonnull
     * @param <T> type of value
     * @return first nonnull value found
     *
     * @throws NullPointerException if none of the variants specified is nonnull
     */
    @SafeVarargs
    public <T> T nonNullOr(final @NonNull Supplier<T> nullHandler,
                           final @Nullable T @NonNull ... variants) {
        for (val variant : variants) if (variant != null) return variant;

        return nullHandler.get();
    }

    /**
     * Returns the first nonnull value of specified variants or throws {@link NullPointerException} if none found.
     *
     * @param nullHandler handler to be used to supply the value in case of all variants being {@code null}
     * @param variants variant suppliers whose values may be null
     * @param <T> type of value
     * @return first nonnull value found
     *
     * @throws NullPointerException if none of the variants specified is nonnull
     */
    @SafeVarargs
    public <T> T nonNullOr(final @NonNull Supplier<T> nullHandler,
                           final @NotNull Supplier<@Nullable T> @NonNull ... variants) {
        for (val variant : variants) {
            final T value;
            if ((value = variant.get()) != null) return value;
        }

        return nullHandler.get();
    }

    /**
     * Maps (transforms) the value specified using the mapping function.
     * This may come in handy in case of initializing fields with expressions which have checked exceptions.
     *
     * @param value value to map
     * @param mappingFunction function to map the value to the required type
     * @param <T> type of source value
     * @param <R> type of resulting value
     * @return mapped (transformed) value
     */
    public <T, R> R map(final T value, final @NonNull Function<T, R> mappingFunction) {
        return mappingFunction.apply(value);
    }

    /**
     * Returns the first nonnull value of specified variants or {@code null} if none found
     * mapped using function specified.
     *
     * @param mappingFunction function to map the value to the required type
     * @param variants variants which may be nonnull
     * @param <T> type of source value
     * @param <R> type of resulting value
     * @return first nonnull value found or {@code null} if none found mapped using mapping function
     */
    @SafeVarargs
    public <T, R> R mapNonNull(final @NonNull Function<@Nullable T, R> mappingFunction,
                               final @Nullable T @NonNull ... variants) {
        for (val variant : variants) if (variant != null) return mappingFunction.apply(variant);

        return mappingFunction.apply(null);
    }

    /**
     * Returns the first nonnull value of specified variants or {@code null} if none found
     * mapped using function specified.
     *
     * @param mappingFunction function to map the value to the required type
     * @param variants variant suppliers whose values may be null
     * @param <T> type of source value
     * @param <R> type of resulting value
     * @return first nonnull value found or {@code null} if none found mapped using mapping function
     */
    @SafeVarargs
    public <T, R> R mapNonNull(final @NonNull Function<@Nullable T, R> mappingFunction,
                               final @NotNull Supplier<@Nullable T> @NonNull ... variants) {
        for (val variant : variants) {
            final T value;
            if ((value = variant.get()) != null) return mappingFunction.apply(value);
        }

        return mappingFunction.apply(null);
    }

    /**
     * Returns the first nonnull value of specified variants mapped using function specified
     * or {@code null} if none found.
     *
     * @param mappingFunction function to map the value to the required type
     * @param variants variants which may be nonnull
     * @param <T> type of source value
     * @param <R> type of resulting value
     * @return first nonnull value found mapped using mapping function or {@code null} if none found
     */
    @SafeVarargs
    public <T, R> @Nullable R mapOnlyNonNull(final @NonNull Function<@NotNull T, R> mappingFunction,
                                             final @Nullable T @NonNull ... variants) {
        for (val variant : variants) if (variant != null) return mappingFunction.apply(variant);

        return null;
    }

    /**
     * Returns the first nonnull value of specified variants mapped using function specified
     * or {@code null} if none found.
     *
     * @param mappingFunction function to map the value to the required type
     * @param variants variant suppliers whose values may be null
     * @param <T> type of source value
     * @param <R> type of resulting value
     * @return first nonnull value found mapped using mapping function or {@code null} if none found
     */
    @SafeVarargs
    public <T, R> @Nullable R mapOnlyNonNull(final @NonNull Function<@NotNull T, R> mappingFunction,
                                             final @NotNull Supplier<@Nullable T> @NonNull ... variants) {
        for (val variant : variants) {
            final T value;
            if ((value = variant.get()) != null) return mappingFunction.apply(value);
        }

        return null;
    }

    /**
     * Returns the first nonnull value of specified variants mapped using function specified
     * or throws {@link NullPointerException} if none found.
     *
     * @param mappingFunction function to map the value to the required type
     * @param variants variants which may be nonnull
     * @param <T> type of source value
     * @param <R> type of resulting value
     * @return first nonnull value found mapped using mapping function
     *
     * @throws NullPointerException if none of the variants specified is nonnull
     */
    @SafeVarargs
    public <T, R> @NotNull R mapNonNullOrThrow(final @NonNull Function<@NotNull T, R> mappingFunction,
                                               final @Nullable T @NonNull ... variants) {
        for (val variant : variants) if (variant != null) return mappingFunction.apply(variant);

        throw new NullPointerException("No nonnull value found among variants");
    }

    /**
     * Returns the first nonnull value of specified variants mapped using function specified
     * or throws {@link NullPointerException} if none found.
     *
     * @param mappingFunction function to map the value to the required type
     * @param variants variant suppliers whose values may be null
     * @param <T> type of source value
     * @param <R> type of resulting value
     * @return first nonnull value found mapped using mapping function
     *
     * @throws NullPointerException if none of the variants specified is nonnull
     */
    @SafeVarargs
    public <T, R> @NotNull R mapNonNullOrThrow(final @NonNull Function<@NotNull T, R> mappingFunction,
                                               final @NotNull Supplier<@Nullable T> @NonNull ... variants) {
        for (val variant : variants) {
            final T value;
            if ((value = variant.get()) != null) return mappingFunction.apply(value);
        }

        throw new NullPointerException("No nonnull value found among variants");
    }
}
