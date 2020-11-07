/*
 * Copyright 2019 Feather Core
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.progrm_jarvis.javacommons.object;

import lombok.*;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A value which may be present (which includes {@code null}) or not-present.
 * <p>
 * This differs from {@link java.util.Optional} as presence of a {@code null} value
 * does not make this value container empty.
 *
 * @param <T> type of stored value
 */
@SuppressWarnings("PublicInnerClass")
public interface ValueContainer<T> extends Supplier<T> {

    /**
     * Gets a value container being empty.
     *
     * @param <T> type of stored value
     * @return empty value container
     */
    @SuppressWarnings("unchecked")
    static <T> @NotNull ValueContainer<T> empty() {
        return (ValueContainer<T>) Empty.INSTANCE;
    }

    /**
     * Gets a non-empty value container containing {@code null}.
     *
     * @param <T> type of stored value
     * @return value container containing {@code null}
     */
    @SuppressWarnings("unchecked")
    static <T> @NotNull ValueContainer<T> ofNull() {
        return (ValueContainer<T>) OfNull.INSTANCE;
    }

    /**
     * Gets a non-empty value container containing the specified value.
     *
     * @param value nullable value stored in the container
     * @param <T> type of stored value
     * @return non-empty value container containing the specified value
     */
    @SuppressWarnings("unchecked")
    static <T> @NotNull ValueContainer<T> of(final @Nullable T value) {
        return value == null ? (ValueContainer<T>) OfNull.INSTANCE : new Containing<>(value);
    }

    /**
     * Gets a value container containing the specified value or an empty one if the values is {@code null}.
     *
     * @param value value stored in the container
     * @param <T> type of stored value
     * @return non-empty value container containing the specified value
     * if it is not {@code null} and an empty one otherwise
     */
    @SuppressWarnings("unchecked")
    static <T> @NotNull ValueContainer<T> nonnullOrEmpty(final @Nullable T value) {
        return value == null ? (ValueContainer<T>) Empty.INSTANCE : new Containing<>(value);
    }

    /**
     * Gets the value stored in this value container.
     *
     * @return stored value if it is {@link #isPresent() present}
     *
     * @throws EmptyValueException if this value container is empty
     */
    @Override
    T get();

    /**
     * Checks whether the object is present ot not.
     *
     * @return {@code true} if the value is present (might be {@code null}) and {@code false} otherwise
     */
    boolean isPresent();

    /**
     * Gets the value if it is present otherwise using the specified one.
     *
     * @param value value to use if this value container is empty
     * @return this value container's value if it is not empty or the specified value otherwise
     */
    @Nullable T orElse(@Nullable T value);

    /**
     * Gets the value if it is present otherwise using the one got from the specified supplier.
     *
     * @param defaultValueSupplier supplier to be used for getting the value if this value container is empty
     * @return this value container's value if it is not empty or the one got from the supplier otherwise
     */
    @Nullable T orElseGet(@NonNull Supplier<T> defaultValueSupplier);

    /**
     * Gets the value if it is present otherwise throwing an exception.
     *
     * @param exceptionSupplier supplier to be used for getting the exception if this value container is empty
     * @param <X> type of an exception thrown if this value container is empty
     * @return this value container's value if it is not empty
     *
     * @throws X if this value container is empty
     */
    <X extends Throwable> @Nullable T orElseThrow(final @NonNull Supplier<X> exceptionSupplier) throws X;

    /**
     * Gets the value if it is present otherwise throwing an exception.
     * Throws {@code X} if this value container is empty.
     *
     * @param exceptionSupplier supplier to be used for getting the exception if this value container is empty
     * @param <X> type of an exception thrown if this value container is empty
     * @return this value container's value if it is not empty
     */
    @SneakyThrows
    default <X extends Throwable> @Nullable T orElseSneakyThrow(final @NonNull Supplier<X> exceptionSupplier) {
        return orElseThrow(exceptionSupplier);
    }

    /**
     * Converts this value container into a {@link Result#nullError() null-error result}.
     *
     * @param <E> type of the result error
     * @return {@link Result#success(Object) successful result} containing the value contained by this value container
     * if is is {@link #isPresent() present} or a {@link Result#nullError() null-error result} otherwise
     *
     * @see #asResult(Supplier) alternative with customizable error value
     */
    <E> @NotNull Result<T, @Nullable E> asResult();

    /**
     * Converts this value container into a {@link Result}.
     *
     * @param errorSupplier supplier of the error in case of this value container being empty
     * @param <E> type of the result error
     * @return {@link Result#success(Object) successful result} containing the value contained by this value container
     * if is is {@link #isPresent() present} or an {@link Result#error(Object) error result}
     * with the error got by using the specified supplier
     *
     * @see #asResult() alternative with default (i.e. null) error
     */
    <E> @NotNull Result<T, E> asResult(Supplier<E> errorSupplier);

    /**
     * Empty value-container.
     *
     * @param <T> type of stored value
     */
    // use identity equals and hashCode
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class Empty<T> implements ValueContainer<T> {

        /**
         * Singleton instance of this empty {@link ValueContainer value-container}
         */
        private static final ValueContainer<?> INSTANCE = new Empty<>();

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public T get() {
            throw new EmptyValueException("There is no value associated with this value container");
        }

        @Override
        public @Nullable T orElse(final @Nullable T value) {
            return value;
        }

        @Override
        public @Nullable T orElseGet(final @NonNull Supplier<T> defaultValueSupplier) {
            return defaultValueSupplier.get();
        }

        @Override
        public <X extends Throwable> @Nullable T orElseThrow(final @NonNull Supplier<X> exceptionSupplier) throws X {
            throw exceptionSupplier.get();
        }

        @Override
        public @NotNull <E> Result<T, @Nullable E> asResult() {
            return Result.nullError();
        }

        @Override
        public @NotNull <E> Result<T, E> asResult(final Supplier<E> errorSupplier) {
            return Result.error(errorSupplier.get());
        }

        @Override
        public String toString() {
            return "Empty ValueContainer";
        }
    }

    /**
     * Non-empty value-container containing {@code null}.
     *
     * @param <T> type of stored value
     */
    // use identity equals and hashCode
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class OfNull<T> implements ValueContainer<T> {

        /**
         * Singleton instance of this {@link ValueContainer value-container} containing {@code null}
         */
        private static final ValueContainer<?> INSTANCE = new OfNull<>();

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public T get() {
            return null;
        }

        @Override
        public @Nullable T orElse(final @Nullable T value) {
            return null;
        }

        @Override
        public @Nullable T orElseGet(final @NonNull Supplier<T> defaultValueSupplier) {
            return null;
        }

        @Override
        public <X extends Throwable> @Nullable T orElseThrow(final @NonNull Supplier<X> exceptionSupplier) {
            return null;
        }

        @Override
        public @NotNull <E> Result<T, @Nullable E> asResult() {
            return Result.nullSuccess();
        }

        @Override
        public @NotNull <E> Result<T, E> asResult(final Supplier<E> errorSupplier) {
            return Result.nullSuccess();
        }

        @Override
        public String toString() {
            return "ValueContainer{null}";
        }
    }

    /**
     * A simple value-container which is meant to keep one unchanged value.
     *
     * @param <T> type of stored value
     */
    @Value
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class Containing<T> implements ValueContainer<T> {

        /**
         * Value stored by this value container
         */
        @NonNull T value;

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public @Nullable T orElse(final @Nullable T value) {
            return value;
        }

        @Override
        public @Nullable T orElseGet(final @NonNull Supplier<T> defaultValueSupplier) {
            return value;
        }

        @Override
        public <X extends Throwable> @Nullable T orElseThrow(final @NonNull Supplier<X> exceptionSupplier) {
            return value;
        }

        @Override
        public @NotNull <E> Result<T, @Nullable E> asResult() {
            return Result.success(value);
        }

        @Override
        public @NotNull <E> Result<T, E> asResult(final Supplier<E> errorSupplier) {
            return Result.success(value);
        }
    }

    /**
     * Value-container which provides new values for each request.
     *
     * @param <T> type of stored value
     */
    @Value
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class Supplying<T> implements ValueContainer<T> {

        /**
         * Supplier responsible for supplying new values on each value request
         */
        @Getter(AccessLevel.NONE) @Delegate @NotNull Supplier<T> valueSupplier;

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public @Nullable T orElse(final @Nullable T value) {
            return valueSupplier.get();
        }

        @Override
        public @Nullable T orElseGet(final @NonNull Supplier<T> defaultValueSupplier) {
            return valueSupplier.get();
        }

        @Override
        public <X extends Throwable> @Nullable T orElseThrow(final @NonNull Supplier<X> exceptionSupplier) {
            return valueSupplier.get();
        }

        @Override
        public @NotNull <E> Result<T, @Nullable E> asResult() {
            return Result.success(valueSupplier.get());
        }

        @Override
        public @NotNull <E> Result<T, E> asResult(final Supplier<E> errorSupplier) {
            return Result.success(valueSupplier.get());
        }
    }

    /**
     * An exception thrown whenever {@link #get()} is called on an empty value container.
     */
    @NoArgsConstructor
    @SuppressWarnings("PublicConstructor")
    class EmptyValueException extends RuntimeException {
        //<editor-fold desc="Inheriting constructors" defaultstate="collapsed">

        /**
         * Constructs a new exception with the specified message.
         *
         * @param message message describing the exception cause
         */
        public EmptyValueException(final String message) {
            super(message);
        }

        /**
         * Constructs a new exception with the specified message and cause.
         *
         * @param message message describing the exception cause
         * @param cause cause of this exception
         */
        public EmptyValueException(final String message, final Throwable cause) {
            super(message, cause);
        }

        /**
         * Constructs a new exception with the specified cause.
         *
         * @param cause cause of this exception
         */
        public EmptyValueException(final Throwable cause) {
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
        public EmptyValueException(final String message, final Throwable cause, final boolean enableSuppression,
                                   final boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }

        //</editor-fold>
    }
}
