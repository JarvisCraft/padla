package ru.progrm_jarvis.javacommons.io.wrapper;

import ru.progrm_jarvis.javacommons.service.NonAutoCloseable;

import java.io.IOException;
import java.io.InputStream;

/**
 * A {@link NonAutoCloseable} wrapper for {@link InputStream}.
 */
public abstract class NonAutoCloseableInputStream extends InputStream implements NonAutoCloseable {

    @Override
    public void close() {}

    @Override
    public abstract void doClose() throws IOException;
}
