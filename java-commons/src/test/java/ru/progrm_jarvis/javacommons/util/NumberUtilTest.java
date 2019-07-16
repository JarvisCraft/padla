package ru.progrm_jarvis.javacommons.util;

import lombok.val;
import lombok.var;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class NumberUtilTest {

    @Test
    void testParseInt() {
        val random = ThreadLocalRandom.current();

        val radixDelta = Character.MAX_RADIX - Character.MIN_RADIX;
        int number, radix;
        StringBuilder numberAsString;
        for (var i = 0; i < 0xFFFF; i++) { // a lot of tests :)
            number = random.nextInt();
            radix = Character.MIN_RADIX + random.nextInt(radixDelta);

            // generate random number
            numberAsString = new StringBuilder(Integer.toString(number, radix));
            if (random.nextBoolean()) { // append sign with ~50% chance
                if (number == 0) numberAsString.append(random.nextBoolean() ? '+' : '-');
                else if (number > 0) numberAsString.insert(0, '+');
            }

            assertThat(
                    NumberUtil.parseInt(numberAsString.toString(), radix).orElseThrow(AssertionError::new),
                    is(number)
            );
        }
    }

    @Test
    void testParseLong() {
        val random = ThreadLocalRandom.current();

        val radixDelta = Character.MAX_RADIX - Character.MIN_RADIX;
        long number;
        int radix;
        StringBuilder numberAsString;
        for (var i = 0; i < 0xFFFF; i++) { // a lot of tests :)
            number = random.nextLong();
            radix = Character.MIN_RADIX + random.nextInt(radixDelta);

            // generate random number
            numberAsString = new StringBuilder(Long.toString(number, radix));
            if (random.nextBoolean()) { // append sign with ~50% chance
                if (number == 0) numberAsString.append(random.nextBoolean() ? '+' : '-');
                else if (number > 0) numberAsString.insert(0, '+');
            }

            assertThat(
                    NumberUtil.parseLong(numberAsString.toString(), radix).orElseThrow(AssertionError::new),
                    is(number)
            );
        }
    }
}