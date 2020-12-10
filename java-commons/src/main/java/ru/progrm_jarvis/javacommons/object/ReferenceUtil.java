package ru.progrm_jarvis.javacommons.object;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

/**
 * Utility for reference-related features.
 */
@UtilityClass
public class ReferenceUtil {

    /**
     * Singleton of {@link WeakReference} to {@code null}
     */
    private final @NotNull WeakReference<@Nullable ?> WEAK_REFERENCE_TO_NULL = new WeakReference<>(null);

    /**
     * Gets a {@link WeakReference} stub singleton.
     *
     * @param <T> type of value intended to be referenced
     * @return {@link WeakReference} stub singleton
     */
    @SuppressWarnings("unchecked")
    public static <@Nullable T> @NotNull WeakReference<T> weakReferenceToNull() {
        return (WeakReference<T>) WEAK_REFERENCE_TO_NULL;
    }
}
