package ru.progrm_jarvis.javacommons.util.function;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Represents a function that accepts a {@code boolean} argument and produces result of specified type.
 * This is the primitive type specialization of {@link UnaryOperator} for {@code boolean}.
 *
 * @param <R> the type of the result of the function
 *
 * @see Function non-primitive generic equivalent
 */
@FunctionalInterface
public interface BooleanFunction<R> extends Function<Boolean, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param argument the function argument
     * @return the function result
     */
    R apply(boolean argument);

    @Override
    default R apply(final Boolean argument /* no need for explicit null-check */) {
        return apply(argument.booleanValue());
    }
}
