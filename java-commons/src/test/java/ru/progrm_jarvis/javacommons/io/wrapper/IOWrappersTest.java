package ru.progrm_jarvis.javacommons.io.wrapper;

import lombok.val;
import lombok.var;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import ru.progrm_jarvis.javacommons.util.NonAutoCloseable;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class IOWrappersTest {

    static <T> Arguments nonAutoCloseableTestArguments(final Class<? extends T> autoCloseableType,
                                                       final Function<T, NonAutoCloseable> nonAutoCloseableCreator) {
        val autoCloseable = Mockito.mock(autoCloseableType);

        return arguments(autoCloseable, nonAutoCloseableCreator.apply(autoCloseable));
    }

    static Stream<Arguments> provideNonAutoCloseableWrappers() {
        return Stream.of(
                nonAutoCloseableTestArguments(InputStream.class, IOWrappers::nonAutoCloseable),
                nonAutoCloseableTestArguments(OutputStream.class, IOWrappers::nonAutoCloseable),
                nonAutoCloseableTestArguments(Reader.class, IOWrappers::nonAutoCloseable),
                nonAutoCloseableTestArguments(Writer.class, IOWrappers::nonAutoCloseable)
        );
    }

    @ParameterizedTest
    @MethodSource("provideNonAutoCloseableWrappers")
    void testNonClosingOutputStream(final AutoCloseable original, final NonAutoCloseable wrapped) throws Exception {
        verify(original, never()).close();
        {
            val attempts = 8 + ThreadLocalRandom.current().nextInt(8);
            for (var i = 0; i < attempts; i++) {
                wrapped.close();
                verify(original, never()).close();
            }
        }

        wrapped.doClose();
        verify(original).close();
    }
}