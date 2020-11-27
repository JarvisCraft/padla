package ru.progrm_jarvis.javacommons.object;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.lang.ref.WeakReference;

/**
 * Utility for reference-related features.
 */
@UtilityClass
public class ReferenceUtil {

    /**
     * Stub of a {@link WeakReference} actually referencing {@code null}
     */
    private final @NonNull WeakReference<?> WEAK_REFERENCE_STUB = new WeakReference<>(null);

    /**
     * Gets a {@link WeakReference} stub singleton.
     *
     * @param <T> type of value intended to be referenced
     * @return {@link WeakReference} stub singleton
     */
    @SuppressWarnings("unchecked")
    public static <T> WeakReference<T> weakReferenceStub() {
        return (WeakReference<T>) WEAK_REFERENCE_STUB;
    }
}
