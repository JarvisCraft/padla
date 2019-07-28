package ru.progrm_jarvis.reflector.wrapper;

import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

/**
 * {@link FieldWrapper} requiring a target object (non-bound non-static field).
 *
 * @param <T> type of the object containing the wrapped method
 * @param <V> type of the field's value
 */
public interface DynamicFieldWrapper<T, V> extends FieldWrapper<T, V> {

    /**
     * Gets the field's value.
     *
     * @param instance instance whose field it is
     * @return value of the field
     */
    V get(@NotNull T instance);

    /**
     * Sets the field's value.
     *
     * @param instance instance whose field it is
     * @param value new value of the field
     */
    void set(@NotNull T instance, V value);

    /**
     * Gets the field's value and sets it to the new one.
     *
     * @param instance instance whose field it is
     * @param value new value of the field
     * @return old value of the field
     */
    default V getAndSet(@NotNull T instance, V value) {
        val previousValue = get(instance);
        set(instance, value);

        return previousValue;
    }

    /**
     * Sets the field's value and gets it.
     *
     * @param instance instance whose field it is
     * @param value new value of the field
     * @return new value of the field (literally, {@code value})
     */
    default V setAndGet(@NotNull T instance, V value) {
        set(instance, value);

        return value;
    }

    /**
     * Gets the field's value and sets it to the new one based on the previous one.
     *
     * @param instance instance whose field it is
     * @param operator operator to transform the old value to the new one
     * @return old value of the field
     */
    default V getAndUpdate(@NotNull T instance, @NotNull UnaryOperator<V> operator) {
        val previousValue = get(instance);
        set(instance, operator.apply(previousValue));

        return previousValue;
    }

    /**
     * Sets the field value to the new one based on the old one returning its new value.
     *
     * @param instance instance whose field it is
     * @param operator operator to transform the old value to the new one
     * @return new value of the field
     */
    default V updateAndGet(@NotNull T instance, @NotNull UnaryOperator<V> operator) {
        val newValue = operator.apply(get(instance));
        set(instance, newValue);

        return newValue;
    }
}
