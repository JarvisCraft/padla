package ru.progrm_jarvis.javacommons.collection.concurrent;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentQueueWrapper<E, W extends Queue<E>>
        extends ConcurrentCollectionWrapper<E, W> implements Queue<E> {

    protected ConcurrentQueueWrapper(@NotNull final W wrapped,
                                     final @NotNull Lock readLock,
                                     final @NotNull Lock writeLock) {
        super(wrapped, readLock, writeLock);
    }

    public static <E> @NotNull Queue<E> create(final @NonNull Queue<E> wrapped) {
        final ReadWriteLock lock;

        return new ConcurrentQueueWrapper<>(
                wrapped, (lock = new ReentrantReadWriteLock()).readLock(), lock.writeLock()
        );
    }

    @Override
    public boolean offer(final E e) {
        writeLock.lock();
        try {
            return wrapped.offer(e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public E remove() {
        writeLock.lock();
        try {
            return wrapped.remove();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public E poll() {
        writeLock.lock();
        try {
            return wrapped.poll();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public E element() {
        readLock.lock();
        try {
            return wrapped.element();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public E peek() {
        readLock.lock();
        try {
            return wrapped.peek();
        } finally {
            readLock.unlock();
        }
    }
}
