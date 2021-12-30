package ru.progrm_jarvis.padla.annotation.importing;

import lombok.NonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

/**
 * Set of imported classes.
 */
public interface Imports {

    /**
     * Retrieves currently requested imports.
     *
     * @return currently requested imports
     */
    @NotNull @Unmodifiable Set<@NotNull String> imports();

    /**
     * Imports the provided class.
     *
     * @param packageName package name of the imported class
     * @param simpleName simple name of the imported class
     *
     * @return name to be used for the provided class considering the import
     */
    @Contract(mutates = "this")
    @NotNull String importClass(@NonNull String packageName, @NonNull String simpleName);

    /**
     * Imports the provided class.
     *
     * @param fullName full name of the imported class
     *
     * @return name to be used for the provided class considering the import
     */
    @Contract(mutates = "this")
    @NotNull String importClass(@NonNull String fullName);
}
