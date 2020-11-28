package ru.progrm_jarvis.javacommons.service;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A service which gives per-owner access to its owned service.
 * This allows implementing hooks called whenever all services are ready.
 *
 * @param <O> type of owner of the service
 * @param <S> type of service owned while pending
 * @param <R> type of value passed to hooks indicating that the service's pending has ended (the service became ready)
 */
@FunctionalInterface
public interface PendingService<O, S, R> {

    /**
     * Gets an instance of a pending service which <b>must</b> be closed once the owner is ready with it.
     *
     * @param owner owner requesting the service
     * @return owned service which <b>must</b> be closed once the owner is ready with it
     */
    @NotNull OwnedService<S, R> request(@NonNull O owner);

    /**
     * An owned service which <b>must</b> be closed once the owner is ready with it.
     *
     * @param <S> type of service owned
     * @param <R> type of value passed to the hook
     * indicating that the service's pending has ended (the service became ready)
     */
    interface OwnedService<S, R> extends AutoCloseable {

        /**
         * Service accessible until this instance gets closed.
         *
         * @return actual owned service
         */
        S service();

        /**
         * Specifies a callback to be called once the service's pending has ended (the service became ready).
         *
         * @param serviceCallback callback to be called when the service becomes ready
         */
        void onceReady(@NonNull Consumer<R> serviceCallback);

        @Override
        void close();
    }
}
