package ru.progrm_jarvis.javacommons.collection.concurrent;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * An utility for creating concurrent {@link Collection} wrappers.
 *
 * @implNote concurrent wrappers delegate all operations to source collections
 * yet performing precondition-checks ans using {@link ReadWriteLock}s
 */
@UtilityClass
public class ConcurrentCollections {

    public <E> Collection<E> concurrentCollection(@NonNull final Collection<E> collection) {
        return new ConcurrentCollectionWrapper<>(collection);
    }

    public <E> List<E> concurrentList(@NonNull final List<E> list) {
        return new ConcurrentListWrapper<>(list);
    }

    public <E> Set<E> concurrentSet(@NonNull final Set<E> set) {
        return new ConcurrentSetWrapper<>(set);
    }

    public <E> Set<E> concurrentSetFromMap(@NonNull final Map<E, Boolean> map) {
        return new ConcurrentSetFromMapWrapper<>(map);
    }

    public <E> Queue<E> concurrentQueue(@NonNull final Queue<E> set) {
        return new ConcurrentQueueWrapper<>(set);
    }

    public <E> Deque<E> concurrentDeque(@NonNull final Deque<E> set) {
        return new ConcurrentDequeWrapper<>(set);
    }

    public <K, V> Map<K, V> concurrentMap(@NonNull final Map<K, V> map) {
        return new ConcurrentMapWrapper<>(map);
    }
}
