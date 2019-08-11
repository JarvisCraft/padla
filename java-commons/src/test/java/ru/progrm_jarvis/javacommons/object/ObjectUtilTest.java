package ru.progrm_jarvis.javacommons.object;

import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ObjectUtilTest {

    @Test
    void testNonNull() {
        assertEquals("foo", ObjectUtil.nonNull("foo"));
        assertEquals("foo", ObjectUtil.nonNull("foo", null));
        assertEquals("foo", ObjectUtil.nonNull(null, "foo", null));
        assertEquals("foo", ObjectUtil.nonNull(null, "foo"));
        assertEquals("foo", ObjectUtil.nonNull("foo", "bar"));
        assertEquals("foo", ObjectUtil.nonNull("foo", null, "bar"));
        assertEquals("foo", ObjectUtil.nonNull(null, "foo", null, "bar"));
        assertEquals("foo", ObjectUtil.nonNull(null, "foo", "bar"));
        assertNull(ObjectUtil.nonNull((Object) null));
        assertNull(ObjectUtil.nonNull((Object) null, null));
        assertNull(ObjectUtil.nonNull((Object) null, null, null));
    }

    @Test
    void testOptionalNonNull() {
        assertEquals(Optional.of("foo"), ObjectUtil.optionalNonNull("foo"));
        assertEquals(Optional.of("foo"), ObjectUtil.optionalNonNull("foo", null));
        assertEquals(Optional.of("foo"), ObjectUtil.optionalNonNull(null, "foo", null));
        assertEquals(Optional.of("foo"), ObjectUtil.optionalNonNull(null, "foo"));
        assertEquals(Optional.of("foo"), ObjectUtil.optionalNonNull("foo", "bar"));
        assertEquals(Optional.of("foo"), ObjectUtil.optionalNonNull("foo", null, "bar"));
        assertEquals(Optional.of("foo"), ObjectUtil.optionalNonNull(null, "foo", null, "bar"));
        assertEquals(Optional.of("foo"), ObjectUtil.optionalNonNull(null, "foo", "bar"));
        assertEquals(Optional.empty(), ObjectUtil.optionalNonNull((Object) null));
        assertEquals(Optional.empty(), ObjectUtil.optionalNonNull((Object) null, null));
        assertEquals(Optional.empty(), ObjectUtil.optionalNonNull((Object) null, null, null));
    }

    @Test
    void testNonNullOrThrow() {
        assertEquals("foo", ObjectUtil.nonNullOrThrow("foo"));
        assertEquals("foo", ObjectUtil.nonNullOrThrow("foo", null));
        assertEquals("foo", ObjectUtil.nonNullOrThrow(null, "foo", null));
        assertEquals("foo", ObjectUtil.nonNullOrThrow(null, "foo"));
        assertEquals("foo", ObjectUtil.nonNullOrThrow("foo", "bar"));
        assertEquals("foo", ObjectUtil.nonNullOrThrow("foo", null, "bar"));
        assertEquals("foo", ObjectUtil.nonNullOrThrow(null, "foo", null, "bar"));
        assertEquals("foo", ObjectUtil.nonNullOrThrow(null, "foo", "bar"));
        assertThrows(NullPointerException.class, () -> ObjectUtil.nonNullOrThrow((Object) null));
        assertThrows(NullPointerException.class, () -> ObjectUtil.nonNullOrThrow((Object) null, null));
        assertThrows(NullPointerException.class, () -> ObjectUtil.nonNullOrThrow((Object) null, null, null));
    }

    @Test
    void testMap() {
        assertEquals("f", ObjectUtil.map("foo", t -> t.substring(0, 1)));
        assertEquals("1", ObjectUtil.map(1, t -> Integer.toString(t)));
        assertNull(ObjectUtil.map(123, t -> null));
        assertThrows(NullPointerException.class, () -> ObjectUtil.map(null, t -> {
            if (t == null) throw new NullPointerException();
            return "nonnull";
        }));
        assertThrows(NullPointerException.class, () -> ObjectUtil.map(null, t -> {
            throw new NullPointerException();
        }));

        final class CustomException extends RuntimeException {}
        assertThrows(CustomException.class, () -> ObjectUtil.map(null, t -> {
            throw new CustomException();
        }));
    }

    @Test
    void testMapNonNull() {
        assertEquals("f", ObjectUtil.mapNonNull(t -> t.substring(0, 1), "foo"));
        assertEquals("f", ObjectUtil.mapNonNull(t -> t.substring(0, 1), "foo"));
        assertEquals("f", ObjectUtil.mapNonNull(t -> t.substring(0, 1), "foo", null));
        assertEquals("f", ObjectUtil.mapNonNull(t -> t.substring(0, 1), null, "foo", null));
        assertEquals("f", ObjectUtil.mapNonNull(t -> t.substring(0, 1), null, "foo"));
        assertEquals("f", ObjectUtil.mapNonNull(t -> t.substring(0, 1), "foo", "bar"));
        assertEquals("f", ObjectUtil.mapNonNull(t -> t.substring(0, 1), "foo", null, "bar"));
        assertEquals("f", ObjectUtil.mapNonNull(t -> t.substring(0, 1), null, "foo", null, "bar"));
        assertEquals("f", ObjectUtil.mapNonNull(t -> t.substring(0, 1), null, "foo", "bar"));
        assertEquals("+", ObjectUtil.mapNonNull(t -> t == null ? "+" : "-", (Object) null));
        assertEquals("+", ObjectUtil.mapNonNull(t -> t == null ? "+" : "-", (Object) null, null));
        assertEquals("+", ObjectUtil.mapNonNull(t -> t == null ? "+" : "-", (Object) null, null, null));
    }

    @Test
    void testMapOnlyNonNull() {
        assertEquals("f", ObjectUtil.mapOnlyNonNull(t -> t.substring(0, 1), "foo"));
        assertEquals("f", ObjectUtil.mapOnlyNonNull(t -> t.substring(0, 1), "foo"));
        assertEquals("f", ObjectUtil.mapOnlyNonNull(t -> t.substring(0, 1), "foo", null));
        assertEquals("f", ObjectUtil.mapOnlyNonNull(t -> t.substring(0, 1), null, "foo", null));
        assertEquals("f", ObjectUtil.mapOnlyNonNull(t -> t.substring(0, 1), null, "foo"));
        assertEquals("f", ObjectUtil.mapOnlyNonNull(t -> t.substring(0, 1), "foo", "bar"));
        assertEquals("f", ObjectUtil.mapOnlyNonNull(t -> t.substring(0, 1), "foo", null, "bar"));
        assertEquals("f", ObjectUtil.mapOnlyNonNull(t -> t.substring(0, 1), null, "foo", null, "bar"));
        assertEquals("f", ObjectUtil.mapOnlyNonNull(t -> t.substring(0, 1), null, "foo", "bar"));
        assertNull(ObjectUtil.mapOnlyNonNull(t -> t.substring(0, 1), (String) null));
        assertNull(ObjectUtil.mapOnlyNonNull(t -> t.substring(0, 1), (String) null, null));
        assertNull(ObjectUtil.mapOnlyNonNull(t -> t.substring(0, 1), (String) null, null, null));
    }

    @Test
    void testMapNonNullOrThrow() {
        assertEquals("f", ObjectUtil.mapNonNullOrThrow(t -> t.substring(0, 1), "foo"));
        assertEquals("f", ObjectUtil.mapNonNullOrThrow(t -> t.substring(0, 1), "foo", null));
        assertEquals("f", ObjectUtil.mapNonNullOrThrow(t -> t.substring(0, 1), null, "foo", null));
        assertEquals("f", ObjectUtil.mapNonNullOrThrow(t -> t.substring(0, 1), null, "foo"));
        assertEquals("f", ObjectUtil.mapNonNullOrThrow(t -> t.substring(0, 1), "foo", "bar"));
        assertEquals("f", ObjectUtil.mapNonNullOrThrow(t -> t.substring(0, 1), "foo", null, "bar"));
        assertEquals("f", ObjectUtil.mapNonNullOrThrow(t -> t.substring(0, 1), null, "foo", null, "bar"));
        assertEquals("f", ObjectUtil.mapNonNullOrThrow(t -> t.substring(0, 1), null, "foo", "bar"));
        // mapping function should not be called so the one which allows nulls is used
        assertThrows(NullPointerException.class, () -> ObjectUtil.mapNonNullOrThrow(Objects::isNull, (Object) null));
        assertThrows(NullPointerException.class, () -> ObjectUtil.mapNonNullOrThrow(Objects::isNull, (Object) null, null));
        assertThrows(NullPointerException.class, () -> ObjectUtil.mapNonNullOrThrow(Objects::isNull, (Object) null, null, null));
    }
}