package ru.progrm_jarvis.javacommons.io.wrapper;

import ru.progrm_jarvis.javacommons.service.NonAutoCloseable;

import java.io.IOException;
import java.io.Reader;

/**
 * A {@link NonAutoCloseable} wrapper for {@link Reader}.
 */
public abstract class NonAutoCloseableReader extends Reader implements NonAutoCloseable {

    @Override
    public void close() {}

    @Override
    public abstract void doClose() throws IOException;
}
