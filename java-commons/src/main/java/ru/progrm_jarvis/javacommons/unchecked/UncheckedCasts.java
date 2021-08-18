package ru.progrm_jarvis.javacommons.unchecked;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;

/**
 * Commonly used unchecked cast operations.
 *
 * @apiNote no nullability annotations are present on parameters and return types
 * because casts of {@code null} are always safe
 */
@UtilityClass
public class UncheckedCasts {

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
    @SuppressWarnings("unchecked")
    public <T> Class<T> uncheckedClassCast(final Class<?> type) {
        return (Class<T>) type;
    }
}
