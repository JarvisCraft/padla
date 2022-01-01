package ru.progrm_jarvis.padla.annotation.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.SourceVersion;

/**
 * Utilities for working with {@link SourceVersion source versions}.
 */
@UtilityClass
public class SourceVersions {

    /**
     * Picks the bigger source version.
     *
     * @param left the first source version
     * @param right the second source version
     * @return the bigger source version
     */
    public @NotNull SourceVersion max(final @NonNull SourceVersion left,
                                      final @NotNull SourceVersion right) {
        return left.compareTo(right) > 0 ? left : right;
    }

    /**
     * Picks the smaller source version.
     *
     * @param left the first source version
     * @param right the second source version
     * @return the smaller source version
     */
    public @NotNull SourceVersion min(final @NonNull SourceVersion left,
                                      final @NotNull SourceVersion right) {
        return left.compareTo(right) < 0 ? left : right;
    }
}
