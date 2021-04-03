package ru.progrm_jarvis.javacommons.primitive.wrapper;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/**
 * Wrapper of a reference.
 */
public interface ReferenceWrapper<T> {

    /**
     * Creates new simple reference wrapper.
     *
     * @param value initial reference of int wrapper
     * @return created reference wrapper
     */
    static <T> ReferenceWrapper<T> create(final T value) {
        return new PrimitiveReferenceWrapper<>(value);
    }

    /**
     * Creates new simple reference wrapper with initial value set to {@code null}.
     *
     * @return created reference wrapper
     */
    static <T> ReferenceWrapper<T> create() {
        return new PrimitiveReferenceWrapper<>();
    }

    /**
     * Creates new atomic reference wrapper.
     *
     * @param value initial value of reference wrapper
     * @return created reference wrapper
     */
    static <T> ReferenceWrapper<T> createAtomic(final T value) {
        return new AtomicReferenceWrapper<>(new AtomicReference<>(value));
    }

    /**
     * Creates new atomic reference wrapper with initial value set to {@code null}.
     *
     * @return created reference wrapper
     */
    static <T> ReferenceWrapper<T> createAtomic() {
        return new AtomicReferenceWrapper<>(new AtomicReference<>());
    }

    /**
     * Gets the value.
     *
     * @return value
     */
    T get();

    /**
     * Sets the value.
     *
     * @param newValue value to be set
     */
    void set(T newValue);

    /**
     * Sets the value to the one given returning the previous one.
     *
     * @param newValue value to be set
     * @return previous value
     */
    T getAndSet(T newValue);

    /**
     * Updates the current value using the specified function after what the new value is returned.
     *
     * @param updateFunction function to be used for updating the value
     * @return value before update
     */
    T getAndUpdate(@NonNull UnaryOperator<T> updateFunction);

    /**
     * Gets the value after what it gets updated using the specified function.
     *
     * @param updateFunction function to be used for updating the value
     * @return value after update
     */
    T updateAndGet(@NonNull UnaryOperator<T> updateFunction);

    /**
     * Updates the current value using specified function and update value after what the new value is returned.
     *
     * @param updateValue update value (will be passed as the second function parameter)
     * @param accumulatorFunction function to be used for updating the value
     * @return value before update
     */
    T getAndAccumulate(T updateValue, @NonNull BinaryOperator<T> accumulatorFunction);

    /**
     * Gets the value after what it gets updated using the specified function and update value.
     *
     * @param updateValue update value (will be passed as the second function parameter)
     * @param accumulatorFunction function to be used for updating the value
     * @return value after update
     */
    T accumulateAndGet(T updateValue, @NonNull BinaryOperator<T> accumulatorFunction);

    /**
     * {@link ReferenceWrapper} implementation based on primitive reference.
     */
    @ToString
    @EqualsAndHashCode
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    final class PrimitiveReferenceWrapper<T> implements ReferenceWrapper<T> {

        T value;

        @Override
        public T get() {
            return value;
        }

        @Override
        public void set(final T newValue) {
            value = newValue;
        }

        @Override
        public T getAndSet(final T newValue) {
            val oldValue = value;
            value = newValue;

            return oldValue;
        }

        @Override
        public T getAndUpdate(final @NonNull UnaryOperator<T> updateFunction) {
            final T oldValue;
            value = updateFunction.apply(oldValue = value);

            return oldValue;
        }

        @Override
        public T updateAndGet(final @NonNull UnaryOperator<T> updateFunction) {
            return value = updateFunction.apply(value);
        }

        @Override
        public T getAndAccumulate(final T updateValue, final @NonNull BinaryOperator<T> accumulatorFunction) {
            final T oldValue;
            value = accumulatorFunction.apply(oldValue = value, updateValue);

            return oldValue;
        }

        @Override
        public T accumulateAndGet(final T updateValue, final @NonNull BinaryOperator<T> accumulatorFunction) {
            return value = accumulatorFunction.apply(value, updateValue);
        }
    }

    /**
     * {@link ReferenceWrapper} implementation based on {@link AtomicReference}.
     */
    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    final class AtomicReferenceWrapper<T> implements ReferenceWrapper<T> {

        // @Delegate(types = ReferenceWrapper.class): this guy might be feeling bad
        @NotNull AtomicReference<T> value;

        @Override
        public T get() {
            return value.get();
        }

        @Override
        public void set(final T newValue) {
            value.set(newValue);
        }

        @Override
        public T getAndSet(final T newValue) {
            return value.getAndSet(newValue);
        }

        @Override
        public T getAndUpdate(final @NonNull UnaryOperator<T> updateFunction) {
            return value.getAndUpdate(updateFunction);
        }

        @Override
        public T updateAndGet(final @NonNull UnaryOperator<T> updateFunction) {
            return value.updateAndGet(updateFunction);
        }

        @Override
        public T getAndAccumulate(final T updateValue, final @NonNull BinaryOperator<T> accumulatorFunction) {
            return value.getAndAccumulate(updateValue, accumulatorFunction);
        }

        @Override
        public T accumulateAndGet(final T updateValue, final @NonNull BinaryOperator<T> accumulatorFunction) {
            return value.accumulateAndGet(updateValue, accumulatorFunction);
        }
    }
}
