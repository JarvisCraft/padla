package ru.progrm_jarvis.javacommons.primitive.wrapper;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;

/**
 * {@link PrimitiveWrapper Primitive wrapper} of {@code long}.
 */
public abstract class LongWrapper extends Number implements PrimitiveWrapper {

    @Override
    public Class<?> getPrimitiveClass() {
        return long.class;
    }

    @Override
    public Class<?> getWrapperClass() {
        return Long.class;
    }

    /**
     * Gets the value.
     *
     * @return value
     */
    public abstract long get();

    /**
     * Sets the value.
     *
     * @param value value to be set
     */
    public abstract void set(long value);

    /**
     * Gets the value after what it gets incremented.
     *
     * @return value before increment
     */
    public abstract long getAndIncrement();

    /**
     * Increments the value after what it is returned.
     *
     * @return value after increment
     */
    public abstract long incrementAndGet();

    /**
     * Gets the value after what it gets decremented.
     *
     * @return value before decrement
     */
    public abstract long getAndDecrement();

    /**
     * Decrements the value after what it is returned.
     *
     * @return value after decrement
     */
    public abstract long decrementAndGet();

    /**
     * Gets the value after what delta is added to it.
     *
     * @param delta the value which should be added to the current value
     * @return value before addition
     */
    public abstract long getAndAdd(long delta);

    /**
     * Adds the delta to the value after what it is returned.
     *
     * @param delta the value which should be added to the current value
     * @return value after addition
     */
    public abstract long addAndGet(long delta);

    /**
     * Updates the current value using the specified function after what the new value is returned.
     *
     * @param updateFunction function to be used for updating the value
     * @return value after update
     */
    public abstract long getAndUpdate(@NonNull LongUnaryOperator updateFunction);

    /**
     * Gets the value after what it gets updated using the specified function.
     *
     * @param updateFunction function to be used for updating the value
     * @return value after update
     */
    public abstract long updateAndGet(@NonNull LongUnaryOperator updateFunction);

    /**
     * Updates the current value using specified function and update value after what the new value is returned.
     *
     * @param updateValue update value (will be passed as the second function parameter)
     * @param accumulatorFunction function to be used for updating the value
     * @return value after update
     */
    public abstract long getAndAccumulate(long updateValue, @NonNull LongBinaryOperator accumulatorFunction);

    /**
     * Gets the value after what it gets updated using the specified function and update value.
     *
     * @param updateValue update value (will be passed as the second function parameter)
     * @param accumulatorFunction function to be used for updating the value
     * @return value after update
     */
    public abstract long accumulateAndGet(long updateValue, @NonNull LongBinaryOperator accumulatorFunction);

    /**
     * Creates new simple long wrapper.
     *
     * @param value initial value of long wrapper
     * @return created long wrapper
     */
    public static LongWrapper create(final long value) {
        return new LongLongWrapper(value);
    }

    /**
     * Creates new simple long wrapper with initial value set to {@code 0}.
     *
     * @return created long wrapper
     */
    public static LongWrapper create() {
        return new LongLongWrapper(0);
    }

    /**
     * Creates new atomic long wrapper.
     *
     * @param value initial value of long wrapper
     * @return created long wrapper
     */
    public static LongWrapper createAtomic(final long value) {
        return new AtomicLongLongWrapper(value);
    }

    /**
     * Creates new atomic long wrapper with initial value set to {@code 0}.
     *
     * @return created long wrapper
     */
    public static LongWrapper createAtomic() {
        return new AtomicLongLongWrapper();
    }

    /**
     * {@link LongWrapper} implementation based on {@code long}.
     */
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    private static final class LongLongWrapper extends LongWrapper {

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
    private static final class AtomicLongLongWrapper extends LongWrapper {

        @NonNull AtomicLong value;

        /**
         * Creates new atomic long long wrapper.
         *
         * @param value initial value
         */
        public AtomicLongLongWrapper(final long value) {
            this.value = new AtomicLong(value);
        }

        /**
         * Creates new atomic long long wrapper with initial value set to {@code 0}.
         */
        public AtomicLongLongWrapper() {
            this.value = new AtomicLong();
        }

        @Override
        public long get() {
            return value.get();
        }

        @Override
        public void set(final long value) {
            this.value.set(value);
        }

        @Override
        public long getAndIncrement() {
            return value.getAndIncrement();
        }

        @Override
        public long incrementAndGet() {
            return value.incrementAndGet();
        }

        @Override
        public long getAndDecrement() {
            return value.getAndDecrement();
        }

        @Override
        public long decrementAndGet() {
            return value.decrementAndGet();
        }

        @Override
        public long getAndAdd(final long delta) {
            return value.getAndAdd(delta);
        }

        @Override
        public long addAndGet(final long delta) {
            return value.addAndGet(delta);
        }

        @Override
        public long getAndUpdate(@NonNull final LongUnaryOperator updateFunction) {
            return value.getAndUpdate(updateFunction);
        }

        @Override
        public long updateAndGet(@NonNull final LongUnaryOperator updateFunction) {
            return value.updateAndGet(updateFunction);
        }

        @Override
        public long getAndAccumulate(final long updateValue, @NonNull final LongBinaryOperator accumulatorFunction) {
            return value.getAndAccumulate(updateValue, accumulatorFunction);
        }

        @Override
        public long accumulateAndGet(final long updateValue, @NonNull final LongBinaryOperator accumulatorFunction) {
            return value.accumulateAndGet(updateValue, accumulatorFunction);
        }

        @Override
        public int intValue() {
            return value.intValue();
        }

        @Override
        public long longValue() {
            return value.longValue();
        }

        @Override
        public float floatValue() {
            return value.floatValue();
        }

        @Override
        public double doubleValue() {
            return value.doubleValue();
        }
    }
}
