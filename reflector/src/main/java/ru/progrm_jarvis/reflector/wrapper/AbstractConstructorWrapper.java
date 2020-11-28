package ru.progrm_jarvis.reflector.wrapper;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

/**
 * Simple POJO abstract implementation of the {@link ConstructorWrapper}.
 *
 * @param <T> type of object instantiated by the wrapped constructor
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractConstructorWrapper<@NotNull T>
        extends AbstractReflectorWrapper<T, Constructor<? extends T>> implements ConstructorWrapper<T> {

    protected AbstractConstructorWrapper(final @NotNull Class<? extends T> containingClass,
                                         final @NotNull Constructor<? extends T> wrapped) {
        super(containingClass, wrapped);
    }
}
