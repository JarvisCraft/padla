package ru.progrm_jarvis.javacommons.util;

import lombok.val;
import lombok.var;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UuidUtilTest {

    @Test
    void testBytesToUuidFailSafe() {
        val random = ThreadLocalRandom.current();
        var iterations = 32 + random.nextInt(33);
        for (int i = 0; i < iterations; i++) {
            val bytes = new byte[random.nextInt(16)];
            assertThrows(IllegalArgumentException.class, () -> UuidUtil.uuidFromBytes(bytes));
        }

        iterations = 32 + random.nextInt(33);
        for (int i = 0; i < iterations; i++) {
            val bytes = new byte[17 + random.nextInt(Integer.MAX_VALUE - 16)];
            assertThrows(IllegalArgumentException.class, () -> UuidUtil.uuidFromBytes(bytes));
        }
    }

    @Test
    void testUuidToBytesAndOpposite() {
        val iterations = 32 + ThreadLocalRandom.current().nextInt(33);
        for (int i = 0; i < iterations; i++) {
            val uuid = UUID.randomUUID();
            assertThat(UuidUtil.uuidFromBytes(UuidUtil.uuidToBytes(uuid)), equalTo(uuid));
        }
    }

    @Test
    void testBytesToUuidAndOpposite() {
        val random = ThreadLocalRandom.current();
        var iterations = 32 + random.nextInt(33);
        for (int i = 0; i < iterations; i++) {
            val bytes = new byte[16];
            random.nextBytes(bytes);

            assertArrayEquals(bytes, UuidUtil.uuidToBytes(UuidUtil.uuidFromBytes(bytes)));
        }
    }
}