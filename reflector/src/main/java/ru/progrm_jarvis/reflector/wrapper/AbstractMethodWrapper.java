package ru.progrm_jarvis.reflector.wrapper;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.lang.reflect.Method;

@ToString
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractMethodWrapper<T, R>
        extends AbstractReflectorWrapper<T, Method> implements MethodWrapper<T, R> {

    protected AbstractMethodWrapper(@NonNull final Class<? extends T> containingClass,
                                 @NonNull final Method wrapped) {
        super(containingClass, wrapped);
    }
}
