package ru.progrm_jarvis.javacommons.io.wrapper;

import ru.progrm_jarvis.javacommons.util.NonAutoCloseable;

import java.io.IOException;
import java.io.Writer;

/**
 * A {@link NonAutoCloseable} wrapper for {@link Writer}.
 */
public abstract class NonAutoCloseableWriter extends Writer implements NonAutoCloseable {

    @Override
    public void close() {}

    @Override
    public abstract void doClose() throws IOException;
}
