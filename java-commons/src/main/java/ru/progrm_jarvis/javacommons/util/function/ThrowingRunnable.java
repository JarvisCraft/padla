package ru.progrm_jarvis.javacommons.util.function;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.annotation.Any;

import java.util.function.Supplier;

/**
 * An extension of {@link Runnable} allowing throwing calls in its body.
 *
 * @param <X> the type of the exception thrown by the function
 */
@FunctionalInterface
public interface ThrowingRunnable<X extends Throwable> extends Runnable{

    /**
     * Runs an action.
     *
     * @throws X if an exception happens
     */
    void runChecked() throws X;

    @Override
    @SneakyThrows
    default void run() {
        runChecked();
    }

    /**
     * Creates an empty (no-op) throwing runnable.
     *
     * @param <X> any formal type of the (never thrown) exception
     * @return empty throwing runnable
     */
    static <@Any X extends Throwable> @NotNull ThrowingRunnable<X> empty() {
        return () -> {};
    }

    /**
     * Creates a throwing runnable which always throws an exception produced by using the factory.
     *
     * @param exceptionFactory factory used for creation of the exception
     * @param <X> the type of the exception thrown by the function
     * @return throwing runnable which always throws {@code X} by creating it via the provided factory
     * @throws NullPointerException if {@code exceptionFactory} is {@code null}
     *
     * @apiNote if the {@code exceptionFactory} produces {@code null}
     * then {@link NullPointerException} will be thrown when attempting to throw the expected exception
     */
    static <@Any X extends Throwable> @NotNull ThrowingRunnable<X> throwing(
            final @NonNull Supplier<? extends @NotNull X> exceptionFactory
    ) {
        return () -> {
            throw exceptionFactory.get();
        };
    }
}
