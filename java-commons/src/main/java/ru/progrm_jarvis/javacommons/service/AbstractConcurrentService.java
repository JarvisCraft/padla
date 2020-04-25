package ru.progrm_jarvis.javacommons.service;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

/**
 * Abstract implementation of {@link Service} making its concurrent.
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AbstractConcurrentService implements Service {

    /**
     * Flag indicating whether this service is enabled
     */
    @NonNull AtomicBoolean enabled;

    /**
     * Lock used for synchronizing lifecycle operations
     */
    @NonNull Lock lifecycleLock;

    /**
     * Creates a new abstract concurrent service using the given lock.
     *
     * @param lifecycleLock lock used for synchronizing lifecycle operations
     */
    protected AbstractConcurrentService(final @NonNull Lock lifecycleLock) {
        enabled = new AtomicBoolean();
        this.lifecycleLock = lifecycleLock;
    }

    @Override
    public void enable() {
        final Lock lock;
        (lock = lifecycleLock).lock();
        try {
            if (enabled.compareAndSet(false, true)) onEnable();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Actually enables this service.
     * This should not be manually synchronized as it is called by an already concurrent {@link #enable()}.
     */
    protected abstract void onEnable();

    @Override
    public void disable() {
        final Lock lock;
        (lock = lifecycleLock).lock();
        try {
            if (enabled.compareAndSet(true, false)) onDisable();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Actually disables this service.
     * This should not be manually synchronized as it is called by an already concurrent {@link #disable()} ()}.
     */
    protected abstract void onDisable();
}
