package ru.progrm_jarvis.javacommons.io.wrapper;

/**
 * <p>A wrapper for {@link AutoCloseable} which disables its {@link AutoCloseable#close()} method
 * instead providing {@link #doClose()} method for explicit closing of this object.</p>
 *
 * <p>This may come in handy when passing {@link AutoCloseable} objects to foreign methods
 * which may close this object although this should happen later</p>
 */
public interface NonAutoCloseable extends AutoCloseable {

    /**
     * Imitates closing this object not actually doing it.
     *
     * @apiNote this object should be explicitly called using {@link #doClose()}
     */
    @Override
    default void close() {}

    /**
     * Actually closes this object as if it was done by calling {@link AutoCloseable#close()}.
     *
     * @see AutoCloseable#close() for this method's behaviour
     */
    void doClose();
}
