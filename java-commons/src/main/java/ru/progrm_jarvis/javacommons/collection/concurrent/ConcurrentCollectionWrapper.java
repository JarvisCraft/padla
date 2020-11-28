package ru.progrm_jarvis.javacommons.collection.concurrent;


import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ConcurrentCollectionWrapper<E, W extends Collection<E>>
        extends AbstractConcurrentSizedCollectionWrapper<W> implements Collection<E> {


    protected ConcurrentCollectionWrapper(final @NotNull W wrapped,
                                          final @NotNull Lock readLock,
                                          final @NotNull Lock writeLock) {
        super(wrapped, readLock, writeLock);
    }

    public static <E> @NotNull Collection<E> create(final @NonNull Collection<E> wrapped) {
        final ReadWriteLock lock;

        return new ConcurrentCollectionWrapper<>(
                wrapped, (lock = new ReentrantReadWriteLock()).readLock(), lock.writeLock()
        );
    }

    @Override
    protected int internalSize() {
        return wrapped.size();
    }

    @Override
    protected boolean internalIsEmpty() {
        return wrapped.isEmpty();
    }

    @Override
    protected void internalClear() {
        wrapped.clear();
    }

    @Override
    public boolean contains(final Object o) {
        readLock.lock();
        try {
            return wrapped.contains(o);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public @NotNull Iterator<E> iterator() {
        readLock.lock();
        try {
            return wrapped.iterator();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public @NotNull Object[] toArray() {
        readLock.lock();
        try {
            return wrapped.toArray();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public <R> @NotNull R[] toArray(final @NonNull R[] a) {
        readLock.lock();
        try {
            //noinspection SuspiciousToArrayCall
            return wrapped.toArray(a);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean add(final E e) {
        writeLock.lock();
        try {
            return wrapped.add(e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean remove(final Object o) {
        writeLock.lock();
        try {
            return wrapped.remove(o);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean containsAll(final @NonNull Collection<?> elements) {
        readLock.lock();
        try {
            return wrapped.containsAll(elements);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean addAll(final @NonNull Collection<? extends E> elements) {
        writeLock.lock();
        try {
            return wrapped.addAll(elements);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean removeAll(final @NonNull Collection<?> elements) {
        writeLock.lock();
        try {
            return wrapped.removeAll(elements);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean removeIf(final @NonNull Predicate<? super E> filter) {
        writeLock.lock();
        try {
            return wrapped.removeIf(filter);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean retainAll(final @NonNull Collection<?> elements) {
        writeLock.lock();
        try {
            return wrapped.retainAll(elements);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Spliterator<E> spliterator() {
        readLock.lock();
        try {
            return wrapped.spliterator();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Stream<E> stream() {
        readLock.lock();
        try {
            return wrapped.stream();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Stream<E> parallelStream() {
        readLock.lock();
        try {
            return wrapped.parallelStream();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void forEach(final @NonNull Consumer<? super E> action) {
        readLock.lock();
        try {
            wrapped.forEach(action);
        } finally {
            readLock.unlock();
        }
    }
}
