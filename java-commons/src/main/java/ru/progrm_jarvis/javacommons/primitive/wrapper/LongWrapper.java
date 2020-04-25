package ru.progrm_jarvis.javacommons.primitive.wrapper;

import lombok.*;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;
import ru.progrm_jarvis.javacommons.primitive.Numeric;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;

/**
 * {@link PrimitiveWrapper Primitive wrapper} of {@code long}.
 */
public interface LongWrapper extends PrimitiveWrapper<Long>, Numeric {

    /**
     * Creates new simple long wrapper.
     *
     * @param value initial value of long wrapper
     * @return created long wrapper
     */
    static LongWrapper create(final long value) {
        return new LongLongWrapper(value);
    }

    /**
     * Creates new simple long wrapper with initial value set to {@code 0}.
     *
     * @return created long wrapper
     */
    static LongWrapper create() {
        return new LongLongWrapper(0);
    }

    /**
     * Creates new atomic long wrapper.
     *
     * @param value initial value of long wrapper
     * @return created long wrapper
     */
    static LongWrapper createAtomic(final long value) {
        return new AtomicLongLongWrapper(value);
    }

    /**
     * Creates new atomic long wrapper with initial value set to {@code 0}.
     *
     * @return created long wrapper
     */
    static LongWrapper createAtomic() {
        return new AtomicLongLongWrapper();
    }

    @Override
    default Class<Long> getPrimitiveClass() {
        return long.class;
    }

    @Override
    default Class<Long> getWrapperClass() {
        return Long.class;
    }

    /**
     * Gets the value.
     *
     * @return value
     */
    long get();

    /**
     * Sets the value.
     *
     * @param value value to be set
     */
    void set(long value);

    /**
     * Sets the value to the one given returning the previous one.
     *
     * @param newValue value to be set
     * @return previous value
     */
    long getAndSet(long newValue);

    /**
     * Gets the value after what it gets incremented.
     *
     * @return value before increment
     */
    long getAndIncrement();

    /**
     * Increments the value after what it is returned.
     *
     * @return value after increment
     */
    long incrementAndGet();

    /**
     * Gets the value after what it gets decremented.
     *
     * @return value before decrement
     */
    long getAndDecrement();

    /**
     * Decrements the value after what it is returned.
     *
     * @return value after decrement
     */
    long decrementAndGet();

    /**
     * Gets the value after what delta is added to it.
     *
     * @param delta the value which should be added to the current value
     * @return value before addition
     */
    long getAndAdd(long delta);

    /**
     * Adds the delta to the value after what it is returned.
     *
     * @param delta the value which should be added to the current value
     * @return value after addition
     */
    long addAndGet(long delta);

    /**
     * Updates the current value using the specified function after what the new value is returned.
     *
     * @param updateFunction function to be used for updating the value
     * @return value after update
     */
    long getAndUpdate(@NonNull LongUnaryOperator updateFunction);

    /**
     * Gets the value after what it gets updated using the specified function.
     *
     * @param updateFunction function to be used for updating the value
     * @return value after update
     */
    long updateAndGet(@NonNull LongUnaryOperator updateFunction);

    /**
     * Updates the current value using specified function and update value after what the new value is returned.
     *
     * @param updateValue update value (will be passed as the second function parameter)
     * @param accumulatorFunction function to be used for updating the value
     * @return value after update
     */
    long getAndAccumulate(long updateValue, @NonNull LongBinaryOperator accumulatorFunction);

    /**
     * Gets the value after what it gets updated using the specified function and update value.
     *
     * @param updateValue update value (will be passed as the second function parameter)
     * @param accumulatorFunction function to be used for updating the value
     * @return value after update
     */
    long accumulateAndGet(long updateValue, @NonNull LongBinaryOperator accumulatorFunction);

    /**
     * {@link LongWrapper} implementation based on {@code long}.
     */
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    final class LongLongWrapper implements LongWrapper {

        long value;

        @Override
        public long get() {
            return value;
        }

        @Override
        public void set(final long value) {
            this.value = value;
        }

        @Override
        public long getAndSet(final long newValue) {
            val oldValue = value;
            value = newValue;

            return oldValue;
        }

        @Override
        public long getAndIncrement() {
            return value++;
        }

        @Override
        public long incrementAndGet() {
            return ++value;
        }

        @Override
        public long getAndDecrement() {
            return value--;
        }

        @Override
        public long decrementAndGet() {
            return --value;
        }

        @Override
        public long getAndAdd(final long delta) {
            val oldValue = value;
            value += delta;

            return oldValue;
        }

        @Override
        public long addAndGet(final long delta) {
            return value += delta;
        }

        @Override
        public long getAndUpdate(@NonNull final LongUnaryOperator updateFunction) {
            val oldValue = value;
            value = updateFunction.applyAsLong(oldValue);

            return oldValue;
        }

        @Override
        public long updateAndGet(@NonNull final LongUnaryOperator updateFunction) {
            return value = updateFunction.applyAsLong(value);
        }

        @Override
        public long getAndAccumulate(final long updateValue, @NonNull final LongBinaryOperator accumulatorFunction) {
            val oldValue = value;
            value = accumulatorFunction.applyAsLong(value, updateValue);

            return oldValue;
        }

        @Override
        public long accumulateAndGet(final long updateValue, @NonNull final LongBinaryOperator accumulatorFunction) {
            return value = accumulatorFunction.applyAsLong(value, updateValue);
        }

        @Override
        public byte byteValue() {
            return (byte) value;
        }

        @Override
        public short shortValue() {
            return (short) value;
        }

        @Override
        public int intValue() {
            return (int) value;
        }

        @Override
        public long longValue() {
            return value;
        }

        @Override
        public float floatValue() {
            return value;
        }

        @Override
        public double doubleValue() {
            return value;
        }
    }

    /**
     * {@link LongWrapper} implementation based on {@link AtomicLong}.
     */
    @ToString
    @EqualsAndHashCode(callSuper = false)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    final class AtomicLongLongWrapper implements LongWrapper {

        @Delegate(types = LongWrapper.class, excludes = PrimitiveWrapper.class)
        @NonNull AtomicLong value;

        /**
         * Creates new atomic long long wrapper.
         *
         * @param value initial value
         */
        private AtomicLongLongWrapper(final long value) {
            this.value = new AtomicLong(value);
        }

        /**
         * Creates new atomic long long wrapper with initial value set to {@code 0}.
         */
        private AtomicLongLongWrapper() {
            value = new AtomicLong();
        }
    }
}
