package ru.progrm_jarvis.reflector.wrapper;

import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public interface DynamicFieldWrapper<T, V> extends FieldWrapper<T, V> {

    V get(@NotNull T instance);

    void set(@NotNull T instance, V value);

    default V getAndSet(@NotNull T instance, V value) {
        val previousValue = get(instance);
        set(instance, value);

        return previousValue;
    }

    default V setAndGet(@NotNull T instance, V value) {
        set(instance, value);

        return value;
    }

    default V getAndUpdate(@NotNull T instance, @NotNull UnaryOperator<V> operator) {
        val previousValue = get(instance);
        set(instance, operator.apply(previousValue));

        return previousValue;
    }

    default V updateAndGet(@NotNull T instance, @NotNull UnaryOperator<V> operator) {
        val newValue = operator.apply(get(instance));
        set(instance, newValue);

        return newValue;
    }
}
