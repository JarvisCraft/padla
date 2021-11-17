package ru.progrm_jarvis.javacommons.lazy;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.javacommons.object.ReferenceUtil;
import ru.progrm_jarvis.javacommons.object.Result;
import ru.progrm_jarvis.javacommons.util.UncheckedCasts;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
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
     * @apiNote both {@code null} value and uninitialized state will be represented by {@code null}
     *
     * @see #getAsOptional() analog wraping into {@link Optional}
     */
    @Nullable T getInitializedOrNull();

    /**
     * Gets the wrapped value wrapped in {@link Optional} if it is already initialized
     * or an {@link Optional#empty() empty optional} otherwise.
     *
     * @return wrapped value wrapped in {@link Optional} if it is already initialized
     * or an {@link Optional#empty() empty optional} otherwise
     * @apiNote both {@code null} value and uninitialized state will be represented by {@link Optional#empty()}
     *
     * @see #getInitializedOrNull() behaves similarly but uses raw value instead of {@link Optional}
     */
    default @NotNull Optional<T> getAsOptional() {
        return Optional.ofNullable(getInitializedOrNull());
    }

    /**
     * Gets the wrapped value wrapped in {@link Result} if it is already initialized
     * or a {@link Result#nullError()} otherwise.
     *
     * @return wrapped value wrapped in {@link Result} if it is already initialized
     * or {@link Result#nullError()} otherwise
     */
    @NotNull Result<T, Void> getAsResult();

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
    static <T> Lazy<T> create(final @NonNull Supplier<? extends T> valueSupplier) {
        return new SimpleLazy<>(valueSupplier);
    }

    /**
     * Creates a new thread-safe lazy creating its value using the given supplier.
     *
     * @param valueSupplier supplier of the value to be called once needed
     * @param <T> type of value wrapped
     * @return created lazy
     */
    static <T> Lazy<T> createThreadSafe(final @NonNull Supplier<? extends T> valueSupplier) {
        //noinspection ZeroLengthArrayAllocation: mutex object
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
    static <T> Lazy<@NotNull T> createWeak(final @NonNull Supplier<@NotNull ? extends T> valueSupplier) {
        return new SimpleWeakLazy<>(valueSupplier, ReferenceUtil.weakReferenceToNull());
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
    static <T> Lazy<T> createWeakThreadSafe(final @NonNull Supplier<? extends T> valueSupplier) {
        final ReadWriteLock lock;
        return new LockingWeakLazy<>(
                (lock = new ReentrantReadWriteLock()).readLock(), lock.writeLock(),
                valueSupplier, ReferenceUtil.weakReferenceToNull()
        );
    }

    /**
     * Creates a new thread-local lazy creating its value using the given supplier.
     *
     * @param valueSupplier supplier of the value to be called once needed
     * @param <T> type of value wrapped
     * @return created lazy
     */
    static <T> Lazy<T> createThreadLocal(final @NonNull Supplier<? extends T> valueSupplier) {
        return new ThreadLocalLazy<>(valueSupplier, ThreadLocal.withInitial(() -> ThreadLocalLazy.UNSET_VALUE));
    }

    /**
     * Non-thread-safe lazy getting its value from the specified value supplier.
     *
     * @param <T> type of wrapped value
     */
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    final class SimpleLazy<T> implements Lazy<T> {

        /**
         * Supplier used for creation of the value
         */
        @Nullable Supplier<? extends T> valueSupplier;

        /**
         * The value stored
         */
        T value;

        private SimpleLazy(final @NotNull Supplier<? extends T> valueSupplier) {
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

        @Override
        public @NotNull Result<T, Void> getAsResult() {
            return valueSupplier == null ? Result.success(value) : Result.nullError();
        }
    }

    /**
     * Thread-safe (using double-checked locking) lazy getting its value from the specified value supplier.
     *
     * @param <T> type of wrapped value
     */
    @FieldDefaults(level = AccessLevel.PRIVATE)
    final class DoubleCheckedLazy<T> implements Lazy<T> {

        /**
         * Mutex used for synchronizations
         */
        final @NotNull Object mutex;

        /**
         * Supplier used for creation of the value
         */
        @Nullable volatile Supplier<? extends T> valueSupplier;

        /**
         * The value stored
         */
        volatile T value;

        /**
         * Creat a new double-checked lazy based on the given mutex.
         *
         * @param mutex mutex to be used for synchronization
         * @param valueSupplier supplier used for creation of the value
         */
        private DoubleCheckedLazy(final @NotNull Object mutex, final @NotNull Supplier<? extends T> valueSupplier) {
            this.mutex = mutex;
            this.valueSupplier = valueSupplier;
        }

        @Override
        public T get() {
            if (valueSupplier != null) synchronized (mutex) {
                final Supplier<? extends T> valueSupplier;
                if ((valueSupplier = this.valueSupplier) != null) {
                    val value = this.value = valueSupplier.get();
                    this.valueSupplier = null;

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

        @Override
        public @NotNull Result<T, Void> getAsResult() {
            if (valueSupplier != null) synchronized (mutex) {
                if (valueSupplier != null) return Result.nullError();
            }
            return Result.success(value);
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
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    final class SimpleWeakLazy<@NotNull T> implements Lazy<T> {

        /**
         * Supplier used for creation of the value
         */
        final @NotNull Supplier<@NotNull ? extends T> valueSupplier;

        /**
         * The value stored wrapped in {@link WeakReference}
         */
        @NotNull WeakReference<T> weakValue;

        @Override
        public T get() {
            if (weakValue.get() == null) {
                val value = valueSupplier.get();
                weakValue = new WeakReference<>(value);

                return value;
            }

            return weakValue.get();
        }

        @Override
        public boolean isInitialized() {
            return weakValue.get() != null;
        }

        @Override
        public @Nullable T getInitializedOrNull() {
            return weakValue.get();
        }

        @Override
        public @NotNull Result<T, Void> getAsResult() {
            final T value;
            return (value = weakValue.get()) == null ? Result.nullError() : Result.success(value);
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
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    final class LockingWeakLazy<T> implements Lazy<@NotNull T> {

        /**
         * Mutex used for synchronizations
         */
        @NotNull Lock readLock, writeLock;

        /**
         * Supplier used for creation of the value
         */
        @NotNull Supplier<? extends T> valueSupplier;

        /**
         * The value stored wrapped in {@link WeakReference}
         */
        @NonFinal volatile @NotNull WeakReference<T> weakValue;

        @Override
        public T get() {
            readLock.lock();
            try {
                T value;
                if ((value = weakValue.get()) == null) {
                    writeLock.lock();
                    try {
                        // read to variable `value` so that in case of it not being null it gets returned
                        if ((value = weakValue.get()) == null) weakValue
                                = new WeakReference<>(value = valueSupplier.get());
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
                return weakValue.get() != null;
            } finally {
                readLock.unlock();
            }
        }

        @Override
        public @Nullable T getInitializedOrNull() {
            readLock.lock();
            try {
                return weakValue.get();
            } finally {
                readLock.unlock();
            }
        }

        @Override
        public @NotNull Result<@NotNull T, Void> getAsResult() {
            readLock.lock();
            try {
                final T value;
                return (value = weakValue.get()) == null ? Result.nullError() : Result.success(value);
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
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    class ThreadLocalLazy<T> implements Lazy<T> {

        /**
         * Stub value used as the default value of {@link #threadLocalValue}
         */
        private static final @NotNull Object UNSET_VALUE = new Object[0];

        /**
         * Supplier used for creation of the value
         */
        @NotNull Supplier<? extends T> valueSupplier;

        /**
         * The value stored wrapped in {@link ThreadLocal}
         *
         * @implNote In order to minimize the amount of variables thread-local is typed weakly
         * and contains {@link #UNSET_VALUE} if it is unset
         */
        @SuppressWarnings("ThreadLocalNotStaticFinal") // this is the idea behind this class
        @NotNull ThreadLocal<Object> threadLocalValue;

        @Override
        public T get() {
            Object value;
            if ((value = threadLocalValue.get()) == UNSET_VALUE) threadLocalValue.set(value = valueSupplier.get());

            // value is now known to be of type `T`
            return UncheckedCasts.uncheckedObjectCast(value);
        }

        @Override
        public boolean isInitialized() {
            return threadLocalValue.get() != UNSET_VALUE;
        }

        @Override
        public @Nullable T getInitializedOrNull() {
            final Object value;
            // value is either `UNSET_VALUE` or of type `T`
            return (value = threadLocalValue.get()) == UNSET_VALUE
                    ? null
                    : UncheckedCasts.uncheckedObjectCast(value);
        }

        @Override
        public @NotNull Result<T, Void> getAsResult() {
            final Object value;
            // value is either `UNSET_VALUE` or of type `T`
            return (value = threadLocalValue.get()) == UNSET_VALUE
                    ? Result.nullError()
                    : Result.success(UncheckedCasts.uncheckedObjectCast(value));
        }
    }
}
