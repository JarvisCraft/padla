package ru.progrm_jarvis.javacommons.map;

import lombok.val;
import org.junit.jupiter.api.Test;
import ru.progrm_jarvis.javacommons.pair.SimplePair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.collect.Maps.immutableEntry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MapFillerTest {

    @Test
    @SuppressWarnings("unchecked")
    void testFillMapFromArrayOfUncheckedSimplePairs() {
        assertEquals(new HashMap<>(), MapUtil.fillMap(new HashMap<>()));

        assertThrows(IllegalArgumentException.class, () -> MapUtil.fillMap(new HashMap<>(), 1));

        assertThrows(IllegalArgumentException.class, () -> MapUtil.fillMap(new HashMap<>(), 1, 3, "String"));

        val entries = new HashMap<Integer, String>() {{
            put(1, "Hello");
            put(2, "world");
        }}.entrySet();

        assertThat(entries, hasSize(2));
        assertThat(entries, contains(immutableEntry(1, "Hello"), immutableEntry(2, "world")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFillMapFromArray() {
        val entries = MapUtil.<Integer, String, Map<Integer, String>>fillMap(
                new HashMap<>(), SimplePair.of(1, "Hello"), SimplePair.of(2, "world")
        ).entrySet();

        assertThat(entries, hasSize(2));
        assertThat(entries, contains(immutableEntry(1, "Hello"), immutableEntry(2, "world")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFillMapFromIterator() {
        val entries = MapUtil
                .fillMap(new HashMap<>(), Arrays.asList(SimplePair.of(1, "Hello"), SimplePair.of(2, "world")).iterator())
                .entrySet();

        assertThat(entries, hasSize(2));
        assertThat(entries, contains(immutableEntry(1, "Hello"), immutableEntry(2, "world")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFillMapFromIterable() {
        val entries = MapUtil
                .fillMap(new HashMap<>(), Arrays.asList(SimplePair.of(1, "Hello"), SimplePair.of(2, "world")))
                .entrySet();

        assertThat(entries, hasSize(2));
        assertThat(entries, contains(immutableEntry(1, "Hello"), immutableEntry(2, "world")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFillMapFromStream() {
        val entries = MapUtil.fillMap(new HashMap<>(), Stream.of(SimplePair.of(1, "Hello"), SimplePair.of(2, "world"))).entrySet();

        assertThat(entries, hasSize(2));
        assertThat(entries, contains(immutableEntry(1, "Hello"), immutableEntry(2, "world")));
    }

    @Test
    void testGetOrDefault() {
        val map = new HashMap<Integer, String>();
        map.put(1, "One");
        map.put(2, "Two");

        @SuppressWarnings("unchecked") final Supplier<String> defaultSupplier = mock(Supplier.class);
        when(defaultSupplier.get()).thenReturn("Default");

        assertEquals("One", MapUtil.getOrDefault(map, 1, defaultSupplier));
        verify(defaultSupplier, times(0)).get();

        assertEquals("Two", MapUtil.getOrDefault(map, 2, defaultSupplier));
        verify(defaultSupplier, times(0)).get();

        assertEquals("Default", MapUtil.getOrDefault(map, 3, defaultSupplier));
        verify(defaultSupplier, times(1)).get();
    }
}