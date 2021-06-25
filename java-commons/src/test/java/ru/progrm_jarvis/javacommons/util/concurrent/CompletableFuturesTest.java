package ru.progrm_jarvis.javacommons.util.concurrent;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.progrm_jarvis.javacommons.object.Pair;
import ru.progrm_jarvis.javacommons.object.Result;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class CompletableFuturesTest {

    static @NotNull Stream<@NotNull Throwable> throwableStream() {
        return Stream.of(
                new Throwable("Great"),
                new AssertionError("Amazing"),
                new RuntimeException("Nice"),
                new Error("Just right")
        );
    }

    static @NotNull Stream<@NotNull Arguments> provideThrowables() {
        return throwableStream().map(Arguments::of);
    }

    static @NotNull Stream<@NotNull Arguments> provideCompletableFuturesBothNotExceptional() {
        return Stream.of(
                arguments(completedFuture("A"), completedFuture(123), Pair.of("A", 123))
        );
    }

    static @NotNull Stream<@NotNull Arguments> provideCompletableFuturesAtLeastOneExceptional() {
        return throwableStream()
                .flatMap(throwable -> Stream.of(
                        arguments(
                                completedFuture("First"),
                                CompletableFutures.exceptionallyCompletedFuture(throwable),
                                throwable
                        ),
                        arguments(
                                CompletableFutures.exceptionallyCompletedFuture(throwable),
                                completedFuture("Second"),
                                throwable
                        ),
                        arguments(
                                CompletableFutures.exceptionallyCompletedFuture(throwable),
                                CompletableFutures.exceptionallyCompletedFuture(throwable),
                                throwable
                        )
                ));
    }

    @ParameterizedTest
    @MethodSource("provideThrowables")
    void exceptionallyCompletedFuture(final @NotNull Throwable throwable) {
        val future = CompletableFutures.exceptionallyCompletedFuture(throwable);

        assertTrue(future.isCompletedExceptionally()); // thus no need for test timeout
        try {
            future.join();
        } catch (final Throwable thrown) {
            assertSame(throwable, thrown.getCause());
            return; // don't fail
        }

        fail("The future completed un-exceptionally");
    }

    @ParameterizedTest
    @MethodSource("provideCompletableFuturesBothNotExceptional")
    <F, S> void ofBoth(final @NotNull CompletableFuture<F> first,
                       final @NotNull CompletableFuture<S> second,
                       final @NotNull Pair<F, S> result) {
        val future = CompletableFutures.bothOf(first, second);

        assertTrue(future.isDone()); // provider guarantee

        assertEquals(result, future.join());
    }

    @ParameterizedTest
    @MethodSource("provideCompletableFuturesAtLeastOneExceptional")
    <F, S> void ofBothExceptionally(final @NotNull CompletableFuture<F> first,
                                    final @NotNull CompletableFuture<S> second,
                                    final @NotNull Throwable throwable) {
        val future = CompletableFutures.bothOf(first, second);

        assertTrue(future.isCompletedExceptionally());

        try {
            future.join();
        } catch (final Throwable thrown) {
            assertSame(throwable, thrown.getCause());
            return; // don't fail
        }

        fail("The future completed un-exceptionally");
    }

    @ParameterizedTest
    @MethodSource("provideCompletableFuturesBothNotExceptional")
    <F, S> void ofEither(final @NotNull CompletableFuture<F> first,
                         final @NotNull CompletableFuture<S> second,
                         final @NotNull Pair<F, S> resultVariants) {
        val future = CompletableFutures.eitherOf(first, second);

        assertTrue(future.isDone()); // provider guarantee

        assertThat(future.join(),
                either(equalTo(Result.<F, S>success(resultVariants.getFirst())))
                        .or(equalTo(Result.<F, S>error(resultVariants.getSecond())))
        );
    }
}