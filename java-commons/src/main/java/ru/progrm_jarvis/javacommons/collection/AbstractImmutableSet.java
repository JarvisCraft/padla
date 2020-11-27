package ru.progrm_jarvis.javacommons.collection;

import lombok.NonNull;
import lombok.val;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Abstract base for custom immutable {@link Set} implementations.
 *
 * @param <E> the type of elements maintained by this set
 */
public abstract class AbstractImmutableSet<E> implements Set<E> {

    /**
     * Message used for {@link UnsupportedOperationException}s thrown on calls to mutating operations.
     */
    private static final String UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE = "This enum set is immutable";

    @Override
    public boolean containsAll(final @NonNull Collection<?> collection) {
        for (val element : collection) if (!collection.contains(element)) return false;
        return true;
    }

    @Override
    public boolean add(final E e) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
    }

    @Override
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
    }

    @Override
    public boolean addAll(final @NonNull Collection<? extends E> collection) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
    }

    @Override
    public boolean retainAll(final @NonNull Collection<?> collection) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
    }

    @Override
    public boolean removeAll(final @NonNull Collection<?> collection) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
    }

    @Override
    public boolean removeIf(final @NonNull Predicate<? super E> filter) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
    }
}
