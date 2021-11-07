package ru.progrm_jarvis.javacommons.object;

import lombok.NonNull;
import lombok.experimental.StandardException;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void test_tryRun_withInferredType_success() {
        assertTrue(
                Result.tryRun(TestMethods::voidMethodNotThrowingDeclaredIOException).isSuccess()
        );
    }

    @Test
    void test_tryRun_withInferredType_error() {
        val thrown = new IOException("Expected exception");
        assertSame(thrown,
                Result.tryRun(() -> TestMethods.voidMethodThrowingDeclaredIOException(thrown)).unwrapError()
        );
    }

    @Test
    void test_tryRun_withInferredType_rethrow() {
        val thrown = new RuntimeException("You didn't expect it, did you?");
        assertSame(thrown,
                assertThrows(RuntimeException.class, () -> Result.tryRun(
                        () -> TestMethods.voidMethodNotThrowingDeclaredIOExceptionButRuntimeException(thrown)
                ))
        );
    }

    @Test
    void test_tryRun_withInferredType_typeInference() {
        assertTrue(
                Result.tryRun(() -> TestMethods.voidMethodThrowingCustomException(new CustomException()))
                        .peek(aVoid -> fail("Result should be an error result"))
                        .peekError(CustomException::thisIsACustomError)
                        .isError()
        );
    }

    @Test
    void test_tryGet_withInferredType_success() {
        val returned = "Hello world";
        assertSame(returned,
                Result.tryGet(() -> TestMethods.stringMethodNotThrowingDeclaredIOException(returned)).unwrap()
        );
    }

    @Test
    void test_tryGet_withInferredType_error() {
        val thrown = new IOException("Expected exception");
        assertSame(thrown,
                Result.tryGet(() -> TestMethods.stringMethodThrowingDeclaredIOException(thrown)).unwrapError()
        );
    }

    @Test
    void test_tryGet_withInferredType_rethrow() {
        val thrown = new RuntimeException("You didn't expect it, did you?");
        assertSame(thrown,
                assertThrows(RuntimeException.class, () -> Result.tryGet(
                        () -> TestMethods.stringMethodNotThrowingDeclaredIOExceptionButRuntimeException(thrown)
                ))
        );
    }

    @Test
    void test_tryGet_withInferredType_typeInference() {
        assertTrue(
                Result.tryGet(() -> TestMethods.stringMethodThrowingCustomException(new CustomException()))
                        .peek(string -> fail("Result should be an error result"))
                        .peekError(CustomException::thisIsACustomError)
                        .isError()
        );
    }

    @UtilityClass
    private class TestMethods {

        @SuppressWarnings("RedundantThrows")
        private void voidMethodNotThrowingDeclaredIOException() throws IOException {}

        private void voidMethodThrowingDeclaredIOException(
                final @NonNull IOException thrown
        ) throws IOException {
            throw thrown;
        }

        @SuppressWarnings("RedundantThrows")
        private void voidMethodNotThrowingDeclaredIOExceptionButRuntimeException(
                final @NonNull RuntimeException thrown
        ) throws IOException {
            throw thrown;
        }

        @SuppressWarnings("RedundantThrows")
        private String stringMethodNotThrowingDeclaredIOException(final String returned) throws IOException {
            return returned;
        }

        private String stringMethodThrowingDeclaredIOException(
                final @NonNull IOException thrown
        ) throws IOException {
            throw thrown;
        }

        @SuppressWarnings("RedundantThrows")
        private String stringMethodNotThrowingDeclaredIOExceptionButRuntimeException(
                final @NonNull RuntimeException thrown
        ) throws IOException {
            throw thrown;
        }

        @SuppressWarnings("RedundantThrows")
        private void voidMethodThrowingCustomException(
                final @NonNull CustomException customException
        ) throws CustomException {
            throw customException;
        }

        @SuppressWarnings("RedundantThrows")
        private String stringMethodThrowingCustomException(
                final @NonNull CustomException customException
        ) throws CustomException {
            throw customException;
        }
    }

    @StandardException
    private final class CustomException extends Exception {
        public void thisIsACustomError() {}
    }
}