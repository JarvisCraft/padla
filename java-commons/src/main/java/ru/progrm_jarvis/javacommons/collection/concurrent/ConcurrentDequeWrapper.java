package ru.progrm_jarvis.javacommons.collection.concurrent;

import lombok.NonNull;

import javax.annotation.Nonnull;
import java.util.Deque;
import java.util.Iterator;

public class ConcurrentDequeWrapper<E, T extends Deque<E>>
        extends ConcurrentCollectionWrapper<E, T> implements Deque<E> {

    public ConcurrentDequeWrapper(@NonNull final T wrapped) {
        super(wrapped);
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
    @Nonnull public Iterator<E> descendingIterator() {
        readLock.lock();
        try {
            return wrapped.descendingIterator();
        } finally {
            readLock.unlock();
        }
    }
}
