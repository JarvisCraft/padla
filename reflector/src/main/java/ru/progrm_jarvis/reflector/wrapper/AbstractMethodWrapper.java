package ru.progrm_jarvis.reflector.wrapper;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.lang.reflect.Method;

/**
 * Simple POJO abstract implementation of the {@link MethodWrapper}.
 *
 * @param <T> type of the object containing the wrapped method
 * @param <R> type of the method's return-value
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractMethodWrapper<T, R>
        extends AbstractReflectorWrapper<T, Method> implements MethodWrapper<T, R> {

    protected AbstractMethodWrapper(@NonNull final Class<? extends T> containingClass,
                                 @NonNull final Method wrapped) {
        super(containingClass, wrapped);
    }
}
