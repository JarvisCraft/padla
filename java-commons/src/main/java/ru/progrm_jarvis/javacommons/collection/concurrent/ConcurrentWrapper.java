package ru.progrm_jarvis.javacommons.collection.concurrent;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.Lock;

/**
 * Base for all concurrent wrappers.
 *
 * @param <W> type of wrapped value
 */
@ToString(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class ConcurrentWrapper<W> {

    @ToString.Include
    @NotNull W wrapped;
    @NotNull Lock readLock, writeLock;

    /**
     * {@inheritDoc}
     *
     * @implNote this method is not concurrent because if modification happens
     * then the result of its call is anyway irrelevant
     * @implNote simply calls to {@link #wrapped}'s {@link Object#hashCode()} method
     * as it provides a logically unique value
     */
    @Override
    public int hashCode() {
        return wrapped.hashCode();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote this method is not concurrent because if modification happens
     * then the result of its call is anyway irrelevant
     * @implNote simply calls to {@link #wrapped}'s {@link Object#equals(Object)} method
     * as it provides mostly symmetric logic
     */
    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(final Object obj) {
        return this == obj || wrapped.equals(obj);
    }
}
