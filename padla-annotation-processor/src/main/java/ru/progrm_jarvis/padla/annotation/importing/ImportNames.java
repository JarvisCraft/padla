package ru.progrm_jarvis.padla.annotation.importing;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

/**
 * Common comparators for importing.
 */
@UtilityClass
public class ImportNames {

    public @NotNull Comparator<@NotNull String> javaPackagesLast() {
        return (left, right) -> {
            // Java package
            return isInJavaPackage(left)
                    ? isInJavaPackage(right) ? left.compareTo(right) : 1
                    : isInJavaPackage(right) ? -1 : left.compareTo(right);
        };
    }

    public boolean isInJavaPackage(final @NotNull String packageName) {
        // minimal form is `java.X`
        if (packageName.length() < 6 || !packageName.startsWith("java")) return false;

        final char nextChar;
        return (nextChar = packageName.charAt(4)) == '.' || nextChar == 'x' && packageName.charAt(5) == '.';
    }
}
