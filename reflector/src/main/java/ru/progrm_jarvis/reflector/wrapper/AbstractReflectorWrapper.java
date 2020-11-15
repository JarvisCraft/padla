package ru.progrm_jarvis.reflector.wrapper;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

/**
 * Simple POJO abstract implementation of the {@link ReflectorWrapper}.
 *
 * @param <T> type of class to whom the method belongs
 * @param <W> type of wrapped class element
 */
@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AbstractReflectorWrapper<@NotNull T, @NotNull W>
        implements ReflectorWrapper<T, W> {

    @NotNull Class<? extends T> containingClass;
    @NotNull W wrapped;
}
