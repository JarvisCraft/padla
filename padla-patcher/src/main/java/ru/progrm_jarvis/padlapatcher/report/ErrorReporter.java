package ru.progrm_jarvis.padlapatcher.report;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * Reporter of errors.
 *
 * @param <S> type of source in which an error occurred
 * @param <E> type of reported error
 */
public interface ErrorReporter<S, E> {

    /**
     * Reports an error in the specified source.
     *
     * @param error error source
     */
    void reportError(S source, E error);

    /**
     * Creates a {@link  SourcedErrorReporter sourced error reporter} from this one.
     *
     * @param source source type
     * @return created sourced error reporter
     */
    @NotNull SourcedErrorReporter<E> sourced(S source);

    /**
     * Aggregates the errors which were reported by the current time.
     *
     * @return stream of aggregated errors
     */
    @NotNull Stream<@NotNull ReportedError<S, E>> aggregateErrors();

    /**
     * A reported error.
     *
     * @param <S> the type of source of the reported error
     * @param <E> the type of the reported errors
     */
    interface ReportedError<S, E> {
        S source();
        E error();
    }
}
