package ru.progrm_jarvis.javacommons.util.function;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.annotation.Any;

import java.util.function.Predicate;

/**
 * Common implementations of {@link Predicate}.
 */
@UtilityClass
public class Predicates {

    /**
     * Creates a predicate which is always {@code true}.
     *
     * @return predicate which is always {@code true}
     */
    public <@Any T> @NotNull Predicate<T> alwaysTrue() {
        return value -> true;
    }

    /**
     * Creates a predicate which is always {@code false}.
     *
     * @return predicate which is always {@code false}
     */
    public <@Any T> @NotNull Predicate<T> alwaysFalse() {
        return value -> false;
    }
}
