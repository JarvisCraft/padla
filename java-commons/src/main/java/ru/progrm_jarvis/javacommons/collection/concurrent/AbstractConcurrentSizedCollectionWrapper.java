package ru.progrm_jarvis.javacommons.collection.concurrent;


import lombok.NonNull;
import ru.progrm_jarvis.javacommons.collection.SizedCollection;

public abstract class AbstractConcurrentSizedCollectionWrapper<T>
        extends ConcurrentWrapper<T> implements SizedCollection {

    public AbstractConcurrentSizedCollectionWrapper(@NonNull final T wrapped) {
        super(wrapped);
    }

    protected abstract int internalSize();

    protected abstract boolean internalIsEmpty();

    protected abstract void internalClear();

    @Override
    public int size() {
        readLock.lock();
        try {
            return internalSize();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        readLock.lock();
        try {
            return internalIsEmpty();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            internalClear();
        } finally {
            writeLock.unlock();
        }
    }
}
