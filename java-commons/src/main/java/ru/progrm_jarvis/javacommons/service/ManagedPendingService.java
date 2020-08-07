package ru.progrm_jarvis.javacommons.service;

/**
 * A managed {@link PendingService pending service}.
 *
 * @param <O> type of owner of the service
 * @param <S> type of service owned while pending
 * @param <R> type of value passed to hooks indicating that the service's pending has ended (the service became ready)
 */
public interface ManagedPendingService<O, S, R> extends PendingService<O, S, R> {

    /**
     * Marks this service as ready for starting.
     */
    void ready();

    /**
     * Checks if this service has been successfully started.
     *
     * @return {@code true} if this service has been successfully started and {@code false} otherwise
     */
    boolean isSafelyStarted();
}
