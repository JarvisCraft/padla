package ru.progrm_jarvis.javacommons.util.function;

import lombok.SneakyThrows;

import java.util.function.Consumer;

/**
 * An extension of {@link Consumer} allowing throwing calls in its body.
 *
 * @param <T> the type of the input to the operation
 * @param <X> the type of the exception thrown by the function
 */
@FunctionalInterface
public interface ThrowingConsumer<T, X extends Throwable> extends Consumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws X if an exception happens
     */
    void acceptChecked(T t) throws X;

    @Override
    @SneakyThrows
    default void accept(final T t) {
        acceptChecked(t);
    }
}
