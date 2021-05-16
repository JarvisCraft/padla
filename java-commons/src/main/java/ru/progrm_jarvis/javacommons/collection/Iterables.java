package ru.progrm_jarvis.javacommons.collection;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * Utilities related to {@link Iterable} iterables.
 */
@UtilityClass
public class Iterables {

    /**
     * Converts the given iterable into unmodifiable {@link List}.
     *
     * @param iterable iterable to be converted
     * @param <T> type of the elements
     * @return creates unmodifiable list
     */
    public <T> @NotNull @Unmodifiable List<T> toList(final @NotNull Iterable<T> iterable) {
        final Iterator<T> iterator;
        if (!(iterator = iterable.iterator()).hasNext()) return Collections.emptyList();

        val list = new ArrayList<T>();
        do list.add(iterator.next());
        while (iterator.hasNext());

        return Collections.unmodifiableList(list);
    }

    /**
     * Converts the given iterable into modifiable {@link List}.
     *
     * @param iterable iterable to be converted
     * @param <T> type of the elements
     * @return creates modifiable list
     */
    public <T> @NotNull List<T> toModifiableList(final @NotNull Iterable<T> iterable) {
        final Iterator<T> iterator;
        if (!(iterator = iterable.iterator()).hasNext()) return new ArrayList<>(0);

        val list = new ArrayList<T>();
        do list.add(iterator.next());
        while (iterator.hasNext());

        return list;
    }

    /**
     * Converts the given iterable into unmodifiable {@link Set}.
     *
     * @param iterable iterable to be converted
     * @param <T> type of the elements
     * @return creates unmodifiable set
     */
    public <T> @NotNull @Unmodifiable Set<T> toSet(final @NotNull Iterable<T> iterable) {
        final Iterator<T> iterator;
        if (!(iterator = iterable.iterator()).hasNext()) return Collections.emptySet();

        val set = new HashSet<T>();
        do set.add(iterator.next());
        while (iterator.hasNext());

        return Collections.unmodifiableSet(set);
    }

    /**
     * Converts the given iterable into unmodifiable {@link Set}.
     *
     * @param iterable iterable to be converted
     * @param <T> type of the elements
     * @return creates unmodifiable set
     */
    public <T> @NotNull Set<T> toModifiableSet(final @NotNull Iterable<T> iterable) {
        final Iterator<T> iterator;
        if (!(iterator = iterable.iterator()).hasNext()) return new HashSet<>(0);

        val set = new HashSet<T>();
        do set.add(iterator.next());
        while (iterator.hasNext());

        return Collections.unmodifiableSet(set);
    }
}
