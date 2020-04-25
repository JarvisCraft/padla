package ru.progrm_jarvis.javacommons.primitive.wrapper;

import lombok.*;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;
import ru.progrm_jarvis.javacommons.primitive.Numeric;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

/**
 * {@link PrimitiveWrapper Primitive wrapper} of {@code int}.
 */
public interface IntWrapper extends PrimitiveWrapper<Integer>, Numeric {

    @Override
    default Class<Integer> getPrimitiveClass() {
        return int.class;
    }

    @Override
    default Class<Integer> getWrapperClass() {
        return Integer.class;
    }

    /**
     * Gets the value.
     *
     * @return value
     */
    int get();

    /**
     * Sets the value.
     *
     * @param value value to be set
     */
    void set(int value);

    /**
     * Sets the value to the one given returning the previous one.
     *
     * @param newValue value to be set
     * @return previous value
     */
    int getAndSet(int newValue);

    /**
     * Gets the value after what it gets incremented.
     *
     * @return value before increment
     */
    int getAndIncrement();

    /**
     * Increments the value after what it is returned.
     *
     * @return value after increment
     */
    int incrementAndGet();

    /**
     * Gets the value after what it gets decremented.
     *
     * @return value before decrement
     */
    int getAndDecrement();

    /**
     * Decrements the value after what it is returned.
     *
     * @return value after decrement
     */
    int decrementAndGet();

    /**
     * Gets the value after what delta is added to it.
     *
     * @param delta the value which should be added to the current value
     * @return value before addition
     */
    int getAndAdd(int delta);

    /**
     * Adds the delta to the value after what it is returned.
     *
     * @param delta the value which should be added to the current value
     * @return value after addition
     */
    int addAndGet(int delta);

    /**
     * Updates the current value using the specified function after what the new value is returned.
     *
     * @param updateFunction function to be used for updating the value
     * @return value after update
     */
    int getAndUpdate(@NonNull IntUnaryOperator updateFunction);

    /**
     * Gets the value after what it gets updated using the specified function.
     *
     * @param updateFunction function to be used for updating the value
     * @return value after update
     */
    int updateAndGet(@NonNull IntUnaryOperator updateFunction);

    /**
     * Updates the current value using specified function and update value after what the new value is returned.
     *
     * @param updateValue update value (will be passed as the second function parameter)
     * @param accumulatorFunction function to be used for updating the value
     * @return value after update
     */
    int getAndAccumulate(int updateValue, @NonNull IntBinaryOperator accumulatorFunction);

    /**
     * Gets the value after what it gets updated using the specified function and update value.
     *
     * @param updateValue update value (will be passed as the second function parameter)
     * @param accumulatorFunction function to be used for updating the value
     * @return value after update
     */
    int accumulateAndGet(int updateValue, @NonNull IntBinaryOperator accumulatorFunction);

    /**
     * Creates new simple int wrapper.
     *
     * @param value initial value of int wrapper
     * @return created int wrapper
     */
    static IntWrapper create(final int value) {
        return new IntIntWrapper(value);
    }

    /**
     * Creates new simple int wrapper with initial value set to {@code 0}.
     *
     * @return created int wrapper
     */
    static IntWrapper create() {
        return new IntIntWrapper();
    }

    /**
     * Creates new atomic int wrapper.
     *
     * @param value initial value of int wrapper
     * @return created int wrapper
     */
    static IntWrapper createAtomic(final int value) {
        return new AtomicIntegerIntWrapper(value);
    }

    /**
     * Creates new atomic int wrapper with initial value set to {@code 0}.
     *
     * @return created int wrapper
     */
    static IntWrapper createAtomic() {
        return new AtomicIntegerIntWrapper();
    }

    /**
     * {@link IntWrapper} implementation based on {@code int}.
     */
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    final class IntIntWrapper implements IntWrapper {

        int value;

        @Override
        public int get() {
            return value;
        }

        @Override
        public void set(final int value) {
            this.value = value;
        }

        @Override
        public int getAndSet(final int newValue) {
            val oldValue = value;
            value = newValue;

            return oldValue;
        }

        @Override
        public int getAndIncrement() {
            return value++;
        }

        @Override
        public int incrementAndGet() {
            return ++value;
        }

        @Override
        public int getAndDecrement() {
            return value--;
        }

        @Override
        public int decrementAndGet() {
            return --value;
        }

        @Override
        public int getAndAdd(final int delta) {
            val oldValue = value;
            value += delta;

            return oldValue;
        }

        @Override
        public int addAndGet(final int delta) {
            return value += delta;
        }

        @Override
        public int getAndUpdate(@NonNull final IntUnaryOperator updateFunction) {
            val oldValue = value;
            value = updateFunction.applyAsInt(oldValue);

            return oldValue;
        }

        @Override
        public int updateAndGet(@NonNull final IntUnaryOperator updateFunction) {
            return value = updateFunction.applyAsInt(value);
        }

        @Override
        public int getAndAccumulate(final int updateValue, @NonNull final IntBinaryOperator accumulatorFunction) {
            val oldValue = value;
            value = accumulatorFunction.applyAsInt(value, updateValue);

            return oldValue;
        }

        @Override
        public int accumulateAndGet(final int updateValue, @NonNull final IntBinaryOperator accumulatorFunction) {
            return value = accumulatorFunction.applyAsInt(value, updateValue);
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
            return value;
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
     * {@link IntWrapper} implementation based on {@link AtomicInteger}.
     */
    @ToString
    @EqualsAndHashCode(callSuper = false)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    final class AtomicIntegerIntWrapper implements IntWrapper {

        @Delegate(types = IntWrapper.class)
        @NonNull AtomicInteger value;

        /**
         * Creates new atomic integer int wrapper.
         *
         * @param value initial value
         */
        public AtomicIntegerIntWrapper(final int value) {
            this.value = new AtomicInteger(value);
        }

        /**
         * Creates new atomic integer int wrapper with initial value set to {@code 0}.
         */
        public AtomicIntegerIntWrapper() {
            value = new AtomicInteger();
        }
    }
}
