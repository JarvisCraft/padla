package ru.progrm_jarvis.javacommons.util.function;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.function.*;

/**
 * Utilities related to {@link Runnable runnables}.
 */
@UtilityClass
public class Runnables {

    /**
     * Creates a runnable which does nothing.
     *
     * @return runnable which does nothing
     */
    public @NotNull Runnable none() {
        return () -> {};
    }

    /**
     * Creates a stateful runnable which stores its state mutably.
     *
     * @param initialState initial state of the created runnable
     * @param handler handler invoked on each call to {@link Runnable#run()} with the state applied to it
     * @param <T> type of stored state
     * @return created stateful runnable
     *
     * @throws NullPointerException if {@code handler} is {@code null}
     */
    public <T> @NotNull Runnable stateful(final T initialState,
                                          final @NonNull Consumer<? super T> handler) {
        return new StatefulImmutableRunnable<>(handler, initialState);
    }

    /**
     * Creates a stateful runnable which stores its state immutably.
     *
     * @param initialState initial state of the created runnable
     * @param handler handler invoked on each call to {@link Runnable#run()} with the state applied to it
     * providing the new state
     * @param <T> type of stored state
     * @return created stateful runnable
     *
     * @throws NullPointerException if {@code handler} is {@code null}
     */
    public <T> @NotNull Runnable stateful(final T initialState,
                                          final @NonNull Function<? super T, ? extends T> handler) {
        return new StatefulMutableRunnable<>(handler, initialState);
    }

    /**
     * Creates a stateful runnable which stores its state immutably.
     *
     * @param initialState initial state of the created runnable
     * @param handler handler invoked on each call to {@link Runnable#run()} with the state applied to it
     * providing the new state
     * @return created stateful runnable
     *
     * @throws NullPointerException if {@code handler} is {@code null}
     */
    public @NotNull Runnable intStateful(final int initialState,
                                         final @NonNull IntUnaryOperator handler) {
        return new StatefulMutableIntRunnable(handler, initialState);
    }

    /**
     * Creates a stateful runnable which stores its state immutably.
     *
     * @param initialState initial state of the created runnable
     * @param handler handler invoked on each call to {@link Runnable#run()} with the state applied to it
     * providing the new state
     * @return created stateful runnable
     *
     * @throws NullPointerException if {@code handler} is {@code null}
     */
    public @NotNull Runnable longStateful(final long initialState,
                                          final @NonNull LongUnaryOperator handler) {
        return new StatefulMutableLongRunnable(handler, initialState);
    }

    /**
     * Creates a stateful runnable which stores its state immutably.
     *
     * @param initialState initial state of the created runnable
     * @param handler handler invoked on each call to {@link Runnable#run()} with the state applied to it
     * providing the new state
     * @return created stateful runnable
     *
     * @throws NullPointerException if {@code handler} is {@code null}
     */
    public @NotNull Runnable doubleStateful(final double initialState,
                                            final @NonNull DoubleUnaryOperator handler) {
        return new StatefulMutableDoubleRunnable(handler, initialState);
    }

    /**
     * Stateful {@link Runnable runnable} which stores its state immutably.
     *
     * @param <T> type of stored state
     */
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class StatefulImmutableRunnable<T> implements Runnable {

        /**
         * Handler invoked on each call to {@link #run()} with the state applied to it
         */
        @NotNull Consumer<? super T> handler;

        /**
         * State of this runnable
         */
        T state;

        @Override
        public void run() {
            handler.accept(state);
        }
    }

    /**
     * Stateful {@link Runnable runnable} which stores its state mutably.
     *
     * @param <T> type of stored state
     */
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class StatefulMutableRunnable<T> implements Runnable {

        /**
         * Handler invoked on each call to {@link #run()} with the state applied to it providing the new state
         */
        @NotNull Function<? super T, ? extends T> handler;

        /**
         * State of this runnable
         */
        @NonFinal T state;

        @Override
        public void run() {
            state = handler.apply(state);
        }
    }

    /**
     * Specialization of {@link StatefulMutableRunnable} for {@code int}s which stores its state mutably.
     */
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class StatefulMutableIntRunnable implements Runnable {

        /**
         * Handler invoked on each call to {@link #run()} with the state applied to it providing the new state
         */
        @NotNull IntUnaryOperator handler;

        /**
         * State of this runnable
         */
        @NonFinal int state;

        @Override
        public void run() {
            state = handler.applyAsInt(state);
        }
    }

    /**
     * Specialization of {@link StatefulMutableRunnable} for {@code long}s which stores its state mutably.
     */
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class StatefulMutableLongRunnable implements Runnable {

        /**
         * Handler invoked on each call to {@link #run()} with the state applied to it providing the new state
         */
        @NotNull LongUnaryOperator handler;

        /**
         * State of this runnable
         */
        @NonFinal long state;

        @Override
        public void run() {
            state = handler.applyAsLong(state);
        }
    }

    /**
     * Specialization of {@link StatefulMutableRunnable} for {@code double}s which stores its state mutably.
     */
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class StatefulMutableDoubleRunnable implements Runnable {

        /**
         * Handler invoked on each call to {@link #run()} with the state applied to it providing the new state
         */
        @NotNull DoubleUnaryOperator handler;

        /**
         * State of this runnable
         */
        @NonFinal double state;

        @Override
        public void run() {
            state = handler.applyAsDouble(state);
        }
    }
}
