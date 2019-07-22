package ru.progrm_jarvis.reflector.wrapper;

import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public interface StaticFieldWrapper<T, V> extends FieldWrapper<T, V> {

    V get();

    void set(V value);

    default V getAndSet(V value) {
        val previousValue = get();
        set(value);

        return previousValue;
    }

    default V setAndGet(V value) {
        set(value);

        return value;
    }

    default V getAndUpdate(@NotNull UnaryOperator<V> operator) {
        val previousValue = get();
        set(operator.apply(previousValue));

        return previousValue;
    }

    default V updateAndGet(@NotNull UnaryOperator<V> operator) {
        val newValue = operator.apply(get());
        set(newValue);

        return newValue;
    }
}
