package ru.progrm_jarvis.javacommons.util.function;

import lombok.NonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Represents an operation on a single {@code boolean} operand that produces a {@code boolean}} result.
 * This is the primitive type specialization of {@link UnaryOperator} for {@code boolean}.
 *
 * @see UnaryOperator non-primitive generic equivalent
 */
@FunctionalInterface
public interface BooleanUnaryOperator extends BooleanFunction<Boolean>, UnaryOperator<Boolean> {

    /**
     * Applies this operator to the given operand.
     *
     * @param operand the operand to which to apply this operation
     * @return result of applying this operation to the operand
     */
    boolean applyAsBoolean(boolean operand);

    @Override
    default @NotNull Boolean apply(boolean argument) {
        return applyAsBoolean(argument);
    }

    @Override
    default <V> @NotNull Function<V, Boolean> compose(final @NonNull Function<? super V, ? extends Boolean> before) {
        return operand -> applyAsBoolean(before.apply(operand));
    }

    /**
     * Returns a composed operator that first applies the {@code before} operator to its input
     * and then applies this operator to the result.
     *
     * @param before the operator to provide the value to this operator
     * @return a composed operator that first applies the provided operator and then this one
     * @throws NullPointerException if {@code before} is {@code null}
     *
     * @see #andThen(BooleanUnaryOperator) behaving in opposite manner
     */
    @Contract(value = "null -> fail; _ -> _", pure = true)
    default @NotNull BooleanUnaryOperator composePrimitive(final @NonNull BooleanUnaryOperator before) {
        return operand -> applyAsBoolean(before.applyAsBoolean(operand));
    }

    @Override
    default <V> @NotNull Function<Boolean, V> andThen(final @NonNull Function<? super Boolean, ? extends V> after) {
        return operand -> after.apply(applyAsBoolean(operand));
    }

    /**
     * Returns a composed operator that first applies this operator to its input
     * and then applies the {@code after} operator to the result.
     *
     * @param after the operator to operate on the value provided by this operator
     * @return a composed operator that first applies this operator and then the provided one
     * @throws NullPointerException if {@code after} is {@code null}
     *
     * @see #composePrimitive(BooleanUnaryOperator) behaving in opposite manner
     */
    @Contract(value = "null -> fail; _ -> _", pure = true)
    default @NotNull BooleanUnaryOperator andThen(final @NonNull BooleanUnaryOperator after) {
        return operand -> after.applyAsBoolean(applyAsBoolean(operand));
    }

    /**
     * Returns a composed operator that first applies this operator and and then inverts the result.
     *
     * @return a composed operator that first applies this operator and then inverts the result
     */
    default @NotNull BooleanUnaryOperator invert() {
        return operand -> !applyAsBoolean(operand);
    }

    /**
     * Returns an unary operator that always returns its input argument.
     *
     * @return a unary operator that always returns its input argument
     */
    @Contract(value = "-> _", pure = true)
    static @NotNull BooleanUnaryOperator identity() {
        return operand -> operand;
    }
}
