package ru.progrm_jarvis.javacommons.util.function;

import lombok.SneakyThrows;

import java.util.function.Function;

/**
 * An extension of {@link Function} allowing throwing calls in its body.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @param <X> the type of the exception thrown by the function
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, X extends Throwable> extends Function<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws X if an exception happens
     */
    R applyChecked(T t) throws X;

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws X if an exception happens
     *
     * @deprecated in favor of {@link #applyChecked(Object)}
     */
    @Deprecated
    default R invoke(final T t) throws X {
        return applyChecked(t);
    }

    @Override
    @SneakyThrows
    default R apply(final T t) {
        return applyChecked(t);
    }
}
