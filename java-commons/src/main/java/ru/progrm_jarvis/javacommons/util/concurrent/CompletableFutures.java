package ru.progrm_jarvis.javacommons.util.concurrent;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.annotation.Any;
import ru.progrm_jarvis.javacommons.object.Pair;
import ru.progrm_jarvis.javacommons.object.Result;

import java.util.concurrent.CompletableFuture;

/**
 * Utilities related to {@link CompletableFuture completable futures}.
 */
@UtilityClass
public class CompletableFutures {

    /**
     * Creates a completable future which instantly
     * {@link CompletableFuture#completeExceptionally(Throwable) completes exceptionally}
     * with the provided throwable.
     *
     * @param throwable throwable with which the future completes exceptionally
     * @param <T> the result type of the future
     * @return the exceptionally completed future
     *
     * @throws NullPointerException if {@code throwable} is {@code null}
     */
    public static <@Any T> @NotNull CompletableFuture<T> exceptionallyCompletedFuture(
            final @NonNull Throwable throwable
    ) {
        final CompletableFuture<T> future;
        (future = new CompletableFuture<>()).completeExceptionally(throwable);

        return future;
    }

    /**
     * Returns the completable future whose result is a pair of the provided future's results if both of them complete.
     * The future completes exceptionally if any of the given futures completes exceptionally.
     *
     * @param first first future
     * @param second second future
     * @param <F> the result type of the first future
     * @param <S> the result type of the second future
     * @return a new completable future that is completed when both of the given futures complete
     *
     * @throws NullPointerException if {@code first} is {@code null}
     * @throws NullPointerException if {@code second} is {@code null}
     * @see CompletableFuture#allOf(CompletableFuture[]) var-arg untyped equivalent
     */
    public @NotNull <F, S> CompletableFuture<@NotNull Pair<F, S>> bothOf(
            final @NonNull CompletableFuture<F> first,
            @SuppressWarnings("TypeMayBeWeakened" /* for API stability */) final @NonNull CompletableFuture<S> second
    ) {
        return first.thenCombine(second, Pair::of);
    }

    /**
     * Returns the completable future whose result is the result of either the first or the second future
     * wrapped in either a {@link Result#success(Object) successful result}
     * or an {@link Result#error(Object) error result} respectively.
     * The future completes exceptionally if the first completing future completes exceptionally.
     *
     * @param successFuture first futures
     * @param errorFuture second futures
     * @param <F> the result type of the first future
     * @param <S> the result type of the second future
     * @return a new completable future that is completed when either of the given futures completes
     *
     * @throws NullPointerException if {@code successFuture} is {@code null}
     * @throws NullPointerException if {@code errorFuture} is {@code null}
     * @see CompletableFuture#anyOf(CompletableFuture[]) var-arg untyped equivalent
     */
    public @NotNull <F, S> CompletableFuture<@NotNull Result<F, S>> eitherOf(
            final @NonNull CompletableFuture<F> successFuture,
            final @NonNull CompletableFuture<S> errorFuture
    ) {
        // the `success` and `error` results don't overlap thus it is safe to perform the cast
        return uncheckedCompletableFutureCast(CompletableFuture.anyOf(
                successFuture.thenApply(Result::success),
                errorFuture.thenApply(Result::error)
        ));
    }

    /**
     * Casts the given completable future into the specific one.
     *
     * @param future raw-typed completable future
     * @param <T> exact wanted type of completable future
     * @return the provided completable future with its type cast to the specific one
     *
     * @apiNote this is effectively no-op
     */
    // note: no nullability annotations are present on parameter and return type as cast of `null` is also safe
    @Contract("_ -> param1")
    @SuppressWarnings("unchecked")
    private static <T> CompletableFuture<T> uncheckedCompletableFutureCast(final CompletableFuture<?> future) {
        return (CompletableFuture<T>) future;
    }
}
