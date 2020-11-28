package ru.progrm_jarvis.javacommons.collection;

import org.jetbrains.annotations.Contract;

/**
 * A sized collection.
 * This is a convenient interface applicable both to {@link java.util.Collection} and {@link java.util.Map}.
 */
public interface SizedCollection {

    /**
     * Gets the size of this collection.
     *
     * @return size of this collection
     */
    int size();

    /**
     * Checks if this collection is empty.
     *
     * @return {@code true} if this collection is empty and {@code false} otherwise
     */
    boolean isEmpty();

    /**
     * Clears this collection.
     *
     * @throws UnsupportedOperationException if clearing is not supported by this collection (e.g. it is unmodifiable)
     */
    @Contract(mutates = "this")
    void clear();
}
