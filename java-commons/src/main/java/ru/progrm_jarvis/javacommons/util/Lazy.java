package ru.progrm_jarvis.javacommons.util;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * A wrapper for a value initialized once needed.
 *
 * @param <T> type of wrapped value
 */
public interface Lazy<T> extends Supplier<T> {

    /**
     * Gets the value wrapped initializing it once requested.
     *
     * @return value wrapped by this lazy
     */
    @Override
    T get();

    /**
     * Tests if the value of this lazy was initialized.
     *
     * @return {@code true} if this lazy's value was initialize and {@code false} otherwise
     */
    boolean isInitialized();

    ///////////////////////////////////////////////////////////////////////////
    // Static factories
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Creates a new lazy creating its value using the given supplier.
     *
     * @param valueSupplier supplier of the value to be called once needed
     * @param <T> type of value wrapped
     * @return created lazy
     *
     * @apiNote might be thread-unsafe
     */
    static <T> Lazy<T> create(@NonNull final Supplier<T> valueSupplier) {
        return new SimpleLazy<>(valueSupplier);
    }

    /**
     * Creates a new thread-safe lazy creating its value using the given supplier.
     *
     * @param valueSupplier supplier of the value to be called once needed
     * @param <T> type of value wrapped
     * @return created lazy
     */
    static <T> Lazy<T> createThreadSafe(@NonNull final Supplier<T> valueSupplier) {
        return new DoubleCheckedLazy<>(valueSupplier);
    }

    /**
     * Creates a new weak lazy creating its value using the given supplier.
     *
     * @param valueSupplier supplier of the value to be called once needed
     * @param <T> type of value wrapped
     * @return created lazy
     *
     * @apiNote might be thread-unsafe
     * @apiNote weak lazy stores the value wrapped in weak reference and so it may be GCed
     * and so the new one might be recomputed using the value supplier
     */
    static <T> Lazy<T> createWeak(@NonNull final Supplier<T> valueSupplier) {
        return new SimpleWeakLazy<>(valueSupplier);
    }

    /**
     * Creates a new weak thread-safe lazy creating its value using the given supplier.
     *
     * @param valueSupplier supplier of the value to be called once needed
     * @param <T> type of value wrapped
     * @return created lazy
     *
     * @apiNote weak lazy stores the value wrapped in weak reference and so it may be GCed
     * and so the new one might be recomputed using the value supplier
     */
    static <T> Lazy<T> createWeakThreadSafe(@NonNull final Supplier<T> valueSupplier) {
        return new DoubleCheckedWeakLazy<>(valueSupplier);
    }

    /**
     * Non-thread-safe (using double-checked locking) lazy getting its value from the specified value supplier.
     *
     * @param <T> type of wrapped value
     */
    @Data
    @FieldDefaults(level = AccessLevel.PROTECTED)
    class SimpleLazy<T> implements Lazy<T> {

        /**
         * Supplier used for creation of the value
         */
        @NonNull Supplier<T> valueSupplier;

        /**
         * The value stored
         */
        T value;

        @Override
        public T get() {
            if (valueSupplier != null) {
                value = valueSupplier.get();
                valueSupplier = null;
            }

            return value;
        }

        @Override
        public boolean isInitialized() {
            return valueSupplier != null;
        }
    }

    /**
     * Thread-safe (using double-checked locking) lazy getting its value from the specified value supplier.
     *
     * @param <T> type of wrapped value
     */
    @Data
    @FieldDefaults(level = AccessLevel.PROTECTED)
    class DoubleCheckedLazy<T> implements Lazy<T> {

        /**
         * Mutex used for synchronizations
         */
        @NonNull final Object mutex = new Object[0];

        /**
         * Supplier used for creation of the value
         */
        @NonNull volatile Supplier<T> valueSupplier;

        /**
         * The value stored
         */
        volatile T value;

        @Override
        public T get() {
            if (valueSupplier != null) synchronized (mutex) {
                if (valueSupplier != null) {
                    value = valueSupplier.get();
                    valueSupplier = null;
                }
            }

            return value;
        }

        @Override
        public boolean isInitialized() {
            if (valueSupplier != null) return true;

            synchronized (mutex) {
                return valueSupplier != null;
            }
        }
    }

    /**
     * Non-thread-safe (using double-checked locking) weak lazy getting its value from the specified value supplier.
     *
     * @param <T> type of wrapped value
     *
     * @apiNote weak lazy stores the value wrapped in weak reference and so it may be GCed
     * and so the new one might be recomputed using the value supplier
     */
    @Data
    @FieldDefaults(level = AccessLevel.PROTECTED)
    class SimpleWeakLazy<@NotNull T> implements Lazy<T> {

        /**
         * Supplier used for creation of the value
         */
        @NonNull final Supplier<T> valueSupplier;

        /**
         * The value stored wrapped in {@link WeakReference}
         */
        @NonNull WeakReference<T> value = ReferenceUtil.weakReerenceStub();

        @Override
        public T get() {
            if (value.get() == null) {
                val value = valueSupplier.get();
                this.value = new WeakReference<>(value);

                return value;
            }

            return value.get();
        }

        @Override
        public boolean isInitialized() {
            return value.get() != null;
        }
    }

    /**
     * Thread-safe (using double-checked locking) weak lazy getting its value from the specified value supplier.
     *
     * @param <T> type of wrapped value
     *
     * @apiNote weak lazy stores the value wrapped in weak reference and so it may be GCed
     * and so the new one might be recomputed using the value supplier
     */
    @Data
    @FieldDefaults(level = AccessLevel.PROTECTED)
    class DoubleCheckedWeakLazy<@NotNull T> implements Lazy<T> {

        /**
         * Mutex used for synchronizations
         */
        @NonNull final Lock readLock, writeLock;

        /**
         * Supplier used for creation of the value
         */
        @NonNull final Supplier<T> valueSupplier;

        /**
         * The value stored wrapped in {@link WeakReference}
         */
        @NonNull volatile WeakReference<T> value = ReferenceUtil.weakReerenceStub();

        public DoubleCheckedWeakLazy(@NonNull final Supplier<T> valueSupplier) {
            this.valueSupplier = valueSupplier;

            {
                val lock = new ReentrantReadWriteLock();
                readLock = lock.readLock();
                writeLock = lock.writeLock();
            }
        }

        @Override
        public T get() {
            readLock.lock();
            try {
                var value = this.value.get();
                if (value == null) {
                    writeLock.lock();
                    try {
                        this.value = new WeakReference<>(value = valueSupplier.get());
                    } finally {
                        writeLock.unlock();
                    }
                }

                return value;
            } finally {
                readLock.unlock();
            }
        }

        @Override
        public boolean isInitialized() {
            readLock.lock();
            try {
                return value.get() != null;
            } finally {
                readLock.unlock();
            }
        }
    }
}
