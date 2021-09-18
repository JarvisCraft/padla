package ru.progrm_jarvis.javacommons.util.function;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * Utilities related to {@link Runnable runnables}.
 */
@UtilityClass
public class Runnables {

    /**
     * Creates a runnable which does nothing.
     *
     * @return runnable which does nothing
     */
    public @NotNull Runnable none() {
        return () -> {};
    }
}
