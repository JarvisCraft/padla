package ru.progrm_jarvis.javacommons.collection;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * A wrapper for {@link Iterator} to make it treated as a {@link Collection}.
 * It provides lazy access to its entries so that iteration happens only when needed.
 *
 * @param <E> type of element stored
 */
@RequiredArgsConstructor
@ToString(includeFieldNames = false)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class LazyIteratorToCollectionWrapper<E, C extends Collection<E>> implements Collection<E> {

    /**
     * Iterator wrapped
     */
    @EqualsAndHashCode.Include @NonNull Iterator<E> iterator;
    @NonNull C targetCollection;

    /**
     * Gets the next element in the wrapped iterator
     *
     * @return next element in iterator if any or {@code null} if its end was reached.
     */
    @Nullable protected E readNextIteratorElement() {
        if (iterator.hasNext()) return iterator.next();
        return null;
    }

    /**
     * Iterates the wrapped iterator until the specified element is reached.
     *
     * @param element element to try to reach
     * @return found element reference if found and {@code null} false otherwise
     */
    @Nullable protected E readIteratorUntilReached(final E element) {
        if (iterator.hasNext()) {
            while (iterator.hasNext()) {
                val nextElement = iterator.next();
                if (Objects.equals(element, nextElement)) return nextElement;
            }
        }
        return null;
    }

    /**
     * Checks whether or not the wrapped iterator contains the specified element.
     *
     * @param element element to check for containment
     * @return {@code true} if the element is contained in the wrapped iterator and {@code false} otherwise
     */
    protected boolean isIteratorContaining(final Object element) {
        if (iterator.hasNext()) {
            while (iterator.hasNext()) {
                val nextElement = iterator.next();
                if (Objects.equals(element, nextElement)) return true;
            }
        }
        return false;
    }

    /**
     * Reads all content of the wrapped iterator.
     */
    protected void readIteratorFully() {
        while (iterator.hasNext()) targetCollection.add(iterator.next());
    }

    @Override
    public int size() {
        readIteratorFully();

        return targetCollection.size();
    }

    @Override
    public boolean isEmpty() {
        return targetCollection.isEmpty() && !iterator.hasNext();
    }

    @Override
    public boolean contains(final Object object) {
        return targetCollection.contains(object) || isIteratorContaining(object);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return new PublicIterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        readIteratorFully();

        return targetCollection.toArray();
    }

    @NotNull
    @Override
    @SuppressWarnings({"unchecked", "SuspiciousToArrayCall"})
    public <T> T[] toArray(@NotNull final T... a) {
        readIteratorFully();

        return targetCollection.toArray(a);
    }

    @Override
    public boolean add(final E e) {
        return targetCollection.add(e);
    }

    @Override
    public boolean remove(final Object o) {
        if (targetCollection.remove(o)) return true;
        if (isIteratorContaining(o)) {
            iterator.remove();

            return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> valuesToFind) {
        if (valuesToFind.isEmpty()) return true;

        valuesToFind = new ArrayList<>(valuesToFind);
        // remove elements which are definitely contained in the read part of the iterator
        //noinspection SuspiciousMethodCalls
        valuesToFind.removeAll(targetCollection);

        if (valuesToFind.isEmpty()) return true;

        while (iterator.hasNext()) {
            val element = readNextIteratorElement();
            valuesToFind.remove(element);

            if (valuesToFind.isEmpty()) return true;
        }

        return false;
    }

    @Override
    public boolean addAll(@NotNull final Collection<? extends E> elements) {
        return targetCollection.addAll(elements);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> valuesToRemove) {
        if (valuesToRemove.isEmpty()) return false;

        var changed = targetCollection.removeAll(valuesToRemove);
        while (iterator.hasNext()) {
            val element = iterator.next();
            if (valuesToRemove.contains(element)) changed = true;
            else targetCollection.add(element);
        }

        return changed;
    }

    @Override
    public boolean retainAll(@NotNull final Collection<?> valuesToRetain) {
        if (valuesToRetain.isEmpty()) {
            val changed = !targetCollection.isEmpty();
            targetCollection.clear();

            return changed;
        }

        boolean changed = false;
        {
            val targetIterator = targetCollection.iterator();
            while (targetIterator.hasNext()) {
                val element = targetIterator.next();
                if (!valuesToRetain.contains(element)) {
                    targetIterator.remove();
                    changed = true;
                }
            }
        }
        while (iterator.hasNext()) {
            val element = iterator.next();
            if (valuesToRetain.contains(element)) targetCollection.add(element);
            else changed = true;
        }

        return changed;
    }

    @Override
    public void clear() {
        targetCollection.clear();
        while (iterator.hasNext()) iterator.next();
    }

    /**
     * Iterator used by this {@link LazyIteratorToCollectionWrapper}.
     */
    @FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
    protected class PublicIterator implements Iterator<E> {

        /**
         * Iterator of the {@link #targetCollection}
         */
        @NonNull Iterator<E> collectionIterator = targetCollection.iterator();
        /**
         * Flag indicating whether the last {@link Iterator#next()} was called on {@link #collectionIterator} or not
         */
        @NonFinal boolean lastReadFromCollection = false;

        @Override
        public boolean hasNext() {
            return collectionIterator.hasNext() || iterator.hasNext();
        }

        @Override
        public E next() {
            if (collectionIterator.hasNext()) {
                lastReadFromCollection = true;

                return collectionIterator.next();
            }
            if (iterator.hasNext()) {
                lastReadFromCollection = false;

                return iterator.next();
            }

            throw new IllegalStateException();
        }

        @Override
        public void remove() {
            if (lastReadFromCollection) collectionIterator.remove();
            else iterator.remove();
        }
    }
}
