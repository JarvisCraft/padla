package ru.progrm_jarvis.padlapatcher.report;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * Simple {@link ErrorReporter}.
 *
 * @param <S> type of source in which an error occurred
 * @param <E> type of reported error
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class SimpleErrorReporter<S, E> implements ErrorReporter<S, E> {

    /**
     * List of reported errors
     */
    @NotNull List<@NotNull ReportedError<S, E>> errors;

    /**
     * Creates a new simple concurrent error reporter.
     *
     * @param <S> type of source in which an error occurred
     * @param <E> type of reported error
     * @return created error reporter
     */
    public static <S, E> @NotNull ErrorReporter<S, E> create() {
        return new SimpleErrorReporter<>(new CopyOnWriteArrayList<>());
    }

    @Override
    public void reportError(final S source, final E error) {
        errors.add(new SimpleReportedError<>(source, error));
    }

    @Override
    public @NotNull Stream<@NotNull ReportedError<S, E>> aggregateErrors() {
        return errors.stream();
    }

    @Override
    public @NotNull SourcedErrorReporter<E> sourced(final S source) {
        return new SimpleSourcedErrorReporter(source);
    }

    @Value
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class SimpleReportedError<S, E> implements ReportedError<S, E> {
        S source;
        E error;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private final class SimpleSourcedErrorReporter implements SourcedErrorReporter<E> {

        @NotNull S source;

        @Override
        public void reportError(final E error) {
            SimpleErrorReporter.this.reportError(source, error);
        }
    }
}
