package ru.progrm_jarvis.padla.annotation.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * Utilities for working with Java class names.
 */
@UtilityClass
public class ClassNames {

    /**
     * Splits the full class name into package name and simple name.
     *
     * @param fullName full name of the class such as {@code "java.lang.String"}
     *
     * @return pair of class package and simple name
     *
     * @throws IllegalArgumentException if {@code fullClassName} is discovered to be invalid
     */
    public @NotNull PackageAndName parsePackageAndName(final @NonNull String fullName) {
        if (fullName.isEmpty()) throw new IllegalArgumentException("Full class name cannot be empty");

        final int delimiterIndex;
        if ((delimiterIndex = fullName.lastIndexOf('.')) == 0) throw new IllegalArgumentException(
                "Fill class name cannot start with `.`"
        );

        return PackageAndName.of(
                delimiterIndex > 0 ? fullName.substring(0, delimiterIndex) : "",
                fullName.substring(delimiterIndex + 1)
        );
    }

    /**
     * Substrings the simple class name from the given full class name.
     *
     * @param fullName full name of the class such as {@code "java.lang.String"}
     *
     * @return simple name of the class qualified by the provided full name such as {@code "String"}
     *
     * @throws IllegalArgumentException if {@code fullClassName} is discovered to be invalid
     */
    public @NotNull String toSimpleClassName(final @NonNull String fullName) {
        if (fullName.isEmpty()) throw new IllegalArgumentException("Full class name cannot be empty");

        final int delimiterIndex;
        if ((delimiterIndex = fullName.lastIndexOf('.')) == 0) throw new IllegalArgumentException(
                "Fill class name cannot start with `.`"
        );

        return fullName.substring(delimiterIndex + 1);
    }

    /**
     * Substrings the package name from the given full class name.
     *
     * @param fullName full name of the class such as {@code "java.lang.String"}
     *
     * @return package name of the class qualified by the provided full name such as {@code "java.lang"}
     *
     * @throws IllegalArgumentException if {@code fullClassName} is discovered to be invalid
     */
    public @NotNull String toPackageName(final @NonNull String fullName) {
        if (fullName.isEmpty()) throw new IllegalArgumentException("Full class name cannot be empty");

        final int delimiterIndex;
        if ((delimiterIndex = fullName.lastIndexOf('.')) == 0) throw new IllegalArgumentException(
                "Fill class name cannot start with `.`"
        );

        return delimiterIndex > 0 ? fullName.substring(0, delimiterIndex) : "";
    }

    /**
     * Builds a full class name from its package name and simple name.
     *
     * @param packageName name of the package in which the class is such as {@code "java.lang"}, may be empty
     * @param simpleName simple name of the class {@code "String"}
     *
     * @return full class name such as {@code "java.lang.String"}
     *
     * @throws IllegalArgumentException if {@code packageName} is discovered to be invalid
     * @throws IllegalArgumentException if {@code simpleName} is discovered to be invalid
     */
    public @NotNull String toFullClassName(final @NotNull String packageName, final @NotNull String simpleName) {
        if (simpleName.isEmpty()) throw new IllegalArgumentException("Simple class name cannot be empty");

        return packageName.isEmpty() ? simpleName : packageName + '.' + simpleName;
    }
}
