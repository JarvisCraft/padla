package ru.progrm_jarvis.javacommons.collection.concurrent;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentSetWrapper<E, W extends Set<E>>
        extends ConcurrentCollectionWrapper<E, W> implements Set<E> {
    protected ConcurrentSetWrapper(@NotNull final W wrapped,
                                   final @NotNull Lock readLock,
                                   final @NotNull Lock writeLock) {
        super(wrapped, readLock, writeLock);
    }

    public static <E> @NotNull Set<E> create(final @NonNull Set<E> wrapped) {
        final ReadWriteLock lock;

        return new ConcurrentSetWrapper<>(
                wrapped, (lock = new ReentrantReadWriteLock()).readLock(), lock.writeLock()
        );
    }
}
