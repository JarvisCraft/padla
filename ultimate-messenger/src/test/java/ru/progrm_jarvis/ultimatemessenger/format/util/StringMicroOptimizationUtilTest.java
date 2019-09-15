package ru.progrm_jarvis.ultimatemessenger.format.util;

import lombok.val;
import lombok.var;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class StringMicroOptimizationUtilTest {

    @Test
    void testStringToCharArray() {
        val random = ThreadLocalRandom.current();

        val attempts = random.nextInt(128, 256 + 1);
        for (var i = 0; i < attempts; i++) {
            val string = Integer.toString(random.nextInt(), Character.MAX_RADIX);

            assertArrayEquals(StringMicroOptimizationUtil.getStringChars(string), string.toCharArray());
        }
    }

    @Test
    void testEscapeJavaStringLiteral() {
        assertThat(
                StringMicroOptimizationUtil.escapeJavaStringLiteral("Hello world"),
                equalTo("Hello world")
        );
        assertThat(
                StringMicroOptimizationUtil.escapeJavaStringLiteral("Hello world!"),
                equalTo("Hello world!")
        );
        assertThat(
                StringMicroOptimizationUtil.escapeJavaStringLiteral("Hello \nworld!"),
                equalTo("Hello \\nworld!")
        );
        assertThat(
                StringMicroOptimizationUtil.escapeJavaStringLiteral("Hello \nw\borld!"),
                equalTo("Hello \\nw\\borld!")
        );
        assertThat(
                StringMicroOptimizationUtil.escapeJavaStringLiteral("\b\t\r\f\n\'\"\\"),
                equalTo("\\b\\t\\r\\f\\n\\'\\\"\\\\")
        );
    }
}