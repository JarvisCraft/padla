package ru.progrm_jarvis.reflector.wrapper;

import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

/**
 * {@link FieldWrapper} not requiring any target object (static field or a bound non-static field).
 *
 * @param <T> type of the object containing the wrapped method
 * @param <V> type of the field's value
 */
public interface StaticFieldWrapper<@NotNull T, V>
        extends FieldWrapper<T, V> {

    /**
     * Gets the field's value and sets it to the new one.
     *
     * @param value new value of the field
     * @return old value of the field
     */
    default V getAndSet(final V value) {
        val previousValue = get();
        set(value);

        return previousValue;
    }

    /**
     * Gets the field's value.
     *
     * @return value of the field
     */
    V get();

    /**
     * Sets the field's value.
     *
     * @param value new value of the field
     */
    void set(V value);

    /**
     * Sets the field's value and gets it.
     *
     * @param value new value of the field
     * @return new value of the field (literally, {@code value})
     */
    default V setAndGet(final V value) {
        set(value);

        return value;
    }

    /**
     * Gets the field's value and sets it to the new one based on the previous one.
     *
     * @param operator operator to transform the old value to the new one
     * @return old value of the field
     */
    default V getAndUpdate(final @NotNull UnaryOperator<V> operator) {
        val previousValue = get();
        set(operator.apply(previousValue));

        return previousValue;
    }

    /**
     * Sets the field value to the new one based on the old one returning its new value.
     *
     * @param operator operator to transform the old value to the new one
     * @return new value of the field
     */
    default V updateAndGet(final @NotNull UnaryOperator<V> operator) {
        val newValue = operator.apply(get());
        set(newValue);

        return newValue;
    }
}
