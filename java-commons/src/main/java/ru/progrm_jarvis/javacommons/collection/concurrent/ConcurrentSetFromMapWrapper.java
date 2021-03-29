package ru.progrm_jarvis.javacommons.collection.concurrent;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import lombok.var;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class ConcurrentSetFromMapWrapper<E, W extends Map<E, Boolean>>
        extends AbstractConcurrentSizedCollectionWrapper<W> implements Set<E> {

    @NotNull Set<@NonNull E> keySet;

    protected ConcurrentSetFromMapWrapper(final @NotNull W wrapped,
                                          final @NotNull Set<@NonNull E> keySet,
                                          final @NotNull Lock readLock,
                                          final @NotNull Lock writeLock) {
        super(wrapped, readLock, writeLock);
        this.keySet = keySet;
    }

    public static <E> @NotNull Set<E> create(final @NotNull Map<E, Boolean> wrapped) {
        final ReadWriteLock lock;

        return new ConcurrentSetFromMapWrapper<>(
                wrapped, wrapped.keySet(), (lock = new ReentrantReadWriteLock()).readLock(), lock.writeLock()
        );
    }

    @Override
    protected int internalSize() {
        readLock.lock();
        try {
            return wrapped.size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    protected boolean internalIsEmpty() {
        readLock.lock();
        try {
            return wrapped.isEmpty();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    protected void internalClear() {
        writeLock.lock();
        try {
            wrapped.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean contains(final Object element) {
        readLock.lock();
        try {
            //noinspection SuspiciousMethodCalls
            return wrapped.containsKey(element);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean remove(final Object element) {
        writeLock.lock();
        try {
            return wrapped.remove(element) != null;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public @NotNull Iterator<E> iterator() {
        return keySet.iterator();
    }

    @Override
    public Object @NotNull [] toArray() {
        readLock.lock();
        try {
            return keySet.toArray();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public <T> T @NotNull [] toArray(final T @NotNull [] targetArray) {
        readLock.lock();
        try {
            //noinspection SuspiciousToArrayCall
            return keySet.toArray(targetArray);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean add(final E element) {
        writeLock.lock();
        try {
            return wrapped.put(element, true) == null;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean containsAll(@NotNull final Collection<?> elements) {
        readLock.lock();
        try {
            return keySet.containsAll(elements);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean addAll(final @NotNull Collection<? extends E> elements) {
        @SuppressWarnings("TooBroadScope") var changed = false; // minimize the scope of locking as much as possible
        writeLock.lock();
        try {
            for (val element : elements) changed |= wrapped.put(element, true) == null;
        } finally {
            writeLock.unlock();
        }
        return changed;
    }

    @Override
    public boolean retainAll(@NotNull final Collection<?> elements) {
        writeLock.lock();
        try {
            return keySet.retainAll(elements);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean removeAll(@NotNull final Collection<?> elements) {
        writeLock.lock();
        try {
            return keySet.removeAll(elements);
        } finally {
            writeLock.unlock();
        }
    }
}
