package ru.progrm_jarvis.javacommons.io.wrapper;

import ru.progrm_jarvis.javacommons.util.NonAutoCloseable;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A {@link NonAutoCloseable} wrapper for {@link OutputStream}.
 */
public abstract class NonAutoCloseableOutputStream extends OutputStream implements NonAutoCloseable {

    @Override
    public void close() {}

    @Override
    public abstract void doClose() throws IOException;
}
