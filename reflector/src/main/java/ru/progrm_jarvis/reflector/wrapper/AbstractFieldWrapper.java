package ru.progrm_jarvis.reflector.wrapper;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.lang.reflect.Field;

/**
 * Simple POJO abstract implementation of the {@link FieldWrapper}.
 *
 * @param <T> type of the object containing the wrapped method
 * @param <V> type of the field's value
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractFieldWrapper<T, V>
        extends AbstractReflectorWrapper<T, Field> implements FieldWrapper<T, V> {

    protected AbstractFieldWrapper(@NonNull final Class<? extends T> containingClass,
                                   @NonNull final Field wrapped) {
        super(containingClass, wrapped);
    }
}
