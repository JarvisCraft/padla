package ru.progrm_jarvis.javacommons.io.wrapper;

import lombok.*;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * An utility for creating wrappers over {@link java.io} classes.
 */
@UtilityClass
public class IOWrappers {

    /**
     * Creates a {@link NonAutoCloseableInputStream} from the given one.
     *
     * @param inputStream input stream to wrap
     * @return wrapped input stream
     */
    @SuppressWarnings("MethodReturnOfConcreteClass") // caused by sub-classing
    public @NotNull NonAutoCloseableInputStream nonAutoCloseable(final @NonNull InputStream inputStream) {
        return new NonAutoCloseableInputStreamWrapper(inputStream);
    }

    /**
     * Creates a {@link NonAutoCloseableOutputStream} from the given one.
     *
     * @param outputStream output stream to wrap
     * @return wrapped output stream
     */
    @SuppressWarnings("MethodReturnOfConcreteClass") // caused by sub-classing
    public @NotNull NonAutoCloseableOutputStream nonAutoCloseable(final @NonNull OutputStream outputStream) {
        return new NonAutoCloseableOutputStreamWrapper(outputStream);
    }

    /**
     * Creates a {@link NonAutoCloseableReader} from the given one.
     *
     * @param reader reader to wrap
     * @return wrapped reader
     */
    @SuppressWarnings("MethodReturnOfConcreteClass") // caused by sub-classing
    public @NotNull NonAutoCloseableReader nonAutoCloseable(final @NonNull Reader reader) {
        return new NonAutoCloseableReaderWrapper(reader);
    }

    /**
     * Creates a {@link NonAutoCloseableWriter} from the given one.
     *
     * @param writer writer to wrap
     * @return wrapped writer
     */
    @SuppressWarnings("MethodReturnOfConcreteClass") // caused by sub-classing
    public @NotNull NonAutoCloseableWriter nonAutoCloseable(final @NonNull Writer writer) {
        return new NonAutoCloseableWriterWrapper(writer);
    }

    /**
     * Simple delegating {@link NonAutoCloseableInputStream}.
     */
    @Value
    @EqualsAndHashCode(callSuper = false) // super has no logic
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @RequiredArgsConstructor(access = AccessLevel.PUBLIC)
    private static class NonAutoCloseableInputStreamWrapper extends NonAutoCloseableInputStream {

        @Delegate(excludes = AutoCloseable.class)
        @NonNull InputStream inputStream;

        @Override
        public void doClose() throws IOException {
            inputStream.close();
        }
    }

    /**
     * Simple delegating {@link NonAutoCloseableOutputStream}.
     */
    @Value
    @EqualsAndHashCode(callSuper = false) // super has no logic
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @RequiredArgsConstructor(access = AccessLevel.PUBLIC)
    private static class NonAutoCloseableOutputStreamWrapper extends NonAutoCloseableOutputStream {

        @Delegate(excludes = AutoCloseable.class)
        @NonNull OutputStream outputStream;

        @Override
        public void doClose() throws IOException {
            outputStream.close();
        }
    }

    /**
     * Simple delegating {@link NonAutoCloseableReader}.
     */
    @Value
    @EqualsAndHashCode(callSuper = false) // super has no logic
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @RequiredArgsConstructor(access = AccessLevel.PUBLIC)
    private static class NonAutoCloseableReaderWrapper extends NonAutoCloseableReader {

        @Delegate(excludes = AutoCloseable.class)
        @NonNull Reader reader;

        @Override
        public void doClose() throws IOException {
            reader.close();
        }
    }

    /**
     * Simple delegating {@link NonAutoCloseableWriter}.
     */
    @Value
    @EqualsAndHashCode(callSuper = false) // super has no logic
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @RequiredArgsConstructor(access = AccessLevel.PUBLIC)
    private static class NonAutoCloseableWriterWrapper extends NonAutoCloseableWriter {

        @Delegate(excludes = AutoCloseable.class)
        @NonNull Writer writer;

        @Override
        public void doClose() throws IOException {
            writer.close();
        }
    }
}
