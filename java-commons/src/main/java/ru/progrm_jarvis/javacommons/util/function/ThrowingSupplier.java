package ru.progrm_jarvis.javacommons.util.function;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.annotation.Any;

import java.util.function.Supplier;

/**
 * An extension of {@link Supplier} allowing throwing calls in its body.
 *
 * @param <T> the type of results supplied by this supplier
 * @param <X> the type of the exception thrown by the function
 */
@FunctionalInterface
public interface ThrowingSupplier<T, X extends Throwable> extends Supplier<T> {

    /**
     * Gets a result.
     *
     * @return a result
     * @throws X if an exception happens
     */
    T getChecked() throws X;

    @Override
    @SneakyThrows
    default T get() {
        return getChecked();
    }

    /**
     * Creates throwing supplier which always returns the provided value.
     *
     * @param value the value always returned by this factory
     * @param <T> the type of the supplied result
     * @param <X> any formal type of the (never thrown) exception
     * @return created throwing supplier
     */
    static <T, @Any X extends Throwable> @NotNull ThrowingSupplier<T, X> returning(final T value) {
        return () -> value;
    }

    /**
     * Creates a throwing supplier which always throws an exception produced by using the factory.
     *
     * @param exceptionFactory factory used for creation of the exception
     * @param <T> any formal type of the (never returned) value
     * @param <X> the type of the exception thrown by the function
     * @return throwing runnable which always throws {@code X} by creating it via the provided factory
     * @throws NullPointerException if {@code exceptionFactory} is {@code null}
     *
     * @apiNote if the {@code exceptionFactory} produces {@code null}
     * then {@link NullPointerException} will be thrown when attempting to throw the expected exception
     */
    static <@Any T, X extends Throwable> @NotNull ThrowingSupplier<T, X> throwing(
            final @NonNull Supplier<? extends @NotNull X> exceptionFactory
    ) {
        return () -> {
            throw exceptionFactory.get();
        };
    }
}
