package ru.progrm_jarvis.javacommons.util.function;

import lombok.SneakyThrows;

import java.util.function.BiFunction;

/**
 * An extension of {@link BiFunction} allowing throwing calls in its body.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <X> the type of the exception thrown by the function
 */
public interface ThrowingBiFunction<T, U, R, X extends Throwable> extends BiFunction<T, U, R> {

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return the function result
     * @throws X if an exception happens
     *
     */
    R invoke(T t, U u) throws X;

    @Override
    @SneakyThrows
    default R apply(final T t, final U u) {
        return invoke(t, u);
    }
}
