package ru.progrm_jarvis.reflector.wrapper;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * Simple POJO abstract implementation of the {@link MethodWrapper}.
 *
 * @param <T> type of the object containing the wrapped method
 * @param <R> type of the method's return-value
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractMethodWrapper<@NotNull T, R>
        extends AbstractReflectorWrapper<T, Method> implements MethodWrapper<T, R> {

    protected AbstractMethodWrapper(final @NotNull Class<? extends T> containingClass,
                                    final @NotNull Method wrapped) {
        super(containingClass, wrapped);
    }
}
