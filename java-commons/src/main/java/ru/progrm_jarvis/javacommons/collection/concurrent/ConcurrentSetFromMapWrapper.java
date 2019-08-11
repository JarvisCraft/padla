package ru.progrm_jarvis.javacommons.collection.concurrent;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class ConcurrentSetFromMapWrapper<E, T extends Map<E, Boolean>>
        extends ConcurrentWrapper<Set<E>> implements Set<E> {

    // map used as set backend
    T map;

    public ConcurrentSetFromMapWrapper(@NonNull final T wrapped) {
        super(wrapped.keySet());

         this.map = wrapped;
    }

    @Override
    public int size() {
        readLock.lock();
        try {
            return map.size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        readLock.lock();
        try {
            return map.isEmpty();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean contains(final Object o) {
        readLock.lock();
        try {
            //noinspection SuspiciousMethodCalls
            return map.containsKey(o);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    @Nonnull
    public Iterator<E> iterator() {
        readLock.lock();
        try {
            return wrapped.iterator();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void forEach(@NonNull final Consumer<? super E> action) {
        readLock.lock();
        try {
            wrapped.forEach(action);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    @Nonnull public Object[] toArray() {
        readLock.lock();
        try {
            return wrapped.toArray();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    @Nonnull public <R> R[] toArray(@NonNull final R[] a) {
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
            return map.put(e, true) == null;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean remove(final Object o) {
        writeLock.lock();
        try {
            return map.remove(o) != null;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean containsAll(@NonNull final Collection<?> c) {
        readLock.lock();
        try {
            return wrapped.containsAll(c);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean addAll(@NonNull final Collection<? extends E> c) {
        writeLock.lock();
        try {
            return wrapped.addAll(c);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean retainAll(@NonNull final Collection<?> c) {
        writeLock.lock();
        try {
            return wrapped.retainAll(c);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean removeAll(@NonNull final Collection<?> c) {
        writeLock.lock();
        try {
            return wrapped.removeAll(c);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean removeIf(@NonNull final Predicate<? super E> filter) {
        writeLock.lock();
        try {
            return wrapped.removeIf(filter);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            map.clear();
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
}
