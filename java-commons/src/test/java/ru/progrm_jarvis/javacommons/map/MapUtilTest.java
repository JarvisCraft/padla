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
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MapUtilTest {

    ///////////////////////////////////////////////////////////////////////////
    // MapUtil
    ///////////////////////////////////////////////////////////////////////////

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

    ///////////////////////////////////////////////////////////////////////////
    // MapFiller
    ///////////////////////////////////////////////////////////////////////////

    @Test
    void testMapFillerConstructWithFirst() {
        assertThat(
                MapUtil.mapFiller(new HashMap<>()).map().entrySet(),
                empty()
        );

        assertEquals(
                new HashMap<String, Integer>() {{
                    put("Hello", 1);
                }},
                MapUtil.mapFiller(new HashMap<>(), "Hello", 1).map()
        );
    }

    @Test
    @SuppressWarnings("unchecked") // Hamcrest, R U fine?
    void testMapFillerPut() {
        val entries = MapUtil.mapFiller(new HashMap<String, Integer>())
                .put("one", 1)
                .put("two", 2)
                .map()
                .entrySet();

        assertThat(entries, hasSize(2));

        assertThat(entries, hasItems(immutableEntry("one", 1), immutableEntry("two", 2)));
    }

    @Test
    @SuppressWarnings("unchecked") // Hamcrest, R U fine?
    void testMapFillerFillFromArray() {
        val entries = MapUtil.mapFiller(new HashMap<String, Integer>())
                .fill(SimplePair.of("one", 1), SimplePair.of("two", 2))
                .map()
                .entrySet();

        assertThat(entries, hasSize(2));

        assertThat(entries, hasItems(immutableEntry("one", 1), immutableEntry("two", 2)));
    }

    @Test
    @SuppressWarnings("unchecked") // Hamcrest, R U fine?
    void testMapFillerFillFromIterator() {
        val entries = MapUtil.mapFiller(new HashMap<String, Integer>())
                .fill(Arrays.asList(SimplePair.of("one", 1), SimplePair.of("two", 2)).iterator())
                .map()
                .entrySet();

        assertThat(entries, hasSize(2));

        assertThat(entries, hasItems(immutableEntry("one", 1), immutableEntry("two", 2)));
    }

    @Test
    @SuppressWarnings("unchecked") // Hamcrest, R U fine?
    void testMapFillerFillFromIterable() {
        val entries = MapUtil.mapFiller(new HashMap<String, Integer>())
                .fill(Arrays.asList(SimplePair.of("one", 1), SimplePair.of("two", 2)))
                .map()
                .entrySet();

        assertThat(entries, hasSize(2));

        assertThat(entries, hasItems(immutableEntry("one", 1), immutableEntry("two", 2)));
    }

    @Test
    @SuppressWarnings("unchecked") // Hamcrest, R U fine?
    void testMapFillerFillFromStream() {
        val entries = MapUtil.mapFiller(new HashMap<String, Integer>())
                .fill(Stream.of(SimplePair.of("one", 1), SimplePair.of("two", 2)))
                .map()
                .entrySet();

        assertThat(entries,
                hasSize(2)
        );

        assertThat(entries, hasItems(immutableEntry("one", 1), immutableEntry("two", 2)));
    }

    @Test
    @SuppressWarnings("unchecked") // Hamcrest, R U fine?
    void testMapFillerFillFromEveryKind() {
        val entries = MapUtil.mapFiller(new HashMap<String, Integer>())
                .put("one", 1)
                .put("two", 2)
                .fill(SimplePair.of("three", 3), SimplePair.of("four", 4))
                .fill(Arrays.asList(SimplePair.of("five", 5), SimplePair.of("six", 6)).iterator())
                .fill(Arrays.asList(SimplePair.of("seven", 7), SimplePair.of("eight", 8)))
                .fill(Stream.of(SimplePair.of("nine", 9), SimplePair.of("ten", 10)))
                .map()
                .entrySet();

        assertThat(entries, hasSize(10));

        assertThat(
                entries,
                hasItems(
                        immutableEntry("one", 1),
                        immutableEntry("two", 2),
                        immutableEntry("three", 3),
                        immutableEntry("four", 4),
                        immutableEntry("five", 5),
                        immutableEntry("six", 6),
                        immutableEntry("seven", 7),
                        immutableEntry("eight", 8),
                        immutableEntry("nine", 9),
                        immutableEntry("ten", 10)
                )
        );
    }
}