package ru.progrm_jarvis.javacommons.collection.concurrent;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * An utility for creating concurrent {@link Collection} wrappers.
 *
 * @implNote concurrent wrappers delegate all operations to elements collections
 * yet performing precondition-checks ans using {@link ReadWriteLock}s
 */
@UtilityClass
public class ConcurrentCollections {

    public <E> Collection<E> concurrentCollection(final @NonNull Collection<E> collection) {
        return ConcurrentCollectionWrapper.create(collection);
    }

    public <E> List<E> concurrentList(final @NonNull List<E> list) {
        return ConcurrentListWrapper.create(list);
    }

    public <E> Set<E> concurrentSet(final @NonNull Set<E> set) {
        return ConcurrentSetWrapper.create(set);
    }

    public <E> Set<E> concurrentSetFromMap(final @NonNull Map<E, Boolean> map) {
        return ConcurrentSetWrapper.create(map.keySet());
    }

    public <E> Queue<E> concurrentQueue(final @NonNull Queue<E> set) {
        return ConcurrentQueueWrapper.create(set);
    }

    public <E> Deque<E> concurrentDeque(final @NonNull Deque<E> set) {
        return ConcurrentDequeWrapper.create(set);
    }

    public <K, V> Map<K, V> concurrentMap(final @NonNull Map<K, V> map) {
        return ConcurrentMapWrapper.create(map);
    }
}
