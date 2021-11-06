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
    void test_tryRunChecked_withInferredType_success() {
        assertTrue(
                Result.tryRunChecked(TestMethods::voidMethodNotThrowingDeclaredIOException).isSuccess()
        );
    }

    @Test
    void test_tryRunChecked_withInferredType_error() {
        val thrown = new IOException("Expected exception");
        assertSame(thrown,
                Result.tryRunChecked(() -> TestMethods.voidMethodThrowingDeclaredIOException(thrown)).unwrapError()
        );
    }

    @Test
    void test_tryRunChecked_withInferredType_rethrow() {
        val thrown = new RuntimeException("You didn't expect it, did you?");
        assertSame(thrown,
                assertThrows(RuntimeException.class, () -> Result.tryRunChecked(
                        () -> TestMethods.voidMethodNotThrowingDeclaredIOExceptionButRuntimeException(thrown)
                ))
        );
    }

    @Test
    void test_tryRunChecked_withInferredType_typeInference() {
        assertTrue(
                Result.tryRunChecked(() -> TestMethods.voidMethodThrowingCustomException(new CustomException()))
                        .peek(aVoid -> fail("Result should be an error result"))
                        .peekError(customError -> customError.thisIsACustomError())
                        .isError()
        );
    }

    @Test
    void test_tryGetChecked_withInferredType_success() {
        val returned = "Hello world";
        assertSame(returned,
                Result.tryGetChecked(() -> TestMethods.stringMethodNotThrowingDeclaredIOException(returned)).unwrap()
        );
    }

    @Test
    void test_tryGetChecked_withInferredType_error() {
        val thrown = new IOException("Expected exception");
        assertSame(thrown,
                Result.tryGetChecked(() -> TestMethods.stringMethodThrowingDeclaredIOException(thrown)).unwrapError()
        );
    }

    @Test
    void test_tryGetChecked_withInferredType_rethrow() {
        val thrown = new RuntimeException("You didn't expect it, did you?");
        assertSame(thrown,
                assertThrows(RuntimeException.class, () -> Result.tryGetChecked(
                        () -> TestMethods.stringMethodNotThrowingDeclaredIOExceptionButRuntimeException(thrown)
                ))
        );
    }

    @Test
    void test_tryGetChecked_withInferredType_typeInference() {
        assertTrue(
                Result.tryGetChecked(() -> TestMethods.stringMethodThrowingCustomException(new CustomException()))
                        .peek(string -> fail("Result should be an error result"))
                        .peekError(customError -> customError.thisIsACustomError())
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