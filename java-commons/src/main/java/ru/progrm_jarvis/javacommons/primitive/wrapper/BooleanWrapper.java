package ru.progrm_jarvis.javacommons.primitive.wrapper;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link PrimitiveWrapper Primitive wrapper} of {@code boolean}.
 */
public interface BooleanWrapper extends PrimitiveWrapper<Boolean> {

    @Override
    default Class<Boolean> getPrimitiveClass() {
        return boolean.class;
    }

    @Override
    default Class<Boolean> getWrapperClass() {
        return Boolean.class;
    }

    /**
     * Gets the value.
     *
     * @return value
     */
    boolean get();

    /**
     * Sets the value.
     *
     * @param value value to be set
     */
    void set(boolean value);

    /**
     * Creates new simple boolean wrapper.
     *
     * @param value initial value of boolean wrapper
     * @return created boolean wrapper
     */
    static BooleanWrapper create(final boolean value) {
        return new BooleanBooleanWrapper(value);
    }

    /**
     * Creates new simple boolean wrapper with initial value set to {@code 0}.
     *
     * @return created boolean wrapper
     */
    static BooleanWrapper create() {
        return new BooleanBooleanWrapper(false);
    }

    /**
     * Creates new atomic boolean wrapper.
     *
     * @param value initial value of boolean wrapper
     * @return created boolean wrapper
     */
    static BooleanWrapper createAtomic(final boolean value) {
        return new AtomicBooleanWrapper(value);
    }

    /**
     * Creates new atomic boolean wrapper with initial value set to {@code 0}.
     *
     * @return created boolean wrapper
     */
    static BooleanWrapper createAtomic() {
        return new AtomicBooleanWrapper();
    }

    /**
     * {@link BooleanWrapper} implementation based on {@code boolean}.
     */
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    final class BooleanBooleanWrapper implements BooleanWrapper {

        boolean value;

        @Override
        public boolean get() {
            return value;
        }

        @Override
        public void set(final boolean value) {
            this.value = value;
        }
    }

    /**
     * {@link BooleanWrapper} implementation based on {@link AtomicBoolean}.
     */
    @ToString
    @EqualsAndHashCode(callSuper = false)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    final class AtomicBooleanWrapper implements BooleanWrapper {

        @NonNull AtomicBoolean value;

        /**
         * Creates new atomic boolean boolean wrapper.
         *
         * @param value initial value
         */
        public AtomicBooleanWrapper(final boolean value) {
            this.value = new AtomicBoolean(value);
        }

        /**
         * Creates new atomic boolean boolean wrapper with initial value set to {@code 0}.
         */
        public AtomicBooleanWrapper() {
            value = new AtomicBoolean();
        }

        @Override
        public boolean get() {
            return value.get();
        }

        @Override
        public void set(final boolean value) {
            this.value.set(value);
        }
    }
}
