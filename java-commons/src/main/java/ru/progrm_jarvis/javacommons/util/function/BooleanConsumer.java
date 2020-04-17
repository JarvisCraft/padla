package ru.progrm_jarvis.javacommons.util.function;

import lombok.NonNull;

import java.util.function.Consumer;

/**
 * Represents an operation that accepts a single {@code boolean} argument and returns no result.
 * This is the primitive type specialization of {@link Consumer} for {@code boolean}.
 *
 * @see Consumer non-primitive generic equivalent
 */
@FunctionalInterface
public interface BooleanConsumer extends Consumer<Boolean> {

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    void accept(boolean value);

    @Override
    default void accept(final Boolean value /* no need for explicit null-check */) {
        accept(value.booleanValue());
    }

    /**
     * Returns a composed consumer that performs, in sequence, this operation followed by the {@code after} operation.
     *
     * @param after the operation to perform after this operation
     * @return a composed operator that first performs this operation and then the provided one
     * @throws NullPointerException if {@code after} is {@code null}
     */
    default BooleanConsumer andThen(@NonNull final BooleanConsumer after) {
        return value -> {
            accept(value);
            after.accept(value);
        };
    }
}
