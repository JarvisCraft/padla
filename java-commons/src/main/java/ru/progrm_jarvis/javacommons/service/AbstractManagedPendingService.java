package ru.progrm_jarvis.javacommons.service;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

/**
 * Abstract implementation of {@link ManagedPendingService}.
 *
 * @param <O> type of owner of the service
 * @param <S> type of service owned while pending
 * @param <R> type of value passed to hooks indicating that the service's pending has ended (the service became ready)
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AbstractManagedPendingService<O, S, R> implements ManagedPendingService<O, S, R> {

    /**
     * Current state if this service
     */
    @NonNull AtomicReference<State> state;

    /**
     * Lock used for synchronizing lifecycle operations
     */
    @NonNull Lock lifecycleLock;

    /**
     * Map of owned service instances by their owners
     */
    @NonNull Map<O, OwnedService<S, R>> owners;

    /**
     * Map of callbacks to be called once the server gets started by their owners
     */
    @NonNull Map<O, Set<Consumer<R>>> readyCallbacks;

    /**
     * Creates a new managed pending service.
     *
     * @param lifecycleLock lock used for synchronizing lifecycle operations
     * @param owners empty map of owned service instances by their owners
     * @param readyCallbacks empty map of callbacks to be called once the server gets started by their owners
     *
     * @throws IllegalArgumentException if {@code owners} or {@code readyCallbacks} is not empty
     */
    protected AbstractManagedPendingService(final @NonNull Lock lifecycleLock,
                                            final @NonNull Map<O, OwnedService<S, R>> owners,
                                            final @NonNull Map<O, Set<Consumer<R>>> readyCallbacks) {
        if (!owners.isEmpty()) throw new IllegalArgumentException(
                "Map of owners should be empty"
        );
        if (!readyCallbacks.isEmpty()) throw new IllegalArgumentException(
                "Multi-map of ready callbacks should be empty"
        );

        state = new AtomicReference<>(State.PENDING);

        this.lifecycleLock = lifecycleLock;
        this.owners = owners;
        this.readyCallbacks = readyCallbacks;
    }

    @Override
    public @NotNull OwnedService<S, R> request(final @NonNull O owner) {
        final Lock lock;
        (lock = lifecycleLock).lock();
        try {
            if (state.get().canRequest()) return owners.computeIfAbsent(
                    owner, freshOwner -> new SafeOwnedService(owner, newService(freshOwner))
            );
            throw new IllegalStateException("Services cannot be requested any longer");
        } finally {
            lock.unlock();
        }
    }

    /**
     * Creates a service for the provided owner.
     *
     * @param owner owner of the created service
     * @return creates service
     */
    protected abstract S newService(@NonNull O owner);

    /**
     * Marks the provided owner as ready.
     *
     * @param owner owner which should now be considered ready
     */
    @SuppressWarnings("resource") // close() is already the cause of this method being called
    protected void markAsReady(final @NonNull O owner) {
        final Lock lock;
        (lock = lifecycleLock).lock();
        try {
            if (!state.get().canClose()) throw new IllegalStateException("Owned services can no longer be closed");

            if (owners.remove(owner) != null) tryStart();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Attempts to start this service.
     */
    protected void tryStart() {
        if (owners.isEmpty() && state.compareAndSet(State.READY, State.STARTED)) runReadyCallbacks(start());
    }

    /**
     * Actually tarts this service returning it.
     *
     * @return started service
     */
    protected abstract R start();

    /**
     * Runs all {@link #readyCallbacks ready-callbacks}.
     *
     * @param service service to be passed to callbacks
     */
    protected void runReadyCallbacks(final R service) {
        for (val callbacks : readyCallbacks.values()) for (val callback : callbacks) callback.accept(service);
    }

    /**
     * Adds a ready-callback for the given owner.
     *
     * @param owner owner of the callback
     * @param readyCallback callback to be added to the given owner
     */
    protected void addReadyCallback(final @NonNull O owner, final @NonNull Consumer<R> readyCallback) {
        final Lock lock;
        (lock = lifecycleLock).lock();
        try {
            readyCallbacks.computeIfAbsent(owner, key -> ConcurrentHashMap.newKeySet()).add(readyCallback);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void ready() {
        final Lock lock;
        (lock = lifecycleLock).lock();
        try {
            if (!state.compareAndSet(State.PENDING, State.READY)) throw new IllegalStateException(
                    "Could not set state to READY from PENDING"
            );
            tryStart();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isSafelyStarted() {
        final Lock lock;
        (lock = lifecycleLock).lock();
        try {
            return state.get() == State.STARTED;
        } finally {
            lock.unlock();
        }
    }

    /**
     * States in which this service can be.
     */
    @Getter
    @RequiredArgsConstructor
    @Accessors(fluent = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    protected enum State {
        /**
         * This service accepts new owners.
         */
        PENDING(true, true),
        /**
         * This service is ready to start thus not accepting any new owners.
         */
        READY(false, true),
        /**
         * This service is already started and no longer usable directly.
         */
        STARTED(false, false);

        /**
         * Flag indicating whether new {@link #request(Object) own-requests} can happen
         */
        boolean canRequest,
        /**
         * Flag indicating whether owned services can still be {@link OwnedService#close() closed}
         */
        canClose;
    }

    /**
     * Safe {@link OwnedService owned service} guaranteeing no undefined behaviour due to illegal state.
     */
    @FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
    protected class SafeOwnedService implements OwnedService<S, R> {

        /**
         * Owner of this service
         */
        @NotNull O owner;

        /**
         * Actual owned service
         */
        S service;

        /**
         * Flag indicating whether this service can no longer be used.
         */
        @NotNull AtomicBoolean closed;

        /**
         * Creates a new safe owned service.
         *
         * @param owner owner of this service
         * @param service actual owned service
         */
        protected SafeOwnedService(final @NotNull O owner, final S service) {
            this.owner = owner;
            this.service = service;
            closed = new AtomicBoolean();
        }

        /**
         * Triggers the failure due to this service being closed.
         *
         * @throws IllegalStateException <i>always</i>
         */
        @SuppressWarnings("MethodMayBeStatic") // overriding classes may implement instance-specific behaviour
        protected void failOnClosed() {
            throw new IllegalStateException("This managed service has already been closed");
        }

        @Override
        public @NotNull S service() {
            if (closed.get()) failOnClosed();

            return service;
        }

        @Override
        public void onceReady(final @NonNull Consumer<R> serviceCallback) {
            if (closed.get()) failOnClosed();

            addReadyCallback(owner, serviceCallback);
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) markAsReady(owner);
            else failOnClosed();
        }
    }
}
