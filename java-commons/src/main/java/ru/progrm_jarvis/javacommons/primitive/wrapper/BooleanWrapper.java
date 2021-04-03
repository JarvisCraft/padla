package ru.progrm_jarvis.javacommons.primitive.wrapper;

import lombok.*;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link PrimitiveWrapper Primitive wrapper} of {@code boolean}.
 */
public interface BooleanWrapper extends PrimitiveWrapper<Boolean> {

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
        return new AtomicBooleanWrapper(new AtomicBoolean(value));
    }

    /**
     * Creates new atomic boolean wrapper with initial value set to {@code 0}.
     *
     * @return created boolean wrapper
     */
    static BooleanWrapper createAtomic() {
        return new AtomicBooleanWrapper(new AtomicBoolean());
    }

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
     * Sets the value to the one given returning the previous one.
     *
     * @param newValue value to be set
     * @return previous value
     */
    boolean getAndSet(boolean newValue);

    /**
     * {@link BooleanWrapper} implementation based on {@code boolean}.
     */
    @ToString
    @EqualsAndHashCode
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
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

        @Override
        public boolean getAndSet(final boolean newValue) {
            val oldValue = value;
            value = newValue;

            return oldValue;
        }
    }

    /**
     * {@link BooleanWrapper} implementation based on {@link AtomicBoolean}.
     */
    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    final class AtomicBooleanWrapper implements BooleanWrapper {

        @Delegate(types = BooleanWrapper.class, excludes = PrimitiveWrapper.class)
        @NotNull AtomicBoolean value;
    }
}
