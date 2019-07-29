package ru.progrm_jarvis.javacommons.collection.concurrent;

import lombok.NonNull;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.UnaryOperator;

public class ConcurrentListWrapper<E, T extends List<E>> extends ConcurrentCollectionWrapper<E, T> implements List<E> {

    public ConcurrentListWrapper(@NonNull final T wrapped) {
        super(wrapped);
    }

    @Override
    public boolean addAll(final int index, @Nonnull final Collection<? extends E> c) {
        if (index < 0) throw new IndexOutOfBoundsException("index should be positive");

        writeLock.lock();
        try {
            return wrapped.addAll(index, c);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void replaceAll(@Nonnull final UnaryOperator<E> operator) {
        writeLock.lock();
        try {
            wrapped.replaceAll(operator);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void sort(@Nonnull final Comparator<? super E> c) {
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
    @Nonnull
    public ListIterator<E> listIterator() {
        readLock.lock();
        try {
            return wrapped.listIterator();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    @Nonnull public ListIterator<E> listIterator(final int index) {
        if (index < 0) throw new IndexOutOfBoundsException("index should be positive");

        readLock.lock();
        try {
            return wrapped.listIterator(index);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    @Nonnull public List<E> subList(final int fromIndex, final int toIndex) {
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
