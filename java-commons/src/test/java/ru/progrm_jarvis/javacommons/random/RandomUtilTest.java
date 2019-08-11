package ru.progrm_jarvis.javacommons.random;

import lombok.val;
import lombok.var;
import org.junit.jupiter.api.Test;
import ru.progrm_jarvis.javacommons.map.MapUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RandomUtilTest {

    @Test
    void testGetRandomSign() {
        val isAllowedValue = anyOf(is(1), is(-1));

        val iterations = 64 + ThreadLocalRandom.current().nextInt(65);
        for (var i = 0; i < iterations; i++) assertThat(RandomUtil.randomSign(), isAllowedValue);
    }

    @Test
    void testGetRandomFromMapOfChances() {
        val random = ThreadLocalRandom.current();

        var values = MapUtil.fillMap(new HashMap<>(), "One", 1, "Two", 2);
        var keySet = values.keySet();
        var iterations = 128 + random.nextInt(129);
        for (var i = 0; i < iterations; i++) assertThat(RandomUtil.getRandom(values), isIn(keySet));

        values = MapUtil.fillMap(new HashMap<>(), "One", 1, "Two", 2, "Three", 3, "Four", 4, "Five", 5);
        keySet = values.keySet();
        iterations = 128 + random.nextInt(129);
        for (var i = 0; i < iterations; i++) assertThat(
                RandomUtil.getRandom(values), isIn(keySet)
        );

        values = MapUtil.fillMap(new HashMap<>(), "Hi", 1);
        iterations = 128 + random.nextInt(129);
        for (var i = 0; i < iterations; i++) assertThat(
                "Hi", equalTo(RandomUtil.getRandom(values))
        );

        assertThrows(IllegalArgumentException.class, () -> RandomUtil.getRandom(new HashMap<>()));
    }

    @Test
    void testGetRandomFromList() {
        val random = ThreadLocalRandom.current();

        var values = new ArrayList<String>();
        var iterations = 64 + random.nextInt(65);
        for (var i = 0; i < iterations; i++) values.add(Integer.toString(random.nextInt()));

        iterations = 128 + random.nextInt(129);
        for (var i = 0; i < iterations; i++) assertThat(RandomUtil.getRandom(values), isIn(values));

        assertThrows(IllegalArgumentException.class, () -> RandomUtil.getRandom(new ArrayList<>()));
    }

    @Test
    void testGetRandomFromCollection() {
        val random = ThreadLocalRandom.current();

        final Collection<String> values;
        var valuesList = new ArrayList<String>();
        var iterations = 64 + random.nextInt(65);
        for (var i = 0; i < iterations; i++) valuesList.add(Integer.toString(random.nextInt()));
        values = valuesList;

        iterations = 128 + random.nextInt(129);
        for (var i = 0; i < iterations; i++) assertThat(                RandomUtil.getRandom(values), isIn(values));

        assertThrows(IllegalArgumentException.class, () -> RandomUtil.getRandom(new HashSet<>()));
    }
}