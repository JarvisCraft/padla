package ru.progrm_jarvis.padla.annotation.util;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Pair of class package and simple name.
 */
public interface PackageAndName {

    /**
     * Gets the class package name.
     *
     * @return class package name
     */
    @Contract(pure = true)
    @NotNull String packageName();

    /**
     * Gets the simple class name.
     *
     * @return simple class name
     */
    @Contract(pure = true)
    @NotNull String simpleName();

    /**
     * Creates a new pair of class package and simple name.
     *
     * @param packageName class package name
     * @param simpleName simple class name
     *
     * @return created pair of class package and simple name
     */
    static @NotNull PackageAndName of(final @NonNull String packageName,
                                      final @NonNull String simpleName) {
        return new SimplePackageAndName(packageName, simpleName);
    }

    @Value
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class SimplePackageAndName implements PackageAndName {

        /**
         * Class package name
         */
        @NotNull String packageName;

        /**
         * Simple class name
         */
        @NotNull String simpleName;
    }
}
