package ru.progrm_jarvis.reflector.wrapper;

import lombok.*;

@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractReflectorWrapper<T, W> implements ReflectorWrapper<T, W> {

    @NonNull Class<? extends T> containingClass;
    @NonNull W wrapped;
}
