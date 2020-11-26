package ru.progrm_jarvis.javacommons.collection.concurrent;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.UnaryOperator;

public class ConcurrentListWrapper<E, W extends List<E>> extends ConcurrentCollectionWrapper<E, W> implements List<E> {

    protected ConcurrentListWrapper(final @NotNull W wrapped,
                                    final @NotNull Lock readLock,
                                    final @NotNull Lock writeLock) {
        super(wrapped, readLock, writeLock);
    }

    public static <E> @NotNull List<E> create(final @NonNull List<E> wrapped) {
        final ReadWriteLock lock;

        return new ConcurrentListWrapper<>(
                wrapped, (lock = new ReentrantReadWriteLock()).readLock(), lock.writeLock()
        );
    }

    @Override
    public boolean addAll(final int index, final @NonNull Collection<? extends E> elements) {
        if (index < 0) throw new IndexOutOfBoundsException("index should be positive");

        writeLock.lock();
        try {
            return wrapped.addAll(index, elements);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void replaceAll(final @NonNull UnaryOperator<E> operator) {
        writeLock.lock();
        try {
            wrapped.replaceAll(operator);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void sort(final @NonNull Comparator<? super E> c) {
        writeLock.lock();
        try {
            wrapped.sort(c);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public E get(final int index) {
        if (index < 0) throw new IndexOutOfBoundsException("index should be positive");

        readLock.lock();
        try {
            return wrapped.get(index);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public E set(final int index, final E element) {
        if (index < 0) throw new IndexOutOfBoundsException("index should be positive");

        writeLock.lock();
        try {
            return wrapped.set(index, element);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void add(final int index, final E element) {
        if (index < 0) throw new IndexOutOfBoundsException("index should be positive");

        writeLock.lock();
        try {
            wrapped.add(index, element);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public E remove(final int index) {
        if (index < 0) throw new IndexOutOfBoundsException("index should be positive");

        writeLock.lock();
        try {
            return wrapped.remove(index);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public int indexOf(final Object o) {
        readLock.lock();
        try {
            return wrapped.indexOf(o);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int lastIndexOf(final Object o) {
        readLock.lock();
        try {
            return wrapped.lastIndexOf(o);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public@Nonnull
     ListIterator<E> listIterator() {
        readLock.lock();
        try {
            return wrapped.listIterator();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public @NotNull ListIterator<E> listIterator(final int index) {
        if (index < 0) throw new IndexOutOfBoundsException("index should be positive");

        readLock.lock();
        try {
            return wrapped.listIterator(index);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public @NotNull List<E> subList(final int fromIndex, final int toIndex) {
        if (fromIndex < 0) throw new IndexOutOfBoundsException("fromIndex should be positive");
        if (toIndex < fromIndex) throw new IndexOutOfBoundsException(
                "toIndex should be greater than or equal to fromIndex"
        );

        readLock.lock();
        try {
            return wrapped.subList(fromIndex, toIndex);
        } finally {
            readLock.unlock();
        }
    }
}
