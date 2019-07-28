package ru.progrm_jarvis.reflector.wrapper;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.lang.reflect.Constructor;

/**
 * Simple POJO abstract implementation of the {@link ConstructorWrapper}.
 *
 * @param <T> type of object instantiated by the wrapped constructor
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractConstructorWrapper<T>
        extends AbstractReflectorWrapper<T, Constructor<? extends T>> implements ConstructorWrapper<T> {


    protected AbstractConstructorWrapper(@NonNull final Class<? extends T> containingClass,
                                      @NonNull final Constructor<? extends T> wrapped) {
        super(containingClass, wrapped);
    }
}
