package ru.progrm_jarvis.reflector.wrapper;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

/**
 * Simple POJO abstract implementation of the {@link FieldWrapper}.
 *
 * @param <T> type of the object containing the wrapped method
 * @param <V> type of the field's value
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractFieldWrapper<@NotNull T, V>
        extends AbstractReflectorWrapper<T, Field> implements FieldWrapper<T, V> {

    protected AbstractFieldWrapper(final @NotNull Class<? extends T> containingClass,
                                   final @NotNull Field wrapped) {
        super(containingClass, wrapped);
    }
}
