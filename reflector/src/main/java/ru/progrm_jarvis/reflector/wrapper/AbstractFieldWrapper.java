package ru.progrm_jarvis.reflector.wrapper;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.lang.reflect.Field;

@ToString
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractFieldWrapper<V, R>
        extends AbstractReflectorWrapper<V, Field> implements FieldWrapper<V, R> {

    protected AbstractFieldWrapper(@NonNull final Class<? extends V> containingClass,
                                   @NonNull final Field wrapped) {
        super(containingClass, wrapped);
    }
}
