package ru.progrm_jarvis.javacommons.util;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import ru.progrm_jarvis.javacommons.annotation.Any;

/**
 * Utility used for rethrowing checked exceptions as unchecked.
 */
@UtilityClass
public class SneakyThrower {

    /**
     * Rethrows the provided throwable without the need to handle it.
     * This <i>always throws</i> {@code X} <i>never returning</i> any value.
     *
     * @param exception rethrown exception
     * @param <X> type of the returned exception
     * @param <R> any formal return type of this expression
     *
     * @return <i>never</i>
     */
    @SneakyThrows
    public <X extends Throwable, @Any R> R sneakyThrow(final @NonNull X exception) {
        throw exception;
    }
}
