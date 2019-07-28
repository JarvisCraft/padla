package ru.progrm_jarvis.reflector.wrapper;

import lombok.*;

/**
 * Simple POJO abstract implementation of the {@link ReflectorWrapper}.
 *
 * @param <T> type of class to whom the method belongs
 * @param <W> type of wrapped class element
 */
@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractReflectorWrapper<T, W> implements ReflectorWrapper<T, W> {

    @NonNull Class<? extends T> containingClass;
    @NonNull W wrapped;
}
