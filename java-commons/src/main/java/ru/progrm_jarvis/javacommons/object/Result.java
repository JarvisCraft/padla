package ru.progrm_jarvis.javacommons.object;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A tagged union representing either a successful result or an error.
 *
 * @param <T> type of successful result
 * @param <E> type of error result
 */
public interface Result<T, E> {

    /* ************************************************* Factories ************************************************* */

    /**
     * Creates a new successful result.
     *
     * @param value value of the successful result
     * @param <T> type of successful result
     * @param <E> type of error result
     * @return created successful result
     */
    static <T, E> Result<T, E> success(final T value) {
        return new Success<>(value);
    }

    /**
     * Creates a new successful result with {@code null} value.
     *
     * @param <T> type of successful result
     * @param <E> type of error result
     * @return created successful result
     */
    @SuppressWarnings("unchecked")
    static <T, E> Result<@Nullable T, E> nullSuccess() {
        return (Result<T, E>) NullSuccess.INSTANCE;
    }

    /**
     * Creates a new error result.
     *
     * @param error value of the error result
     * @param <T> type of successful result
     * @param <E> type of error result
     * @return created error result
     */
    static <T, E> Result<T, E> error(final E error) {
        return new Error<>(error);
    }

    /**
     * Creates a new {@code void}-error result.
     *
     * @param <T> type of successful result
     * @param <E> type of error result
     * @return created error result
     */
    @SuppressWarnings("unchecked")
    static <T, E> Result<T, @Nullable E> nullError() {
        return (Result<T, E>) NullError.INSTANCE;
    }

    /**
     * Converts the given {@link Optional} into a {@link #nullError() null-error result}.
     *
     * @param optional optional to be converted into the result
     * @param <T> type of successful result
     * @param <E> type of error result
     * @return {@link #success(Object) successful result} if the value {@link Optional#isPresent()} in the optional
     * and a {@link #nullError() null-error result} otherwise
     *
     * @see #from(Optional, Supplier) alternative with customizable error value
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // convertion from optional itself
    static <T, E> Result<T, @Nullable E> from(final @NonNull Optional<T> optional) {
        return optional.<Result<T, E>>map(Result::success).orElseGet(Result::nullError);
    }

    /**
     * Converts the given {@link Optional} into an {@link #error(Object) error result}.
     *
     * @param optional optional to be converted into the result
     * @param errorSupplier supplier to create an error
     * if the given {@link Optional} {@link Optional#isEmpty() is empty}
     * @param <T> type of successful result
     * @param <E> type of error result
     * @return {@link #success(Object) successful result} if the value {@link Optional#isPresent()} in the optional
     * and an {@link #error(Object) error result} with an error supplied from {@code error supplier} otherwise
     *
     * @see #from(Optional) alternative with default (i.e. {@code null}) error
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // convertion from optional itself
    static <T, E> Result<T, E> from(final @NonNull Optional<T> optional, final @NonNull Supplier<E> errorSupplier) {
        return optional.<Result<T, E>>map(Result::success).orElseGet(() -> error(errorSupplier.get()));
    }

    /* ********************************************** Checking methods ********************************************** */

    /**
     * Checks is this result is successful.
     *
     * @return {@code true} if this is a successful result and {@code false} otherwise
     */
    boolean isSuccess();

    /**
     * Checks is this result is an error.
     *
     * @return {@code true} if this is an error result and {@code false} otherwise
     */
    boolean isError();

    /* ********************************************* Unwrapping methods ********************************************* */

    /**
     * Gets the value of this result throwing a {@link NotSuccessException}
     * if this is an {@link #isError() error result}.
     *
     * @return successful value of this result
     *
     * @throws NotSuccessException if this is an {@link #isError() error result}
     * @see #expect(String) analog with exception message specification
     * @see #orElseThrow(Supplier) analog with exception specification
     * @see #orElseSneakyThrow(Supplier) analog with unchecked exception specification
     */
    T unwrap();

    /**
     * Gets the value of this result throwing a {@link NotSuccessException}
     * if this is an {@link #isError() error result}.
     *
     * @param message message to be specified to {@link NotSuccessException}
     * @return successful value of this result
     *
     * @throws NotSuccessException if this is an {@link #isError() error result}
     * @see #unwrap() analog with default message
     * @see #orElseThrow(Supplier) analog with exception specification
     * @see #orElseSneakyThrow(Supplier) analog with unchecked exception specification
     */
    T expect(String message);

    /**
     * Gets the value of this result throwing {@code X} got by using the specified supplier
     * if this is an {@link #isError() error result}.
     *
     * @param exceptionSupplier supplier of a thrown exception
     * @return successful value of this result
     *
     * @throws X if this is an {@link #isError() error result}
     * @see #expect(String) {@link NotSuccessException} analog
     * @see #unwrap() default message {@link NotSuccessException} analog
     * @see #orElseSneakyThrow(Supplier) unchecked equivalent
     */
    <X extends Throwable> T orElseThrow(@NonNull Supplier<X> exceptionSupplier) throws X;

    /**
     * Gets the value of this result throwing {@code X} got by using the specified supplier
     * if this is an {@link #isError() error result}.
     * This differs from {@link #orElseThrow(Supplier)} as this does not declare {@code X} as a thrown exception.
     *
     * @param exceptionSupplier supplier of a thrown exception
     * @return successful value of this result
     *
     * @throws X if this is an {@link #isError() error result}
     * @see #expect(String) {@link NotSuccessException} analog
     * @see #unwrap() default message {@link NotSuccessException} analog
     * @see #orElseThrow(Supplier) checked equivalent
     */
    @SneakyThrows
    default <X extends Throwable> T orElseSneakyThrow(@NonNull Supplier<X> exceptionSupplier) {
        return orElseThrow(exceptionSupplier);
    }

    /**
     * Gets the value of this result if this is a {@link #isSuccess() successful}
     * otherwise returning default value.
     *
     * @param defaultValue default value to be returned if this is an {@link #isError()} error value}
     * @return successful value if this a {@link #isSuccess() successful result} or {@code defaultValue} otherwise
     */
    T or(T defaultValue);

    /**
     * Gets the value of this result if this is a {@link #isSuccess() successful}
     * otherwise returning the value got by using the specified supplier.
     *
     * @param defaultValueSupplier supplier of the default value
     * to be returned if this is an {@link #isError()} error value}
     * @return successful value if this a {@link #isSuccess() successful result} or {@code defaultValue} otherwise
     */
    T orGet(@NonNull Supplier<T> defaultValueSupplier);

    /**
     * Gets the error of this result throwing a {@link NotErrorException}
     * if this is a {@link #isSuccess() successful result}.
     *
     * @return error value of this result
     *
     * @throws NotErrorException if this is a {@link #isSuccess() successful result}
     * @see #expectError(String) analog with exception message specification
     * @see #errorOrElseThrow(Supplier) analog with exception specification
     * @see #errorOrElseSneakyThrow(Supplier) analog with unchecked exception specification
     */
    E error();

    /**
     * Gets the error of this result throwing a {@link NotErrorException}
     * if this is a {@link #isSuccess() successful result}.
     *
     * @param message message to be specified to {@link NotErrorException}
     * @return error value of this result
     *
     * @throws NotErrorException if this is a {@link #isSuccess() successful result}
     * @see #error() analog with default message
     * @see #errorOrElseThrow(Supplier) analog with exception specification
     * @see #errorOrElseSneakyThrow(Supplier) analog with unchecked exception specification
     */
    E expectError(String message);

    /**
     * Gets the error of this result throwing {@code X} got by using the specified supplier
     * if this is a {@link #isSuccess() successful result}.
     *
     * @param exceptionSupplier supplier of a thrown exception
     * @return error value of this result
     *
     * @throws X if this is an {@link #isError() error result}
     * @see #error() default message {@link NotErrorException} analog
     * @see #expectError(String) {@link NotErrorException} analog
     * @see #errorOrElseSneakyThrow(Supplier) unchecked equivalent
     */
    <X extends Throwable> E errorOrElseThrow(@NonNull Supplier<X> exceptionSupplier) throws X;

    /**
     * Gets the error of this result throwing {@code X} got by using the specified supplier
     * if this is a {@link #isSuccess() successful result}.
     * This differs from {@link #orElseThrow(Supplier)} as this does not declare {@code X} as a thrown exception.
     *
     * @param exceptionSupplier supplier of a thrown exception
     * @return error value of this result
     *
     * @throws X if this is an {@link #isError() error result}
     * @see #error() default message {@link NotErrorException} analog
     * @see #expectError(String) {@link NotErrorException} analog
     * @see #errorOrElseThrow(Supplier) checked equivalent
     */
    @SneakyThrows
    default <X extends Throwable> E errorOrElseSneakyThrow(@NonNull Supplier<X> exceptionSupplier) {
        return errorOrElseThrow(exceptionSupplier);
    }

    /* ********************************************** Mapping methods ********************************************** */

    /**
     * Maps the result if it is {@link #isSuccess() successful} returning a new result with the result of mapping
     * otherwise keeping the {@link #isError() error result}.
     *
     * @param mappingFunction function to map the successful result
     * @param <R> type of the resulting successful value
     * @return mapped successful result if it was a {@link #isSuccess() successful result}
     * or an error result if it was an {@link #isError() error result}
     */
    <R> Result<R, E> map(@NonNull Function<T, R> mappingFunction);

    /**
     * Returns the given result if this is a {@link #isSuccess() successful result}
     * otherwise keeping the {@link #isError() error result}.
     *
     * @param result result to be returned if this is a {@link #isSuccess() successful result}
     * @param <R> type of the resulting successful value
     * @return {@code result} if this is a {@link #isSuccess() successful result} and
     * or keeps the {@link #isError() error result}
     *
     * @see #flatMap(Function) lazy analog
     */
    <R> Result<R, E> and(@NonNull Result<R, E> result);

    /**
     * Flat-maps the result if it is {@link #isSuccess() successful} returning the result of mapping
     * otherwise keeping the {@link #isError() error result}.
     *
     * @param mappingFunction function to flat-map the successful result
     * @param <R> type of the resulting successful value
     * @return flat-mapped successful result if it was a {@link #isSuccess() successful result}
     * or an error result if this was an {@link #isError() error result}
     *
     * @see #and(Result) non-lazy analog
     */
    <R> Result<R, E> flatMap(@NonNull Function<T, Result<R, E>> mappingFunction);

    /**
     * Swaps this result making an {@link #error(Object) error result} from a {@link #isSuccess() successful result}
     * and a {@link #success(Object) successful result} from an {@link #isError() error result}.
     *
     * @return {@link #error(Object) error result} if this was a {@link #isSuccess() successful result}
     * and a {@link #success(Object) successful result} if this was an {@link #isError() error result}
     */
    Result<E, T> swap();

    /* ********************************************* Conversion methods ********************************************* */

    /**
     * Converts this result to an {@link Optional}.
     *
     * @return {@link Optional optional} containing the successful result's value
     * if this is a {@link #isSuccess() successful result}
     * and an {@link Optional#empty() empty optional} otherwise
     */
    @NotNull Optional<T> asOptional();

    /**
     * Converts this result to a {@link ValueContainer}.
     *
     * @return {@link ValueContainer value container} containing the successful result's value
     * if this is a {@link #isSuccess() successful result}
     * and an {@link ValueContainer#empty() empty value-container} otherwise
     */
    @NotNull ValueContainer<T> asValueContainer();

    /**
     * Representation of a {@link #isSuccess() successful} {@link Result result}.
     *
     * @param <T> type of successful result
     * @param <E> type of error result
     */
    @Value
    class Success<T, E> implements Result<T, E> {

        /**
         * Value wrapped by this result
         */
        T value;

        /**
         * Changes the error type of this result.
         *
         * @param <R> target error type
         * @return this result with changed error type
         */
        @SuppressWarnings("unchecked")
        private <R> Result<T, R> changeErrorType() {
            return (Result<T, R>) this;
        }

        //<editor-fold desc="Checking methods" defaultstate="collapsed">

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public boolean isError() {
            return false;
        }

        //</editor-fold>

        //<editor-fold desc="Unwrapping methods" defaultstate="collapsed">

        @Override
        public T unwrap() {
            return value;
        }

        @Override
        public T expect(final String message) {
            return value;
        }

        @Override
        public <X extends Throwable> T orElseThrow(final @NonNull Supplier<X> exceptionSupplier) {
            return value;
        }

        @Override
        public T or(final T defaultValue) {
            return value;
        }

        @Override
        public T orGet(final @NonNull Supplier<T> defaultValueSupplier) {
            return value;
        }

        @Override
        public E error() {
            throw new NotErrorException("This is not an error result");
        }

        @Override
        public E expectError(final String message) {
            throw new NotErrorException(message);
        }

        @Override
        public <X extends Throwable> E errorOrElseThrow(@NonNull final Supplier<X> exceptionSupplier) throws X {
            throw exceptionSupplier.get();
        }

        //</editor-fold>

        //<editor-fold desc="Mapping methods" defaultstate="collapsed">

        @Override
        public <R> Result<R, E> map(final @NonNull Function<T, R> mappingFunction) {
            return new Success<>(mappingFunction.apply(value));
        }

        @Override
        public <R> Result<R, E> and(final @NonNull Result<R, E> result) {
            return result;
        }

        @Override
        public <R> Result<R, E> flatMap(final @NonNull Function<T, Result<R, E>> mappingFunction) {
            return mappingFunction.apply(value);
        }

        @Override
        public Result<E, T> swap() {
            return new Error<>(value);
        }

        //</editor-fold>

        //<editor-fold desc="Conversion methods" defaultstate="collapsed">

        @Override
        public @NotNull Optional<T> asOptional() {
            return Optional.of(value);
        }

        @Override
        public @NotNull ValueContainer<T> asValueContainer() {
            return ValueContainer.of(value);
        }

        //</editor-fold>
    }

    /**
     * Representation of an {@link #isError() error} {@link Result result}.
     *
     * @param <T> type of successful result
     * @param <E> type of error result
     */
    @Value
    class Error<T, E> implements Result<T, E> {

        /**
         * Error wrapped by this result
         */
        E error;

        /**
         * Changes the success type of this result.
         *
         * @param <R> target success type
         * @return this result with changed success type
         */
        @SuppressWarnings("unchecked")
        private <R> Result<R, E> changeResultType() {
            return (Result<R, E>) this;
        }

        //<editor-fold desc="Checking methods" defaultstate="collapsed">

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public boolean isError() {
            return true;
        }

        //</editor-fold>

        //<editor-fold desc="Unwrapping methods" defaultstate="collapsed">

        @Override
        public T unwrap() {
            throw new NotSuccessException("This is not a success-result");
        }

        @Override
        public T expect(final String message) {
            throw new NotSuccessException(message);
        }

        @Override
        public <X extends Throwable> T orElseThrow(final @NonNull Supplier<X> exceptionSupplier) throws X {
            throw exceptionSupplier.get();
        }

        @Override
        public T or(final T defaultValue) {
            return defaultValue;
        }

        @Override
        public T orGet(final @NonNull Supplier<T> defaultValueSupplier) {
            return defaultValueSupplier.get();
        }

        @Override
        public E error() {
            return error;
        }

        @Override
        public E expectError(final String message) {
            return error;
        }

        @Override
        public <X extends Throwable> E errorOrElseThrow(@NonNull final Supplier<X> exceptionSupplier) {
            return error;
        }

        //</editor-fold>

        //<editor-fold desc="Mapping methods" defaultstate="collapsed">

        @Override
        public <R> Result<R, E> map(final @NonNull Function<T, R> mappingFunction) {
            return changeResultType();
        }

        @Override
        public <R> Result<R, E> and(final @NonNull Result<R, E> result) {
            return changeResultType();
        }

        @Override
        public <R> Result<R, E> flatMap(final @NonNull Function<T, Result<R, E>> mappingFunction) {
            return changeResultType();
        }

        @Override
        public Result<E, T> swap() {
            return new Success<>(error);
        }

        //</editor-fold>

        //<editor-fold desc="Conversion methods" defaultstate="collapsed">

        @Override
        public @NotNull Optional<T> asOptional() {
            return Optional.empty();
        }

        @Override
        public @NotNull ValueContainer<T> asValueContainer() {
            return ValueContainer.empty();
        }

        //</editor-fold>
    }

    /**
     * Holder of a {@code null}-success result.
     */
    final class NullSuccess {

        /**
         * Instance of a {@code null}-error
         */
        private static final Result<@Nullable ?, ?> INSTANCE = new Success<>(null);
    }

    /**
     * Holder of a {@code null}-error result.
     */
    final class NullError {

        /**
         * Instance of a {@code null}-error
         */
        private static final Result<?, @Nullable ?> INSTANCE = new Error<>(null);
    }

    /**
     * An exception thrown whenever {@link #unwrap()} is called on an error result.
     */
    @NoArgsConstructor
    class NotSuccessException extends RuntimeException {
        //<editor-fold desc="Inheriting constructors" defaultstate="collapsed">

        /**
         * Constructs a new exception with the specified message.
         *
         * @param message message describing the exception cause
         */
        public NotSuccessException(final String message) {
            super(message);
        }

        /**
         * Constructs a new exception with the specified message and cause.
         *
         * @param message message describing the exception cause
         * @param cause cause of this exception
         */
        public NotSuccessException(final String message, final Throwable cause) {
            super(message, cause);
        }

        /**
         * Constructs a new exception with the specified cause.
         *
         * @param cause cause of this exception
         */
        public NotSuccessException(final Throwable cause) {
            super(cause);
        }

        /**
         * Constructs a new exception with the specified message and cause.
         *
         * @param message message describing the exception cause
         * @param cause cause of this exception
         * @param enableSuppression flag indicating whether or not suppression
         * is enabled or disabled for this exception
         * @param writableStackTrace flag indicating whether or not not the stack trace
         * should be writable for this exception
         */
        public NotSuccessException(final String message, final Throwable cause, final boolean enableSuppression,
                                   final boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }

        //</editor-fold>
    }

    /**
     * An exception thrown whenever {@link #error()} is called on a successful result.
     */
    @NoArgsConstructor
    class NotErrorException extends RuntimeException {
        //<editor-fold desc="Inheriting constructors" defaultstate="collapsed">

        /**
         * Constructs a new exception with the specified message.
         *
         * @param message message describing the exception cause
         */
        public NotErrorException(final String message) {
            super(message);
        }

        /**
         * Constructs a new exception with the specified message and cause.
         *
         * @param message message describing the exception cause
         * @param cause cause of this exception
         */
        public NotErrorException(final String message, final Throwable cause) {
            super(message, cause);
        }

        /**
         * Constructs a new exception with the specified cause.
         *
         * @param cause cause of this exception
         */
        public NotErrorException(final Throwable cause) {
            super(cause);
        }

        /**
         * Constructs a new exception with the specified message and cause.
         *
         * @param message message describing the exception cause
         * @param cause cause of this exception
         * @param enableSuppression flag indicating whether or not suppression
         * is enabled or disabled for this exception
         * @param writableStackTrace flag indicating whether or not not the stack trace
         * should be writable for this exception
         */
        public NotErrorException(final String message, final Throwable cause, final boolean enableSuppression,
                                 final boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }

        //</editor-fold>
    }
}
