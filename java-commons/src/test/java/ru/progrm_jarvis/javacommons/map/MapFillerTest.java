package ru.progrm_jarvis.javacommons.map;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.progrm_jarvis.javacommons.collection.MapFiller;
import ru.progrm_jarvis.javacommons.object.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MapFillerTest {

    @Test
    void testMapFillerConstructWithFirst() {
        assertThat(
                MapFiller.from(new HashMap<>()).map().entrySet(),
                empty()
        );

        assertEquals(
                new HashMap<String, Integer>() {{
                    put("Hello", 1);
                }},
                MapFiller.from(new HashMap<>(), "Hello", 1).map()
        );
    }

    @Test
    @SuppressWarnings("unchecked") // Hamcrest, R U fine?
    void testMapFillerPut() {
        val entries = MapFiller.from(new HashMap<String, Integer>())
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
        val entries = MapFiller.from(new HashMap<String, Integer>())
                .fill(Pair.of("one", 1), Pair.of("two", 2))
                .map()
                .entrySet();

        assertThat(entries, hasSize(2));

        assertThat(entries, hasItems(immutableEntry("one", 1), immutableEntry("two", 2)));
    }

    @Test
    @SuppressWarnings("unchecked") // Hamcrest, R U fine?
    void testMapFillerFillFromIterator() {
        val entries = MapFiller.from(new HashMap<String, Integer>())
                .fill(Arrays.asList(Pair.of("one", 1), Pair.of("two", 2)).iterator())
                .map()
                .entrySet();

        assertThat(entries, hasSize(2));

        assertThat(entries, hasItems(immutableEntry("one", 1), immutableEntry("two", 2)));
    }

    @Test
    @SuppressWarnings("unchecked") // Hamcrest, R U fine?
    void testMapFillerFillFromIterable() {
        val entries = MapFiller.from(new HashMap<String, Integer>())
                .fill(Arrays.asList(Pair.of("one", 1), Pair.of("two", 2)))
                .map()
                .entrySet();

        assertThat(entries, hasSize(2));

        assertThat(entries, hasItems(immutableEntry("one", 1), immutableEntry("two", 2)));
    }

    @Test
    @SuppressWarnings("unchecked") // Hamcrest, R U fine?
    void testMapFillerFillFromStream() {
        val entries = MapFiller.from(new HashMap<String, Integer>())
                .fill(Stream.of(Pair.of("one", 1), Pair.of("two", 2)))
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
        val entries = MapFiller.from(new HashMap<String, Integer>())
                .put("one", 1)
                .put("two", 2)
                .fill(Pair.of("three", 3), Pair.of("four", 4))
                .fill(Arrays.asList(Pair.of("five", 5), Pair.of("six", 6)).iterator())
                .fill(Arrays.asList(Pair.of("seven", 7), Pair.of("eight", 8)))
                .fill(Stream.of(Pair.of("nine", 9), Pair.of("ten", 10)))
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

    // this is a slow but most safe implementation of Map.Entry creation
    private static <K, V> Map.@NotNull Entry<K, V> immutableEntry(final K key, final V value) {
        final Map<K, V> map;
        (map = new HashMap<>()).put(key, value);

        return map.entrySet().iterator().next();
    }
}