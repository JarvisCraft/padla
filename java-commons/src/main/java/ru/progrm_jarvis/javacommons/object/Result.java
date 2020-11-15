package ru.progrm_jarvis.javacommons.object;

import lombok.*;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A tagged union representing either a successful result or an error.
 *
 * @param <T> type of successful result
 * @param <E> type of error result
 */
@SuppressWarnings("PublicInnerClass")
public interface Result<T, E> extends Supplier<T> {

    /* ************************************************* Factories ************************************************* */

    /**
     * Creates a new successful result.
     *
     * @param value value of the successful result
     * @param <T> type of successful result
     * @param <E> type of error result
     * @return created successful result
     */
    static <T, E> @NotNull Result<T, E> success(final T value) {
        return value == null ? nullSuccess() : new Success<>(value);
    }

    /**
     * Creates a new successful result with {@code null} value.
     *
     * @param <T> type of successful result
     * @param <E> type of error result
     * @return created successful result
     */
    @SuppressWarnings("unchecked")
    static <T, E> @NotNull Result<@Nullable T, E> nullSuccess() {
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
    static <T, E> @NotNull Result<T, E> error(final E error) {
        return error == null ? nullError() : new Error<>(error);
    }

    /**
     * Creates a new {@code void}-error result.
     *
     * @param <T> type of successful result
     * @param <E> type of error result
     * @return created error result
     */
    @SuppressWarnings("unchecked")
    static <T, E> @NotNull Result<T, @Nullable E> nullError() {
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
    static <T, E> @NotNull Result<T, @Nullable E> from(final @NonNull Optional<T> optional) {
        return optional.<Result<T, E>>map(Result::success).orElseGet(Result::nullError);
    }

    /**
     * Converts the given {@link Optional} into an {@link #error(Object) error result}.
     *
     * @param optional optional to be converted into the result
     * @param errorSupplier supplier to create an error
     * if the given {@link Optional} is {@link Optional#empty() empty}
     * @param <T> type of successful result
     * @param <E> type of error result
     * @return {@link #success(Object) successful result} if the value {@link Optional#isPresent()} in the optional
     * and an {@link #error(Object) error result} with an error supplied from {@code error supplier} otherwise
     *
     * @see #from(Optional) alternative with default (i.e. null) error
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // convertion from optional itself
    static <T, E> @NotNull Result<T, E> from(final @NonNull Optional<T> optional,
                                             final @NonNull Supplier<E> errorSupplier) {
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

    @Override
    default T get() {
        return unwrap();
    }

    /**
     * Gets the value of this result throwing a {@link NotSuccessException}
     * if this is an {@link #isError() error result}.
     *
     * @return successful value of this result
     *
     * @throws NotSuccessException if this is an {@link #isError() error result}
     * @see #expect(String) analog with exception message specification
     * @see #orElseThrow(Function) analog with exception specification
     * @see #orElseSneakyThrow(Function) analog with unchecked exception specification
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
     * @see #orElseThrow(Function) analog with exception specification
     * @see #orElseSneakyThrow(Function) analog with unchecked exception specification
     */
    T expect(@NonNull String message);

    /**
     * Gets the value of this result throwing {@code X} got by using the specified supplier
     * if this is an {@link #isError() error result}.
     *
     * @param exceptionFactory factory of a thrown exception consuming the error value of this result
     * @param <X> type of exception thrown if this is an {@link #isError() error result}
     * @return successful value of this result
     *
     * @throws X if this is an {@link #isError() error result}
     * @see #expect(String) {@link NotSuccessException} analog
     * @see #unwrap() default message {@link NotSuccessException} analog
     * @see #orElseSneakyThrow(Function) unchecked equivalent
     */
    <X extends Throwable> T orElseThrow(@NonNull Function<E, X> exceptionFactory) throws X;

    /**
     * Gets the value of this result throwing {@code X} got by using the specified supplier
     * if this is an {@link #isError() error result}. Throws {@code X} if this is an {@link #isError() error result}.
     * This differs from {@link #orElseThrow(Function)} as this does not declare {@code X} as a thrown exception.
     *
     * @param exceptionFactory factory of a thrown exception consuming the error value of this result
     * @param <X> type of exception thrown if this is an {@link #isError() error result}
     * @return successful value of this result
     *
     * @see #expect(String) {@link NotSuccessException} analog
     * @see #unwrap() default message {@link NotSuccessException} analog
     * @see #orElseThrow(Function) checked equivalent
     */
    @SneakyThrows
    default <X extends Throwable> T orElseSneakyThrow(final @NonNull Function<E, X> exceptionFactory) {
        return orElseThrow(exceptionFactory);
    }

    /**
     * Gets the value of this result if this is a {@link #isSuccess() successful}
     * otherwise returning default value.
     *
     * @param defaultValue default value to be returned if this is an {@link #isError()} error value}
     * @return successful value if this a {@link #isSuccess() successful result} or {@code defaultValue} otherwise
     */
    T orDefault(T defaultValue);

    /**
     * Gets the value of this result if this is a {@link #isSuccess() successful}
     * otherwise returning the value got by using the specified supplier.
     *
     * @param defaultValueSupplier supplier of the default value
     * to be returned if this is an {@link #isError()} error value}
     * @return successful value if this a {@link #isSuccess() successful result} or {@code defaultValue} otherwise
     */
    T orGetDefault(@NonNull Supplier<T> defaultValueSupplier);

    /**
     * Gets the error of this result throwing a {@link NotErrorException}
     * if this is a {@link #isSuccess() successful result}.
     *
     * @return error value of this result
     *
     * @throws NotErrorException if this is a {@link #isSuccess() successful result}
     * @see #expectError(String) analog with exception message specification
     * @see #errorOrElseThrow(Function) analog with exception specification
     * @see #errorOrElseSneakyThrow(Function) analog with unchecked exception specification
     */
    E unwrapError();

    /**
     * Gets the error of this result throwing a {@link NotErrorException}
     * if this is a {@link #isSuccess() successful result}.
     *
     * @param message message to be specified to {@link NotErrorException}
     * @return error value of this result
     *
     * @throws NotErrorException if this is a {@link #isSuccess() successful result}
     * @see #unwrapError() analog with default message
     * @see #errorOrElseThrow(Function) analog with exception specification
     * @see #errorOrElseSneakyThrow(Function) analog with unchecked exception specification
     */
    E expectError(String message);

    /**
     * Gets the error of this result throwing {@code X} got by using the specified supplier
     * if this is a {@link #isSuccess() successful result}.
     *
     * @param exceptionFactory factory of a thrown exception consuming the successful value of this result
     * @param <X> type of exception thrown if this is a {@link #isSuccess() successful result}
     * @return error value of this result
     *
     * @throws X if this is an {@link #isError() error result}
     * @see #unwrapError() default message {@link NotErrorException} analog
     * @see #expectError(String) {@link NotErrorException} analog
     * @see #errorOrElseSneakyThrow(Function) unchecked equivalent
     */
    <X extends Throwable> E errorOrElseThrow(@NonNull Function<T, X> exceptionFactory) throws X;

    /**
     * Gets the error of this result throwing {@code X} got by using the specified supplier
     * if this is a {@link #isSuccess() successful result}. Throws {@code X} if this is an {@link #isError() error result}.
     * This differs from {@link #orElseThrow(Function)} as this does not declare {@code X} as a thrown exception.
     *
     * @param exceptionFactory factory of a thrown exception consuming the successful value of this result
     * @param <X> type of exception thrown if this is a {@link #isSuccess() successful result}
     * @return error value of this result
     *
     * @see #unwrapError() default message {@link NotErrorException} analog
     * @see #expectError(String) {@link NotErrorException} analog
     * @see #errorOrElseThrow(Function) checked equivalent
     */
    @SneakyThrows
    default <X extends Throwable> E errorOrElseSneakyThrow(final @NonNull Function<T, X> exceptionFactory) {
        return errorOrElseThrow(exceptionFactory);
    }

    /**
     * Invokes the given function if this result is a {@link #isSuccess() successful result}.
     *
     * @param successConsumer consumer accepting the {@link T successful value}
     */
    void ifSuccess(@NonNull Consumer<T> successConsumer);

    /**
     * Invokes the given function if this result is an {@link #isError() error result}.
     *
     * @param errorConsumer consumer accepting the {@link E error value}
     */
    void ifError(@NonNull Consumer<E> errorConsumer);

    /**
     * Invokes the corresponding function depending on this result's type.
     *
     * @param successConsumer consumer accepting the {@link T successful value}
     * @param errorConsumer consumer accepting the {@link E error value}
     */
    void handle(@NonNull Consumer<T> successConsumer, @NonNull Consumer<E> errorConsumer);

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
    <R> @NotNull Result<R, E> map(@NonNull Function<T, R> mappingFunction);

    /**
     * Returns the given result if this is a {@link #isSuccess() successful result}
     * otherwise keeping the {@link #isError() error result}.
     *
     * @param nextResult result to be returned if this is a {@link #isSuccess() successful result}
     * @param <R> type of the resulting successful value
     * @return {@code nextResult} if this is a {@link #isSuccess() successful result} or this error result otherwise
     *
     * @see #flatMap(Function) lazy analog
     */
    <R> @NotNull Result<R, E> and(@NonNull Result<R, E> nextResult);

    /**
     * Also known as {@code andThen}. Maps the result if this is a {@link #isSuccess() successful result}
     * returning the result of mapping otherwise keeping the {@link #isError() error result}.
     *
     * @param <R> type of the resulting successful value
     * @param mapper function to create a new result from {@link #unwrap() current successful one}
     * @return mapped {@link #unwrap() successful result} if this was {@link #isSuccess() the one}
     * or this error result otherwise
     *
     * @see #and(Result) non-lazy analog
     */
    <R> @NotNull Result<R, E> flatMap(@NonNull Function<T, @NotNull Result<R, E>> mapper);

    /**
     * Returns this result if this is a {@link #isSuccess() successful result}
     * otherwise returning the given result.
     *
     * @param alternateResult result to be returned if this is an {@link #isError() error result}
     * @param <R> type of the resulting error value
     * @return successful result if this is {@link #isSuccess() the one} or {@code alternateResult} otherwise
     *
     * @see #orElse(Function) lazy analog
     */
    <R> @NotNull Result<T, R> or(@NonNull Result<T, R> alternateResult);

    /**
     * Maps the result if this is an {@link #isError() error result}
     * returning the result of mapping otherwise keeping the {@link #isSuccess() successful result}.
     *
     * @param mapper function to create a new result from {@link #unwrapError() current error one}
     * @param <R> type of the resulting error value
     * @return mapped {@link #unwrapError() error result} if this was {@link #isError() the one}
     * or this successful result otherwise
     *
     * @see #or(Result) non-lazy analog
     */
    <R> @NotNull Result<T, R> orElse(@NonNull Function<E, @NotNull Result<T, R>> mapper);

    /**
     * Swaps this result making an {@link #error(Object) error result} from a {@link #isSuccess() successful result}
     * and a {@link #success(Object) successful result} from an {@link #isError() error result}.
     *
     * @return {@link #error(Object) error result} if this was a {@link #isSuccess() successful result}
     * and a {@link #success(Object) successful result} if this was an {@link #isError() error result}
     */
    @NotNull Result<E, T> swap();

    /* ********************************************* Conversion methods ********************************************* */

    /**
     * Converts this result to an {@link Optional} of its successful value.
     *
     * @return {@link Optional optional} containing the successful result's value
     * if this is a {@link #isSuccess() successful result}
     * and an {@link Optional#empty() empty optional} otherwise
     */
    @NotNull Optional<T> asOptional();

    /**
     * Converts this result to an {@link Optional} of its error.
     *
     * @return {@link Optional optional} containing the error result's error
     * if this is an {@link #isError() error result}
     * and an {@link Optional#empty() empty optional} otherwise
     */
    @NotNull Optional<E> asErrorOptional();

    /**
     * Converts this result to a {@link ValueContainer} of its successful value.
     *
     * @return {@link ValueContainer value container} containing the successful result's value
     * if this is a {@link #isSuccess() successful result}
     * or an {@link ValueContainer#empty() empty value-container} otherwise
     */
    @NotNull ValueContainer<T> asValueContainer();

    /**
     * Converts this result to a {@link ValueContainer} of its error value.
     *
     * @return {@link ValueContainer value container} containing the error result's error
     * if this is an {@link #isError() error result}
     * or an {@link ValueContainer#empty() empty value-container} otherwise
     */
    @NotNull ValueContainer<E> asErrorValueContainer();

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
        public T expect(final @NonNull String message) {
            return value;
        }

        @Override
        public <X extends Throwable> T orElseThrow(final @NonNull Function<E, X> exceptionFactory) {
            return value;
        }

        @Override
        public T orDefault(final T defaultValue) {
            return value;
        }

        @Override
        public T orGetDefault(final @NonNull Supplier<T> defaultValueSupplier) {
            return value;
        }

        @Override
        public E unwrapError() {
            throw new NotErrorException("This is not an error result");
        }

        @Override
        public E expectError(final String message) {
            throw new NotErrorException(message);
        }

        @Override
        public <X extends Throwable> E errorOrElseThrow(final @NonNull Function<T, X> exceptionSupplier) throws X {
            throw exceptionSupplier.apply(value);
        }

        @Override
        public void ifSuccess(final @NonNull Consumer<T> successConsumer) {
            successConsumer.accept(value);
        }

        @Override
        public void ifError(final @NonNull Consumer<E> errorConsumer) {}

        @Override
        public void handle(final @NonNull Consumer<T> successConsumer, final @NonNull Consumer<E> errorConsumer) {
            successConsumer.accept(value);
        }

        //</editor-fold>

        //<editor-fold desc="Mapping methods" defaultstate="collapsed">

        @Override
        public <R> @NotNull Result<R, E> map(final @NonNull Function<T, R> mappingFunction) {
            return success(mappingFunction.apply(value));
        }

        @Override
        public <R> @NotNull Result<R, E> and(final @NonNull Result<R, E> nextResult) {
            return nextResult;
        }

        @Override
        public <R> @NotNull Result<R, E> flatMap(final @NonNull Function<T, @NotNull Result<R, E>> mapper) {
            return mapper.apply(value);
        }

        @Override
        public @NotNull <R> Result<T, R> or(final @NonNull Result<T, R> alternateResult) {
            return changeErrorType();
        }

        @Override
        public @NotNull <R> Result<T, R> orElse(final @NonNull Function<E, @NotNull Result<T, R>> mapper) {
            return changeErrorType();
        }

        @Override
        public @NotNull Result<E, T> swap() {
            return error(value);
        }

        //</editor-fold>

        //<editor-fold desc="Conversion methods" defaultstate="collapsed">

        @Override
        public @NotNull Optional<T> asOptional() {
            return Optional.of(value);
        }

        @Override
        public @NotNull Optional<E> asErrorOptional() {
            return Optional.empty();
        }

        @Override
        public @NotNull ValueContainer<T> asValueContainer() {
            return ValueContainer.of(value);
        }

        @Override
        public @NotNull ValueContainer<E> asErrorValueContainer() {
            return ValueContainer.empty();
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
        public T expect(final @NonNull String message) {
            throw new NotSuccessException(message);
        }

        @Override
        public <X extends Throwable> T orElseThrow(final @NonNull Function<E, X> exceptionFactory) throws X {
            throw exceptionFactory.apply(error);
        }

        @Override
        public T orDefault(final T defaultValue) {
            return defaultValue;
        }

        @Override
        public T orGetDefault(final @NonNull Supplier<T> defaultValueSupplier) {
            return defaultValueSupplier.get();
        }

        @Override
        public E unwrapError() {
            return error;
        }

        @Override
        public E expectError(final String message) {
            return error;
        }

        @Override
        public <X extends Throwable> E errorOrElseThrow(final @NonNull Function<T, X> exceptionFactory) {
            return error;
        }

        @Override
        public void ifSuccess(final @NonNull Consumer<T> successConsumer) {}

        @Override
        public void ifError(final @NonNull Consumer<E> errorConsumer) {
            errorConsumer.accept(error);
        }

        @Override
        public void handle(final @NonNull Consumer<T> successConsumer, final @NonNull Consumer<E> errorConsumer) {
            errorConsumer.accept(error);
        }
        //</editor-fold>

        //<editor-fold desc="Mapping methods" defaultstate="collapsed">

        @Override
        public <R> @NotNull Result<R, E> map(final @NonNull Function<T, R> mappingFunction) {
            return changeResultType();
        }

        @Override
        public <R> @NotNull Result<R, E> and(final @NonNull Result<R, E> nextResult) {
            return changeResultType();
        }

        @Override
        public <R> @NotNull Result<R, E> flatMap(final @NonNull Function<T, @NotNull Result<R, E>> mapper) {
            return changeResultType();
        }

        @Override
        public @NotNull <R> Result<T, R> or(final @NonNull Result<T, R> alternateResult) {
            return alternateResult;
        }

        @Override
        public @NotNull <R> Result<T, R> orElse(final @NonNull Function<E, @NotNull Result<T, R>> mapper) {
            return mapper.apply(error);
        }

        @Override
        public @NotNull Result<E, T> swap() {
            return success(error);
        }

        //</editor-fold>

        //<editor-fold desc="Conversion methods" defaultstate="collapsed">

        @Override
        public @NotNull Optional<T> asOptional() {
            return Optional.empty();
        }

        @Override
        public @NotNull Optional<E> asErrorOptional() {
            return Optional.ofNullable(error);
        }

        @Override
        public @NotNull ValueContainer<T> asValueContainer() {
            return ValueContainer.empty();
        }

        @Override
        public @NotNull ValueContainer<E> asErrorValueContainer() {
            return ValueContainer.of(error);
        }

        //</editor-fold>
    }

    /**
     * Holder of a {@code null}-success result.
     */
    @UtilityClass
    final class NullSuccess {

        /**
         * Instance of a {@code null}-error
         */
        private final Result<@Nullable ?, ?> INSTANCE = new Success<>(null);
    }

    /**
     * Holder of a {@code null}-error result.
     */
    @UtilityClass
    final class NullError {

        /**
         * Instance of a {@code null}-error
         */
        private final Result<?, @Nullable ?> INSTANCE = new Error<>(null);
    }

    /**
     * An exception thrown whenever {@link #unwrap()} is called on an error result.
     */
    @NoArgsConstructor
    @SuppressWarnings("PublicConstructor")
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
     * An exception thrown whenever {@link #unwrapError()} is called on a successful result.
     */
    @NoArgsConstructor
    @SuppressWarnings("PublicConstructor")
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
