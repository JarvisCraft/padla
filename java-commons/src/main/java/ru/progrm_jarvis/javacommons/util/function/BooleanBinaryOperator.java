package ru.progrm_jarvis.javacommons.util.function;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/**
 * Represents an operation on a single {@code boolean} operand that produces a {@code boolean} result.
 * This is the primitive type specialization of {@link UnaryOperator} for {@code boolean}.
 *
 * @see UnaryOperator non-primitive generic equivalent
 */
@FunctionalInterface
public interface BooleanBinaryOperator extends BinaryOperator<Boolean> {

    /**
     * Applies this operator to the given operands.
     *
     * @param left the first operand
     * @param right the second operand
     * @return result of applying this operation to the operands
     */
    boolean applyAsBoolean(boolean left, boolean right);

    @Override
    default Boolean apply(final Boolean left /* no need for explicit null-check */,
                          final Boolean right /* no need for explicit null-check */) {
        return applyAsBoolean(left, right);
    }
}
