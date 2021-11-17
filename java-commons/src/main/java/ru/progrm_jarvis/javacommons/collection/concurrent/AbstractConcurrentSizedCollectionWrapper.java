package ru.progrm_jarvis.javacommons.collection.concurrent;

import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.collection.SizedCollection;

import java.util.concurrent.locks.Lock;

public abstract class AbstractConcurrentSizedCollectionWrapper<W>
        extends ConcurrentWrapper<W> implements SizedCollection {

    protected AbstractConcurrentSizedCollectionWrapper(final @NotNull W wrapped,
                                                       final @NotNull Lock readLock,
                                                       final @NotNull Lock writeLock) {
        super(wrapped, readLock, writeLock);
    }

    @Override
    public int size() {
        readLock.lock();
        try {
            return internalSize();
        } finally {
            readLock.unlock();
        }
    }

    protected abstract int internalSize();

    @Override
    public boolean isEmpty() {
        readLock.lock();
        try {
            return internalIsEmpty();
        } finally {
            readLock.unlock();
        }
    }

    protected abstract boolean internalIsEmpty();

    @Override
    public void clear() {
        writeLock.lock();
        try {
            internalClear();
        } finally {
            writeLock.unlock();
        }
    }

    protected abstract void internalClear();
}
