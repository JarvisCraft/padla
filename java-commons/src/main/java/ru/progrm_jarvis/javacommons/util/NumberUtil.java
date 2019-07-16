package ru.progrm_jarvis.javacommons.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Utilities related to numbers.
 */
@UtilityClass
public class NumberUtil {

    /**
     * Parses an {@code int} number.
     * <p>
     * This method is based on OpenJDK {@link Integer#parseInt(String, int)} but it uses {@link OptionalInt} instead of
     * throwing {@link NumberFormatException} and so it is a more optimized equivalent.
     *
     * @param possibleInteger string which is expected to contain an {@code int} number but may not
     * @param radix radix of the possible number
     * @return optional containing the parsed number or an empty one if it could not be parsed
     */
    public OptionalInt parseInt(@NonNull final String possibleInteger, final int radix) {
        // check radix bounds
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) return OptionalInt.empty();

        final boolean negative;
        int negativeResult = 0;
        val length = possibleInteger.length();

        // check string length
        if (length > 0) {
            final int limit;
            int currentIndex = 0, digit;
            { // check sign by first char (may be implicit '+')
                val firstChar = possibleInteger.charAt(0);

                if (firstChar < '0') { // first char is not a digit
                    switch (firstChar) {
                        case '-': {
                            negative = true;
                            limit = Integer.MIN_VALUE;
                            break;
                        }
                        case '+': {
                            negative = false;
                            limit = -Integer.MAX_VALUE;
                            break;
                        }
                        default: return OptionalInt.empty();
                    }

                    /* sign found */

                    // it is not a number if there is only sign without digits
                    if (length == 1) return OptionalInt.empty();
                    currentIndex = 1;
                    digit = Character.digit(possibleInteger.charAt(1), radix);
                } else { // first char is a digit
                    negative = false;
                    limit = -Integer.MAX_VALUE;
                    digit = Character.digit(firstChar, radix);
                }
            }

            final int bound = limit / radix, lastIndex = length - 1;
            while (true) {
                // check if it really is a digit
                if (digit < 0) return OptionalInt.empty();
                // check if corresponds to bound
                if (negativeResult < bound) return OptionalInt.empty();
                // move the pre-number to the left leaving a last zero for a new sign
                negativeResult *= radix;
                // check if the number is OK with the limit
                if (negativeResult < limit + digit) return OptionalInt.empty();
                // update the result
                negativeResult -= digit;

                // check it was the last character
                if (currentIndex == lastIndex) break;
                // if it was not the last character then read the next one
                digit = Character.digit(possibleInteger.charAt(++currentIndex), radix);
            }
        } else return OptionalInt.empty();

        return OptionalInt.of(negative ? negativeResult : -negativeResult);
    }

    /**
     * Parses an {@code int} number with base {@code 10}.
     *
     * @param possibleInteger string which is expected to contain an {@code int} number but may not
     * @return optional containing the parsed number or an empty one if it could not be parsed
     */
    public OptionalInt parseInt(@NonNull final String possibleInteger) {
        return parseInt(possibleInteger, 10);
    }

    /**
     * Parses a {@code long} number.
     * <p>
     * This method is based on OpenJDK {@link Long#parseLong(String)} but it uses {@link OptionalLong} instead of
     * throwing {@link NumberFormatException} and so it is a more optimized equivalent.
     *
     * @param possibleLong string which is expected to contain a {@code long} number but may not
     * @param radix radix of the possible number
     * @return optional containing the parsed number or an empty one if it could not be parsed
     */
    public OptionalLong parseLong(@NonNull final String possibleLong, final int radix) {
        // check radix bounds
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) return OptionalLong.empty();

        final boolean negative;
        long negativeResult = 0;
        val length = possibleLong.length();

        // check string length
        if (length > 0) {
            final long limit;
            int currentIndex = 0, digit;
            { // check sign by first char (may be implicit '+')
                val firstChar = possibleLong.charAt(0);

                if (firstChar < '0') { // first char is not a digit
                    switch (firstChar) {
                        case '-': {
                            negative = true;
                            limit = Long.MIN_VALUE;
                            break;
                        }
                        case '+': {
                            negative = false;
                            limit = -Long.MAX_VALUE;
                            break;
                        }
                        default: return OptionalLong.empty();
                    }

                    /* sign found */

                    // it is not a number if there is only sign without digits
                    if (length == 1) return OptionalLong.empty();
                    currentIndex = 1;
                    digit = Character.digit(possibleLong.charAt(1), radix);
                } else { // first char is a digit
                    negative = false;
                    limit = -Long.MAX_VALUE;
                    digit = Character.digit(firstChar, radix);
                }
            }

            val bound = limit / radix;
            val lastIndex = length - 1;
            while (true) {
                // check if it really is a digit
                if (digit < 0) return OptionalLong.empty();
                // check if corresponds to bound
                if (negativeResult < bound) return OptionalLong.empty();
                // move the pre-number to the left leaving a last zero for a new sign
                negativeResult *= radix;
                // check if the number is OK with the limit
                if (negativeResult < limit + digit) return OptionalLong.empty();
                // update the result
                negativeResult -= digit;

                // check it was the last character
                if (currentIndex == lastIndex) break;
                // if it was not the last character then read the next one
                digit = Character.digit(possibleLong.charAt(++currentIndex), radix);
            }
        } else return OptionalLong.empty();

        return OptionalLong.of(negative ? negativeResult : -negativeResult);
    }

    /**
     * Parses an {@code long} number with base {@code 10}.
     *
     * @param possibleLong string which is expected to contain an {@code long} number but may not
     * @return optional containing the parsed number or an empty one if it could not be parsed
     */
    public OptionalLong parseLong(@NonNull final String possibleLong) {
        return parseLong(possibleLong, 10);
    }
}
