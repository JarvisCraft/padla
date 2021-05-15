package ru.progrm_jarvis.javacommons.object.extension;

import lombok.experimental.ExtensionMethod;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtensionMethod(LegacyOptionalExtensions.class)
class LegacyOptionalExtensionsTest {

    private static final @NotNull String @NotNull [] EMPTY_STRING_ARRAY = {};

    @Test
    void isEmpty() {
        assertTrue(Optional.empty().isEmpty());
        assertFalse(Optional.of("Hello world!").isEmpty());
    }

    @Test
    void ifPresentOrElse_onPresent() {
        @SuppressWarnings("unchecked") final Consumer<String> action = mock(Consumer.class);
        Optional.of("oh, hello").ifPresentOrElse(action, Assertions::fail);
        verify(action).accept("oh, hello");
    }

    @Test
    void ifPresentOrElse_onEmpty() {
        val emptyAction = mock(Runnable.class);
        Optional.empty().ifPresentOrElse(value -> fail(), emptyAction);
        verify(emptyAction).run();
    }

    @Test
    void ifPresentOrElse_onNullLambdas() {
        assertThrows(NullPointerException.class, () -> Optional.of("something").ifPresentOrElse(null, () -> {}));
        assertDoesNotThrow(() -> Optional.of("something").ifPresentOrElse(value -> {}, null));
        assertThrows(NullPointerException.class, () -> Optional.empty().ifPresentOrElse(value -> {}, null));
        assertDoesNotThrow(() -> Optional.empty().ifPresentOrElse(null, () -> {}));
    }

    @Test
    void or() {
        assertEquals(Optional.of("foo"), Optional.of("foo").or(() -> Optional.of("bar")));
        assertEquals(Optional.of("foo"), Optional.of("foo").or(Optional::empty));
        assertEquals(Optional.of("foo"), Optional.of("foo").or(() -> null));
        assertEquals(Optional.of("baz"), Optional.empty().or(() -> Optional.of("baz")));
        assertEquals(Optional.empty(), Optional.empty().or(Optional::empty));
        assertThrows(NullPointerException.class, () -> Optional.empty().or(() -> null));
    }

    @Test
    void or_throwsNPEonNullLambda() {
        assertThrows(NullPointerException.class, () -> Optional.of("some value").or(null));
        assertThrows(NullPointerException.class, () -> Optional.empty().or(null));
    }

    @Test
    void stream() {
        assertArrayEquals(new String[]{"wow"}, Optional.of("wow").stream().toArray(String[]::new));
        assertArrayEquals(EMPTY_STRING_ARRAY, Optional.empty().stream().toArray(String[]::new));
    }

    @Test
    void orElseThrow() {
        assertDoesNotThrow(() -> Optional.of("wow").orElseThrow());
        assertThrows(NoSuchElementException.class, () -> Optional.empty().orElseThrow());
    }
}