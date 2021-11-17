package ru.progrm_jarvis.javacommons.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;

/**
 * Commonly used unchecked casts.
 *
 * @apiNote parameters can be {@code null} because casting of {@code null} is always correct
 */
@UtilityClass
@SuppressWarnings("unchecked")
public class UncheckedCasts {

    /**
     * Casts the given object into the specific generic type.
     *
     * @param object raw-typed object
     * @param <T> exact wanted type of the object
     * @return the provided object with its type cast to the specific one
     *
     * @apiNote this is effectively no-op
     */
    @Contract("_ -> param1")
    public <T> T uncheckedObjectCast(final Object object) {
        return (T) object;
    }

    /**
     * Casts the given class object into the specific one.
     *
     * @param type raw-typed class object
     * @param <T> exact wanted type of class object
     * @return the provided class object with its type cast to the specific one
     *
     * @apiNote this is effectively no-op
     */
    @Contract("_ -> param1")
    public <T> Class<T> uncheckedClassCast(final Class<?> type) {
        return (Class<T>) type;
    }
}
