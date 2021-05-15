package ru.progrm_jarvis.javacommons.util;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.progrm_jarvis.javacommons.primitive.NumberUtil;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class NumberUtilTest {

    // number of random items tested
    private static final long RANDOM_NUMBERS = 4096;

    static @NotNull Stream<@NotNull Arguments> intsWithRadixes() {
        return ThreadLocalRandom.current().ints().limit(RANDOM_NUMBERS)
                .mapToObj(number -> {
                    val radix = ThreadLocalRandom.current().nextInt(Character.MIN_RADIX, Character.MAX_RADIX + 1);
                    return Arguments.of(number, Integer.toString(number, radix), radix);
                });
    }

    static @NotNull Stream<@NotNull Arguments> longsWithRadixes() {
        return ThreadLocalRandom.current().longs().limit(RANDOM_NUMBERS)
                .mapToObj(number -> {
                    val radix = ThreadLocalRandom.current().nextInt(Character.MIN_RADIX, Character.MAX_RADIX + 1);
                    return Arguments.of(number, Long.toString(number, radix), radix);
                });
    }

    @ParameterizedTest
    @MethodSource("intsWithRadixes")
    void testParseInt(final int number, final @NotNull CharSequence numberAsString, final int radix) {
        assertThat(
                NumberUtil.parseInt(numberAsString.toString(), radix).orElseThrow(AssertionError::new),
                is(number)
        );
    }

    @ParameterizedTest
    @MethodSource("intsWithRadixes")
    void testParseIntResult(final int number, final @NotNull CharSequence numberAsString, final int radix) {
        assertThat(
                NumberUtil.parseIntResult(numberAsString.toString(), radix).orElseThrow(AssertionError::new),
                is(number)
        );
    }

    @ParameterizedTest
    @MethodSource("intsWithRadixes")
    void testParseLong(final long number, final @NotNull CharSequence numberAsString, final int radix) {
        assertThat(
                NumberUtil.parseLong(numberAsString.toString(), radix).orElseThrow(AssertionError::new),
                is(number)
        );
    }

    @ParameterizedTest
    @MethodSource("intsWithRadixes")
    void testParseLongResult(final long number, final @NotNull CharSequence numberAsString, final int radix) {
        assertThat(
                NumberUtil.parseLongResult(numberAsString.toString(), radix).orElseThrow(AssertionError::new),
                is(number)
        );
    }
}