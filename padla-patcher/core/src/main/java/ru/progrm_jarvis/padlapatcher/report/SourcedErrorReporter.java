package ru.progrm_jarvis.padlapatcher.report;

/**
 * {@link ErrorReporter} with source attached to it.
 *
 * @param <E> type of the error
 */
public interface SourcedErrorReporter<E> {

    /**
     * Reports the error.
     *
     * @param error reported error
     */
    void reportError(E error);
}
