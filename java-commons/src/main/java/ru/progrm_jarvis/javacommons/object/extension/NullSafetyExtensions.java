package ru.progrm_jarvis.javacommons.object.extension;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Extensions to provide null-safety on nullable types.
 * These methods behave specifically, for example call on {@code null} may not throw {@link NullPointerException}
 * thus they have underscores ({@code _}) in their names.
 */
@UtilityClass
public class NullSafetyExtensions {

    /**
     * Returns the passed value if it not {@code null} otherwise returning the default one.
     *
     * @param value value whose nullability is tested
     * @param defaultValue value returned if {@code value} is {@code null}
     * @param <T> type of the value
     * @return {@code value} if it is not {@code null} and {@code defaultValue} otherwise
     */
    @Contract("!null, _ -> param1; null, _ -> param2")
    public <T> T _or(final @Nullable T value,
                     final @Nullable T defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * Returns the passed value if it not {@code null} otherwise returning the default one.
     *
     * @param value value whose nullability is tested
     * @param defaultValueSupplier supplier of the value returned if {@code value} is {@code null}
     * @param <T> type of the value
     * @return {@code value} if it is not {@code null} and {@code defaultValue} otherwise
     *
     * @throws NullPointerException if {@code defaultValueSupplier} is {@code null}
     */
    @SuppressWarnings("Contract") // Lombok's annotation is treated incorrectly
    @Contract("_, null -> fail; !null, _ -> param1; null, _ -> _")
    public <T> T _orGet(final @Nullable T value,
                        final @NonNull Supplier<@Nullable ? extends T> defaultValueSupplier) {
        return value == null ? defaultValueSupplier.get() : value;
    }

    /**
     * Maps the given value if it is {@code null} otherwise returning {@code null}.
     *
     * @param value value to be mapped if it is not {@code null}
     * @param mappingFunction function to be used for mapping of the the non-{@code null} value
     * @param <T> type of the source value
     * @param <R> type of the mapped value
     * @return mapped {@code value} if the latter was not {@code null} or {@code null} otherwise
     *
     * @throws NullPointerException if {@code mappingFunction} is {@code null}
     */
    @SuppressWarnings("Contract") // Lombok's annotation is treated incorrectly
    @Contract("_, null -> fail; !null, _ -> _; null, _ -> null")
    public <T, R> @Nullable R _map(final @Nullable T value,
                                   final @NonNull Function<@NotNull T, R> mappingFunction) {
        return value == null ? null : mappingFunction.apply(value);
    }

    /**
     * Maps the given value.
     *
     * @param value value to be mapped
     * @param mappingFunction function to be used for mapping of the value
     * @param <T> type of the source value
     * @param <R> type of the mapped value
     * @return mapped value
     *
     * @throws NullPointerException if {@code mappingFunction} is {@code null}
     */
    @SuppressWarnings("Contract") // Lombok's annotation is treated incorrectly
    @Contract("_, null -> fail; _, _ -> _")
    public <T, R> @Nullable R _mapNullable(final @Nullable T value,
                                           final @NonNull Function<@Nullable T, R> mappingFunction) {
        return mappingFunction.apply(value);
    }

    /**
     * Returns the passed value if it is not {@code null} and matches the predicate and {@code null} otherwise.
     *
     * @param value tested value
     * @param predicate condition to test the value agains
     * @param <T> type of the value
     * @return {@code value} if it is not {@code null} and matches the predicate and {@code null} otherwise.
     *
     * @throws NullPointerException if {@code predicate} is {@code null}
     */
    @SuppressWarnings("Contract") // Lombok's annotation is treated incorrectly
    @Contract("_, null -> fail; !null, _ -> _; null, _ -> null")
    public <T> @Nullable T _filter(final @Nullable T value,
                                          final @NonNull Predicate<T> predicate) {
        return value == null ? null : predicate.test(value) ? value : null;
    }

    /**
     * Returns the passed value if it matches the predicate and {@code null} otherwise.
     *
     * @param value tested value
     * @param predicate condition to test the value agains
     * @param <T> type of the value
     * @return {@code value} if it matches the predicate and {@code null} otherwise.
     *
     * @throws NullPointerException if {@code predicate} is {@code null}
     */
    @SuppressWarnings("Contract") // Lombok's annotation is treated incorrectly
    @Contract("_, null -> fail; _, _ -> _")
    public <T> @Nullable T _filterNullable(final @Nullable T value, final @NonNull Predicate<@Nullable T> predicate) {
        return predicate.test(value) ? value : null;
    }

    /**
     * Throws the supplied exception if the given value is null.
     *
     * @param value checked value
     * @param throwableSupplier supplier of a throwable used when the value is {@code null}
     * @param <T> type of the value
     * @param <X> thrown typethrowable
     * @return {@code value} if it is not {@code null}
     * @throws X if {@code value} is null
     * @throws NullPointerException if {@code throwableSupplier} or it supplies {@code null}
     */
    public <T, X extends Throwable> @NotNull T _orElseThrow(final @Nullable T value,
                                                            final @NonNull Supplier<@NonNull X>
                                                                    throwableSupplier) throws X {
        if (value == null) throw throwableSupplier.get();

        return value;
    }

    /**
     * Converts the given value into a {@link Stream stream} containing the value
     * if is not {@code null} or an {@link Stream#empty() empty stream} otherwise.
     *
     * @param value value to be converted into a stream if it is not {@code null}
     * @param <T> type of the value
     * @return {@link Stream stream} containing the value if it is not {@code null}
     * or an {@link Stream#empty() empty stream} otherwise.
     */
    public <T> @NotNull Stream<@NotNull T> _stream(final @Nullable T value) {
        return value == null ? Stream.empty() : Stream.of(value);
    }

    /**
     * Converts the given value into a {@link Stream stream}.
     *
     * @param value value to be converted into a stream if it is not {@code null}
     * @param <T> type of the value
     * @return {@link Stream stream} containing the value.
     */
    public <T> @NotNull Stream<@Nullable T> _streamOfNullable(final @Nullable T value) {
        return Stream.of(value);
    }

    /**
     * Converts the value into an {@link Optional optional}
     * returning {@link Optional#empty()} if the value is {@code null}.
     *
     * @param value value to be converted into an optional
     * @param <T> type of the value
     * @return value wrapped into an {@link Optional}
     */
    public <T> @NotNull Optional<T> _toOptional(final @Nullable T value) {
        return Optional.ofNullable(value);
    }
}
