package ru.progrm_jarvis.reflector.wrapper;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

/**
 * Wrapper of the {@link Field}.
 *
 * @param <T> type of the object containing the wrapped method
 * @param <V> type of the field's value
 */
public interface FieldWrapper<@NotNull T, V>
        extends ReflectorWrapper<T, Field> {}
