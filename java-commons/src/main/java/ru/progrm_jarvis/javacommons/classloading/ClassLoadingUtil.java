package ru.progrm_jarvis.javacommons.classloading;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Optional;

/**
 * General purpose utility for {@link Class}-related operations.
 */
@UtilityClass
public class ClassLoadingUtil {

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
        return getClass(className, loadIfNeeded, ClassLoadingUtil.class.getClassLoader());
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
     * Attempts to find a class by the given name.
     *
     * @param className name of the class to search for
     * @param loadIfNeeded flag indicating whether the class should be loaded if it is available but not loaded yet
     * @param classLoader class-loader to use for lookup
     * @param <T> type of the class
     * @return the class if it was found or {@code null}
     */
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> getNullableClass(@NonNull final String className,
                                                             final boolean loadIfNeeded,
                                                             @NonNull final ClassLoader classLoader) {
        try {
            return (Class<? extends T>) Class.forName(className, loadIfNeeded, classLoader);
        } catch (final ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Attempts to find a class by the given name using current class-loader.
     *
     * @param className name of the class to search for
     * @param <T> type of the class
     * @return the class if it was found or {@code null}
     */
    public <T> Class<? extends T> getNullableClass(@NonNull final String className,
                                                             final boolean loadIfNeeded) {
        return getNullableClass(className, loadIfNeeded, ClassLoadingUtil.class.getClassLoader());
    }

    /**
     * Attempts to find a class by the given name using current class-loader.
     * This will load the class if needed.
     *
     * @param className name of the class to search for
     * @param <T> type of the class
     * @return the class if it was found or {@code null}
     */
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> getNullableClass(@NonNull final String className) {
        try {
            return (Class<? extends T>) Class.forName(className);
        } catch (final ClassNotFoundException e) {
            return null;
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
        return isClassAvailable(className, loadIfNeeded, ClassLoadingUtil.class.getClassLoader());
    }

    /**
     * Checks if the class is available using current class-loader. This will load the class if needed.
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
