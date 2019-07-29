package ru.progrm_jarvis.javacommons.collection.concurrent;

import lombok.NonNull;

import java.util.Queue;

public class ConcurrentQueueWrapper<E, T extends Queue<E>>
        extends ConcurrentCollectionWrapper<E, T> implements Queue<E> {

    public ConcurrentQueueWrapper(@NonNull final T wrapped) {
        super(wrapped);
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
