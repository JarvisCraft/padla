package ru.progrm_jarvis.javacommons.primitive;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.object.Result;
import ru.progrm_jarvis.javacommons.primitive.error.IntegerParseError;

import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Utilities related to numbers.
 */
@UtilityClass
public class NumberUtil {

    /**
     * Number of bits in an {@code int} except for the sign bit.
     */
    public final int INT_NON_SIGN_BITS = Integer.SIZE - 1;

    /**
     * Number of bits in a {@code long} except for the sign bit.
     */
    public final int LONG_NON_SIGN_BITS = Long.SIZE - 1;

    /**
     * Default radix of a number.
     */
    public final int DEFAULT_RADIX = 10;

    /**
     * <p>Parses an {@code int} number.</p>
     * <p>This method is based on OpenJDK {@link Integer#parseInt(String, int)}
     * but it returns {@link OptionalInt} to handle invalid input instead of throwing {@link NumberFormatException}.
     *
     * @param possibleInteger string which is expected to contain an {@code int} number but may not
     * @param radix radix of the possible number
     *
     * @return optional containing the parsed number or an empty one if it could not be parsed
     * @throws IllegalArgumentException if the radix
     * is out of bound <i>[{@link Character#MIN_RADIX}; {@link Character#MAX_RADIX}]</i>.
     */
    public @NotNull OptionalInt parseInt(final @NonNull CharSequence possibleInteger, final int radix) {
        checkRadix(radix);

        // check string length
        final int length;
        if ((length = possibleInteger.length()) == 0) return OptionalInt.empty();

        final boolean negative;
        final int limit;
        int currentIndex = 0, digit;
        { // check sign by first char (may be implicit '+')
            val firstChar = possibleInteger.charAt(0);

            if (firstChar < '0') { // first char is definitely not a digit
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

                // it is not a number if there is only a sign without digits
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
        int negativeResult = 0;
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

        return OptionalInt.of(negative ? negativeResult : -negativeResult);
    }


    /**
     * <p>Parses a decimal {@code int} number.</p>
     * <p>This method is based on OpenJDK {@link Integer#parseInt(String, int)}
     * but it returns {@link OptionalInt} to handle invalid input instead of throwing {@link NumberFormatException}.
     *
     * @param possibleInteger string which is expected to contain an {@code int} number but may not
     *
     * @return optional containing the parsed number or an empty one if it could not be parsed
     * @throws IllegalArgumentException if the radix
     * is out of bound <i>[{@link Character#MIN_RADIX}; {@link Character#MAX_RADIX}]</i>.
     */
    public @NotNull OptionalInt parseInt(final @NonNull CharSequence possibleInteger) {
        return parseInt(possibleInteger, 10);
    }

    /**
     * <p>Parses an {@code int} number.</p>
     * <p>This method is based on OpenJDK {@link Integer#parseInt(String, int)}
     * but it returns {@link Result} to handle invalid input instead of throwing {@link NumberFormatException}.
     *
     * @param possibleInteger string which is expected to contain an {@code int} number but may not
     * @param radix radix of the possible number
     *
     * @return result containing the parsed number or an error one if it could not be parsed
     * @throws IllegalArgumentException if the radix
     * is out of bound <i>[{@link Character#MIN_RADIX}; {@link Character#MAX_RADIX}]</i>.
     */
    public @NotNull Result<@NotNull Integer, @NotNull IntegerParseError> parseIntResult(
            final @NonNull CharSequence possibleInteger, final int radix
    ) {
        checkRadix(radix);

        final int length;

        // check string length
        if ((length = possibleInteger.length()) == 0) return Result.error(IntegerParseError.EMPTY);

        final boolean negative;
        final int limit;
        int currentIndex = 0, digit;
        { // check sign by first char (may be implicit '+')
            val firstChar = possibleInteger.charAt(0);

            if (firstChar < '0') { // first char is definitely not a digit
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
                    default: return Result.error(IntegerParseError.INVALID_CHARACTER);
                }

                /* sign found */

                // it is not a number if there is only a sign without digits
                if (length == 1) return Result.error(IntegerParseError.EMPTY);
                currentIndex = 1;
                digit = Character.digit(possibleInteger.charAt(1), radix);
            } else { // first char is a digit
                negative = false;
                limit = -Integer.MAX_VALUE;
                digit = Character.digit(firstChar, radix);
            }
        }

        final int bound = limit / radix, lastIndex = length - 1;
        int negativeResult = 0;
        while (true) {
            // check if it really is a digit
            if (digit < 0) return Result.error(IntegerParseError.INVALID_CHARACTER);
            // check if corresponds to bound
            if (negativeResult < bound) return Result.error(IntegerParseError.OUT_OF_BOUNDS);
            // move the pre-number to the left leaving a last zero for a new sign
            negativeResult *= radix;
            // check if the number is OK with the limit
            if (negativeResult < limit + digit) Result.error(IntegerParseError.INVALID_CHARACTER);
            // update the result
            negativeResult -= digit;

            // check it was the last character
            if (currentIndex == lastIndex) break;
            // if it was not the last character then read the next one
            digit = Character.digit(possibleInteger.charAt(++currentIndex), radix);
        }

        return Result.success(negative ? negativeResult : -negativeResult);
    }

    /**
     * <p>Parses a decimal {@code int} number.</p>
     * <p>This method is based on OpenJDK {@link Integer#parseInt(String, int)}
     * but it returns {@link Result} to handle invalid input instead of throwing {@link NumberFormatException}.
     *
     * @param possibleInteger string which is expected to contain an {@code int} number but may not
     *
     * @return result containing the parsed number or an error one if it could not be parsed
     * @throws IllegalArgumentException if the radix
     * is out of bound <i>[{@link Character#MIN_RADIX}; {@link Character#MAX_RADIX}]</i>.
     */
    public @NotNull Result<@NotNull Integer, @NotNull IntegerParseError> parseIntResult(
            final @NonNull CharSequence possibleInteger
    ) {
        return parseIntResult(possibleInteger, DEFAULT_RADIX);
    }

    /**
     * <p>Parses a {@code long} number.</p>
     * <p>This method is based on OpenJDK {@link Long#parseLong(String, int)}
     * but it returns {@link OptionalLong} to handle invalid input instead of throwing {@link NumberFormatException}.
     *
     * @param possibleLong string which is expected to contain a {@code long} number but may not
     * @param radix radix of the possible number
     *
     * @return optional containing the parsed number or an empty one if it could not be parsed
     * @throws IllegalArgumentException if the radix
     * is out of bound <i>[{@link Character#MIN_RADIX}; {@link Character#MAX_RADIX}]</i>.
     */
    public @NotNull OptionalLong parseLong(final @NonNull CharSequence possibleLong, final int radix) {
        checkRadix(radix);

        // check string length
        final int length;
        if ((length = possibleLong.length()) == 0) return OptionalLong.empty();

        final boolean negative;
        final long limit;
        int currentIndex = 0, digit;
        { // check sign by first char (may be implicit '+')
            val firstChar = possibleLong.charAt(0);

            if (firstChar < '0') { // first char is definitely not a digit
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

                // it is not a number if there is only a sign without digits
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
        long negativeResult = 0;
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

        return OptionalLong.of(negative ? negativeResult : -negativeResult);
    }

    /**
     * <p>Parses a decimal {@code long} number.</p>
     * <p>This method is based on OpenJDK {@link Long#parseLong(String, int)}
     * but it returns {@link OptionalLong} to handle invalid input instead of throwing {@link NumberFormatException}.
     *
     * @param possibleLong string which is expected to contain a {@code long} number but may not
     *
     * @return optional containing the parsed number or an empty one if it could not be parsed
     * @throws IllegalArgumentException if the radix
     * is out of bound <i>[{@link Character#MIN_RADIX}; {@link Character#MAX_RADIX}]</i>.
     */
    public @NotNull OptionalLong parseLong(final @NonNull CharSequence possibleLong) {
        return parseLong(possibleLong, DEFAULT_RADIX);
    }

    /**
     * <p>Parses a {@code long} number.</p>
     * <p>This method is based on OpenJDK {@link Long#parseLong(String, int)}
     * but it returns {@link Result} to handle invalid input instead of throwing {@link NumberFormatException}.
     *
     * @param possibleLong string which is expected to contain a {@code long} number but may not
     * @param radix radix of the possible number
     *
     * @return result containing the parsed number or an error one if it could not be parsed
     * @throws IllegalArgumentException if the radix
     * is out of bound <i>[{@link Character#MIN_RADIX}; {@link Character#MAX_RADIX}]</i>.
     */
    public @NotNull Result<@NotNull Long, @NotNull IntegerParseError> parseLongResult(
            final @NonNull CharSequence possibleLong, final int radix
    ) {
        checkRadix(radix);

        // check string length
        final int length;
        if ((length = possibleLong.length()) == 0) return Result.error(IntegerParseError.EMPTY);

        final boolean negative;
        final long limit;
        int currentIndex = 0, digit;
        { // check sign by first char (may be implicit '+')
            val firstChar = possibleLong.charAt(0);

            if (firstChar < '0') { // first char is definitely not a digit
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
                    default: return Result.error(IntegerParseError.INVALID_CHARACTER);
                }

                /* sign found */

                // it is not a number if there is only a sign without digits
                if (length == 1) return Result.error(IntegerParseError.EMPTY);
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
        long negativeResult = 0;
        while (true) {
            // check if it really is a digit
            if (digit < 0) return Result.error(IntegerParseError.INVALID_CHARACTER);
            // check if corresponds to bound
            if (negativeResult < bound) return Result.error(IntegerParseError.OUT_OF_BOUNDS);
            // move the pre-number to the left leaving a last zero for a new sign
            negativeResult *= radix;
            // check if the number is OK with the limit
            if (negativeResult < limit + digit) return Result.error(IntegerParseError.OUT_OF_BOUNDS);
            // update the result
            negativeResult -= digit;

            // check it was the last character
            if (currentIndex == lastIndex) break;
            // if it was not the last character then read the next one
            digit = Character.digit(possibleLong.charAt(++currentIndex), radix);
        }

        return Result.success(negative ? negativeResult : -negativeResult);
    }

    /**
     * <p>Parses a decimal {@code long} number.</p>
     * <p>This method is based on OpenJDK {@link Long#parseLong(String, int)}
     * but it returns {@link Result} to handle invalid input instead of throwing {@link NumberFormatException}.
     *
     * @param possibleLong string which is expected to contain a {@code long} number but may not
     *
     * @return result containing the parsed number or an error one if it could not be parsed
     * @throws IllegalArgumentException if the radix
     * is out of bound <i>[{@link Character#MIN_RADIX}; {@link Character#MAX_RADIX}]</i>.
     */
    public @NotNull Result<@NotNull Long, @NotNull IntegerParseError> parseLongResult(
            final @NonNull CharSequence possibleLong
    ) {
        return parseLongResult(possibleLong, DEFAULT_RADIX);
    }

    // the following is based on: https://stackoverflow.com/a/2633161/6277184

    /**
     * Sums the {@code int} values using their bounds instead of wrapping on overflow.
     *
     * @param left left addend
     * @param right right addend
     * @return sum of the addends
     *
     * @implNote implementation is based
     * on <a href="https://stackoverflow.com/a/2633161/6277184">Stackoverflow answer</a>
     */
    public int saturatingSum(final int left, final int right) {
        final int sum;
        return (Integer.MIN_VALUE ^ (sum = left + right) >> INT_NON_SIGN_BITS ^ sum)
                & ((left ^ sum) & ~(left ^ right)) >> INT_NON_SIGN_BITS ^ sum;
    }

    /**
     * Sums the {@code long} values using their bounds instead of wrapping on overflow.
     *
     * @param left left addend
     * @param right right addend
     * @return sum of the addends
     *
     * @implNote implementation is based
     * on <a href="https://stackoverflow.com/a/2633161/6277184">Stackoverflow answer</a>
     */
    public long saturatingSum(final long left, final long right) {
        final long sum;
        return (Long.MIN_VALUE ^ (sum = left + right) >> LONG_NON_SIGN_BITS ^ sum)
                & ((left ^ sum) & ~(left ^ right)) >> LONG_NON_SIGN_BITS ^ sum;
    }

    /**
     * Checks that the radix is between {@link Character#MIN_RADIX} and {@link Character#MAX_RADIX}.
     *
     * @param radix checked radix
     *
     * @throws IllegalArgumentException if {@code radix} is not between the bounds
     */
    private void checkRadix(final int radix) {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) throw new IllegalArgumentException(
                "Radix " + radix + " is out of bounds [" + Character.MIN_RADIX + "; " + Character.MAX_RADIX + "]"
        );
    }
}
