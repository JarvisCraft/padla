package ru.progrm_jarvis.javacommons.classload;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Optional;

/**
 * General purpose utility for {@link Class}-related operations.
 */
@UtilityClass
public class ClassUtil {

    /**
     * Attempts to find a class by the given name wrapping it in an {@link Optional}.
     *
     * @param className name of the class to search for
     * @param loadIfNeeded flag indicating whether the class should be loaded if it is available but not loaded yet
     * @param classLoader class-loader to use for lookup
     * @param <T> type of the class
     * @return optional containing the class if it was found or an empty one otherwise
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<Class<? extends T>> getClass(@NonNull final String className,
                                                     final boolean loadIfNeeded,
                                                     @NonNull final ClassLoader classLoader) {
        try {
            return Optional.of((Class<? extends T>) Class.forName(className, loadIfNeeded, classLoader));
        } catch (final ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Attempts to find a class by the given name using current class-loader wrapping it in an {@link Optional}.
     *
     * @param className name of the class to search for
     * @param <T> type of the class
     * @return optional containing the class if it was found or an empty one otherwise
     */
    public <T> Optional<Class<? extends T>> getClass(@NonNull final String className,
                                                     final boolean loadIfNeeded) {
        return getClass(className, loadIfNeeded, ClassUtil.class.getClassLoader());
    }

    /**
     * Attempts to find a class by the given name using current class-loader wrapping it in an {@link Optional}. This
     * will load the class if needed.
     *
     * @param className name of the class to search for
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

    /**
     * Checks if the class is available.
     *
     * @param className name of the class to search for
     * @param loadIfNeeded flag indicating whether the class should be loaded if it is available but not loaded yet
     * @param classLoader class-loader to use for lookup
     * @return {@code true} if the class is available and {@code false} otherwise
     */
    public boolean isClassAvailable(@NonNull final String className,
                                    final boolean loadIfNeeded,
                                    @NonNull final ClassLoader classLoader) {
        try {
            Class.forName(className, loadIfNeeded, classLoader);
        } catch (final ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    /**
     * Checks if the class is available using current class-loader.
     *
     * @param className name of the class to search for
     * @return {@code true} if the class is available and {@code false} otherwise
     */
    public boolean isClassAvailable(@NonNull final String className,
                                    final boolean loadIfNeeded) {
        return isClassAvailable(className, loadIfNeeded, ClassUtil.class.getClassLoader());
    }

    /**
     * Checks if the class is available using current class-loader.
     * This will load the class if needed.
     *
     * @param className name of the class to search for
     * @return {@code true} if the class is available and {@code false} otherwise
     */
    public boolean isClassAvailable(@NonNull final String className) {
        try {
            Class.forName(className);
        } catch (final ClassNotFoundException e) {
            return false;
        }
        return true;
    }
}
