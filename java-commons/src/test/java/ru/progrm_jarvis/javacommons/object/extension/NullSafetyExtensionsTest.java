package ru.progrm_jarvis.javacommons.object.extension;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.ExtensionMethod;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtensionMethod(NullSafetyExtensions.class)
class NullSafetyExtensionsTest {

    private static final @NotNull String @NotNull [] EMPTY_STRING_ARRAY = {};

    @Test
    void _or() {
        assertEquals("foo", "foo"._or("bar"));
        assertEquals("qux", "qux"._or(null));
        assertEquals("baz", ((String) null)._or("baz"));
        assertNull(((String) null)._or(null));
    }

    @Test
    void _orGet() {
        assertEquals("foo", "foo"._orGet(Assertions::fail));
        assertEquals("baz", ((String) null)._orGet(() -> "baz"));
        assertNull(((String) null)._orGet(() -> null));
    }

    @Test
    void _orGet_throwsNPEOnNullLambda() {
        assertThrows(NullPointerException.class, () -> "magic"._orGet(null));
        assertThrows(NullPointerException.class, () -> ((String) null)._orGet(null));
    }

    @Test
    void _map() {
        assertEquals("abcd", "ab"._map(value -> {
            assertEquals("ab", value);
            return value + "cd";
        }));
        assertNull("yep"._map(value -> {
            assertEquals("yep", value);
            return null;
        }));
        assertNull(((String) null)._map(value -> fail()));
    }

    @Test
    void _map_throwsNPEOnNullLambda() {
        assertThrows(NullPointerException.class, () -> "smth"._map(null));
        assertThrows(NullPointerException.class, () -> ((String) null)._map(null));
    }

    @Test
    void _mapNullable() {
        assertEquals("abcd", "ab"._mapNullable(value -> value + "cd"));
        assertNull("yep"._mapNullable(value -> null));
        assertEquals("null-qrs", ((String) null)._mapNullable(value -> value + "-qrs"));
        assertNull(((String) null)._mapNullable(value -> null));
    }

    @Test
    void _mapNullable_throwsNPEOnNullLambda() {
        assertThrows(NullPointerException.class, () -> "random"._mapNullable(null));
        assertThrows(NullPointerException.class, () -> ((String) null)._mapNullable(null));
    }

    @Test
    void _filter() {
        assertEquals("foo", "foo"._filter(value -> true));
        assertNull("bar"._filter(value -> false));
        assertNull(((String) null)._filter(Assertions::fail));
    }

    @Test
    void _filter_throwsNPEOnNullLambda() {
        assertThrows(NullPointerException.class, () -> "some_value_here"._filter(null));
        assertThrows(NullPointerException.class, () -> ((String) null)._filter(null));
    }

    @Test
    void _filterNullable() {
        assertEquals("foo", "foo"._filterNullable(value -> true));
        assertNull("bar"._filterNullable(value -> false));
        assertNull(((String) null)._filterNullable(value -> true));
        assertNull(((String) null)._filterNullable(value -> false));
    }

    @Test
    void _filterNullable_throwsNPEOnNullLambda() {
        assertThrows(NullPointerException.class, () -> "some_value_here"._filterNullable(null));
        assertThrows(NullPointerException.class, () -> ((String) null)._filterNullable(null));
    }

    @Test
    void _orElseThrow() {
        assertEquals("foo", "foo"._orElseThrow(Assertions::<RuntimeException>fail));
        assertThrows(SuccessException.class, () -> ((String) null)._orElseThrow(SuccessException::new));
    }


    @Test
    void _orElseThrow_throwsNPEOnNullLambda() {
        assertThrows(NullPointerException.class, () -> "some_value_here"._orElseThrow(null));
        assertThrows(NullPointerException.class, () -> ((String) null)._orElseThrow(null));
    }

    @Test
    void _orElseThrow_throwsNPEOnNullLambdaValue() {
        assertThrows(NullPointerException.class, () -> ((String) null)._orElseThrow(() -> null));
    }


    @Test
    void _stream() {
        assertArrayEquals(new String[]{"value"}, "value"._stream().toArray(String[]::new));
        assertArrayEquals(EMPTY_STRING_ARRAY, ((String) null)._stream().toArray(String[]::new));
    }

    @Test
    void _streamOfNullable() {
        assertArrayEquals(new String[]{"value"}, "value"._streamOfNullable().toArray(String[]::new));
        assertArrayEquals(new String[]{null}, ((String) null)._streamOfNullable().toArray(String[]::new));
    }

    @Test
    void _ifPresentOrElse_onNotNull() {
        @SuppressWarnings("unchecked") final Consumer<String> action = mock(Consumer.class);
        "value"._ifPresentOrElse(action, Assertions::fail);
        verify(action).accept("value");
    }

    @Test
    void _ifPresentOrElse_onNull() {
        val nullAction = mock(Runnable.class);
        ((String) null)._ifPresentOrElse(value -> fail(), nullAction);
        verify(nullAction).run();
    }

    @Test
    void _ifPresentOrElse_throwsNPEOnNullLambdaValue() {
        assertThrows(NullPointerException.class, () -> ((String) null)._ifPresentOrElse(null, () -> {}));
        assertThrows(NullPointerException.class, () -> ((String) null)._ifPresentOrElse(value -> {}, null));
        assertThrows(NullPointerException.class, () -> ((String) null)._ifPresentOrElse(null, null));
    }

    @Test
    void _toOptional() {
        assertEquals(Optional.of("some value"), "some value"._toOptional());
        assertEquals(Optional.empty(), ((String) null)._toOptional());
    }

    // this class cannot be local
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class SuccessException extends Exception {

        private static final long serialVersionUID = 0L;
    }
}