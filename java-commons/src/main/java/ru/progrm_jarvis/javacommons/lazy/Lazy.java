package ru.progrm_jarvis.javacommons.lazy;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.javacommons.util.ReferenceUtil;

import java.lang.ref.WeakReference;
import java.util.Optional;
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
     * Gets the wrapped value initializing it once requested.
     *
     * @return wrapped value
     */
    @Override
    T get();

    /**
     * Tests if the value of this lazy was initialized.
     *
     * @return {@code true} if this lazy's value was initialize and {@code false} otherwise
     */
    boolean isInitialized();

    /**
     * Gets the wrapped value if it is already initialized or {@code null} otherwise.
     *
     * @return wrapped value if it is already initialized or {@code null} otherwise
     *
     * @see #getOptionally() behaves similarly but uses {@link Optional} instead of raw value
     */
    @Nullable T getInitializedOrNull();

    /**
     * Gets the wrapped value wrapped in {@link Optional} if it is already initialized
     * or an {@link Optional#empty() empty optional} otherwise.
     *
     * @return wrapped value wrapped in {@link Optional} if it is already initialized
     * or an {@link Optional#empty() empty optional} otherwise.
     *
     * @see #getInitializedOrNull() behaves similarly but uses raw value instead of {@link Optional}
     */
    default @NotNull Optional<T> getOptionally() {
        return Optional.ofNullable(getInitializedOrNull());
    }

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
    static <T> Lazy<T> create(final @NonNull Supplier<T> valueSupplier) {
        return new SimpleLazy<>(valueSupplier);
    }

    /**
     * Creates a new thread-safe lazy creating its value using the given supplier.
     *
     * @param valueSupplier supplier of the value to be called once needed
     * @param <T> type of value wrapped
     * @return created lazy
     */
    static <T> Lazy<T> createThreadSafe(final @NonNull Supplier<T> valueSupplier) {
        return new DoubleCheckedLazy<>(new Object[0], valueSupplier);
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
    static <T> Lazy<@NotNull T> createWeak(final @NonNull Supplier<@NotNull T> valueSupplier) {
        return new SimpleWeakLazy<>(valueSupplier, ReferenceUtil.weakReferenceStub());
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
    static <T> Lazy<T> createWeakThreadSafe(final @NonNull Supplier<T> valueSupplier) {
        return new LockingWeakLazy<>(valueSupplier);
    }

    /**
     * Creates a new thread-local lazy creating its value using the given supplier.
     *
     * @param valueSupplier supplier of the value to be called once needed
     * @param <T> type of value wrapped
     * @return created lazy
     */
    static <T> Lazy<T> createThreadLocal(final @NonNull Supplier<T> valueSupplier) {
        return new ThreadLocalLazy<>(valueSupplier);
    }

    /**
     * Non-thread-safe lazy getting its value from the specified value supplier.
     *
     * @param <T> type of wrapped value
     */
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    final class SimpleLazy<T> implements Lazy<T> {

        /**
         * Supplier used for creation of the value
         */
        @Nullable Supplier<T> valueSupplier;

        /**
         * The value stored
         */
        T value;

        private SimpleLazy(final @NotNull Supplier<T> valueSupplier) {
            this.valueSupplier = valueSupplier;
        }

        @Override
        public T get() {
            val valueSupplier = this.valueSupplier;
            if (valueSupplier != null) {
                value = valueSupplier.get();
                this.valueSupplier = null;
            }

            return value;
        }

        @Override
        public boolean isInitialized() {
            return valueSupplier == null;
        }

        @Override
        public @Nullable T getInitializedOrNull() {
            return valueSupplier == null ? value : null;
        }
    }

    /**
     * Thread-safe (using double-checked locking) lazy getting its value from the specified value supplier.
     *
     * @param <T> type of wrapped value
     */
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    final class DoubleCheckedLazy<T> implements Lazy<T> {

        /**
         * Mutex used for synchronizations
         */
        final @NonNull Object mutex;

        /**
         * Supplier used for creation of the value
         */
        @Nullable volatile Supplier<T> valueSupplier;

        /**
         * The value stored
         */
        volatile T value;

        private DoubleCheckedLazy(final @NotNull Object mutex, final @NotNull Supplier<T> valueSupplier) {
            this.mutex = mutex;
            this.valueSupplier = valueSupplier;
        }

        @Override
        public T get() {
            if (valueSupplier != null) synchronized (mutex) {
                val valueSupplier = this.valueSupplier;
                if (valueSupplier != null) {
                    val value = this.value = valueSupplier.get();
                    this.valueSupplier = null;

                    // make sure no race is possible in theory
                    return value;
                }
            }

            return value;
        }

        @Override
        public boolean isInitialized() {
            if (valueSupplier == null) return true;

            synchronized (mutex) {
                return valueSupplier == null;
            }
        }

        @Override
        public @Nullable T getInitializedOrNull() {
            if (valueSupplier != null) synchronized (mutex) {
                if (valueSupplier != null) return null;
            }
            return value;
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
    @FieldDefaults(level = AccessLevel.PRIVATE)
    final class SimpleWeakLazy<@NotNull T> implements Lazy<T> {

        /**
         * Supplier used for creation of the value
         */
        final @NonNull Supplier<@NotNull T> valueSupplier;

        /**
         * The value stored wrapped in {@link WeakReference}
         */
        @NonNull WeakReference<T> value;

        private SimpleWeakLazy(@NonNull final Supplier<@NotNull T> valueSupplier,
                               @NonNull final WeakReference<T> value) {
            this.valueSupplier = valueSupplier;
            this.value = value;
        }

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

        @Override
        public @Nullable T getInitializedOrNull() {
            return value.get();
        }
    }

    /**
     * Thread-safe (using read-write-lock) weak lazy getting its value from the specified value supplier.
     *
     * @param <T> type of wrapped value
     *
     * @apiNote weak lazy stores the value wrapped in weak reference and so it may be GCed
     * and so the new one might be recomputed using the value supplier
     */
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    final class LockingWeakLazy<@NotNull T> implements Lazy<T> {

        /**
         * Mutex used for synchronizations
         */
        final @NonNull Lock readLock, writeLock;

        /**
         * Supplier used for creation of the value
         */
        final @NonNull Supplier<T> valueSupplier;

        /**
         * The value stored wrapped in {@link WeakReference}
         */
        @NonNull volatile WeakReference<T> value = ReferenceUtil.weakReferenceStub();

        protected LockingWeakLazy(final @NonNull Supplier<T> valueSupplier) {
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

        @Override
        public @Nullable T getInitializedOrNull() {
            readLock.lock();
            try {
                return value.get();
            } finally {
                readLock.unlock();
            }
        }
    }

    /**
     * Lazy which stores its values thread-locally.
     *
     * @param <T> type of wrapped value
     */
    @Value
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) class ThreadLocalLazy<T> implements Lazy<T> {

        /**
         * Stub value used as the default value of {@code #value}
         */
        private static final Object UNSET_VALUE = new Object[0];

        /**
         * Supplier used for creation of the value
         */
        @NonNull Supplier<T> valueSupplier;

        /**
         * The value stored wrapped in {@link ThreadLocal}
         *
         * @implNote In order to minimize the amount of variables thread-local is typed weaker
         * and if is unset returns {@link #UNSET_VALUE}
         */
        @NonNull ThreadLocal<Object> value = ThreadLocal.withInitial(() -> UNSET_VALUE);

        @Override
        public T get() {
            var value = this.value.get();
            if (value == UNSET_VALUE) this.value.set(value = valueSupplier.get());

            //noinspection unchecked: value is weakly typed
            return (T) value;
        }

        @Override
        public boolean isInitialized() {
            return value.get() != UNSET_VALUE;
        }

        @Override
        public @Nullable T getInitializedOrNull() {
            val value = this.value.get();

            //noinspection unchecked: value is weakly typed
            return value == UNSET_VALUE ? null : (T) value;
        }
    }
}
