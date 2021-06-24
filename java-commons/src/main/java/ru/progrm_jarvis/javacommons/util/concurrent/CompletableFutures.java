package ru.progrm_jarvis.javacommons.util.concurrent;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.object.Pair;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utilities related to {@link CompletableFuture completable futures}.
 */
@UtilityClass
public class CompletableFutures {

    /**
     * Returns the completable future whose result is a pair of the provided future's results if both of them complete.
     * The future completes exceptionally if any of the given futures completes exceptionally.
     *
     * @param first first futures
     * @param second second futures
     * @param <F> the result type of the first future
     * @param <S> the result type of the second future
     * @return a new completable future that is completed when both of the given futures complete
     *
     * @see CompletableFuture#allOf(CompletableFuture[]) var-arg untyped equivalent
     */
    public @NotNull <F, S> CompletableFuture<@NotNull Pair<F, S>> bothOf(final @NonNull CompletableFuture<F> first,
                                                                         final @NonNull CompletableFuture<S> second) {
        val firstResult = new AtomicReference<F>();
        val secondResult = new AtomicReference<S>();

        return CompletableFuture.allOf(first.thenAccept(firstResult::set), second.thenAccept(secondResult::set))
                .thenApply(unused -> Pair.of(firstResult.get(), secondResult.get()));
    }
}
