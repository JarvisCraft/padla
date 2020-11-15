package ru.progrm_jarvis.javacommons.collection.concurrent;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentDequeWrapper<E, W extends Deque<E>>
        extends ConcurrentQueueWrapper<E, W> implements Deque<E> {

    protected ConcurrentDequeWrapper(@NotNull final W wrapped,
                                     final @NotNull Lock readLock,
                                     final @NotNull Lock writeLock) {
        super(wrapped, readLock, writeLock);
    }

    public static <E> @NotNull Deque<E> create(final @NonNull Deque<E> wrapped) {
        final ReadWriteLock lock;

        return new ConcurrentDequeWrapper<>(
                wrapped, (lock = new ReentrantReadWriteLock()).readLock(), lock.writeLock()
        );
    }

    @Override
    public void addFirst(final E e) {
        writeLock.lock();
        try {
            wrapped.addFirst(e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void addLast(final E e) {
        writeLock.lock();
        try {
            wrapped.addLast(e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean offerFirst(final E e) {
        writeLock.lock();
        try {
            return wrapped.offerFirst(e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean offerLast(final E e) {
        writeLock.lock();
        try {
            return wrapped.offerLast(e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public E removeFirst() {
        writeLock.lock();
        try {
            return wrapped.removeFirst();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public E removeLast() {
        writeLock.lock();
        try {
            return wrapped.removeLast();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public E pollFirst() {
        writeLock.lock();
        try {
            return wrapped.pollFirst();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public E pollLast() {
        writeLock.lock();
        try {
            return wrapped.pollLast();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public E getFirst() {
        readLock.lock();
        try {
            return wrapped.getFirst();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public E getLast() {
        readLock.lock();
        try {
            return wrapped.getLast();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public E peekFirst() {
        readLock.lock();
        try {
            return wrapped.peekFirst();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public E peekLast() {
        readLock.lock();
        try {
            return wrapped.peekLast();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean removeFirstOccurrence(final Object o) {
        writeLock.lock();
        try {
            return wrapped.removeFirstOccurrence(o);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean removeLastOccurrence(final Object o) {
        writeLock.lock();
        try {
            return wrapped.removeLastOccurrence(o);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void push(final E e) {
        writeLock.lock();
        try {
            wrapped.push(e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public E pop() {
        writeLock.lock();
        try {
            return wrapped.pop();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public @NotNull Iterator<E> descendingIterator() {
        readLock.lock();
        try {
            return wrapped.descendingIterator();
        } finally {
            readLock.unlock();
        }
    }
}
