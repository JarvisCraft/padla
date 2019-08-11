package ru.progrm_jarvis.javacommons.collection.concurrent;


import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Base for all concurrent wrappers.
 *
 * @param <T> type of wrapped value
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public class ConcurrentWrapper<T> {

    @NonNull T wrapped;

    ReadWriteLock lock = new ReentrantReadWriteLock();
    Lock readLock = lock.readLock();
    Lock writeLock = lock.writeLock();

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
        return wrapped.equals(obj);
    }

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
     * @implNote simply adds <i>Concurrent</i> prefix to {@link #wrapped} {@link Object#toString()} call result
     */
    @Override
    public String toString() {
        return "Concurrent" + wrapped.toString();
    }
}
