package ru.progrm_jarvis.javacommons.classload;

import lombok.NonNull;

import java.util.Optional;

/**
 * General purpose utility for {@link Class}-related operations.
 */
public class ClassUtil {

    /**
     * Attempts to get a class by the given name wrapping it in an {@link Optional}.
     *
     * @param className name of the class to attempt to search for
     * @param <T> type of the class
     * @return optional containing the class if it was found or an empty one otherwise
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<Class<? extends T>> getClass(@NonNull final String className) {
        try {
            return Optional.of((Class<? extends T>) Class.forName(className));
        } catch (final ClassNotFoundException e) {
            return Optional.empty();
        }
    }
}
