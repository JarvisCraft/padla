package ru.progrm_jarvis.padla.annotation.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Naive implementation of placeholders.
 */
@UtilityClass
public class TrivialPlaceholders {

    /**
     * Character used to indicate the beginning of the placeholder.
     */
    public final char PLACEHOLDER_PREFIX = '{';

    /**
     * Character used to indicate the ending of the placeholder.
     */
    public final char PLACEHOLDER_SUFFIX = '}';

    /**
     * <p>Replaces the placeholders in the original string.</p>
     * <p>Placeholder's key is the value between {@link #PLACEHOLDER_PREFIX}
     * and (the closest) {@link #PLACEHOLDER_PREFIX}.
     * If another {@link #PLACEHOLDER_PREFIX} appears before any {@link #PLACEHOLDER_SUFFIX}
     * then it is considered the part of placeholder key.</p>
     *
     * @param original string to be formatted
     * @param placeholders placeholders to be replaced in the string
     * @return formatted string
     */
    public @NotNull String replace(final @NotNull String original,
                                   final @NotNull Map<@NotNull String, @NotNull String> placeholders) {
        final int length;
        val result = new StringBuilder(length = original.length());

        var substringStart = 0;
        {
            var beginIndex = -1;
            while ((beginIndex = original.indexOf(PLACEHOLDER_PREFIX, beginIndex + 1)) >= 0) {
                final int endIndex;
                if ((endIndex = original.indexOf(PLACEHOLDER_SUFFIX, beginIndex + 1)) >= 0) {
                    val endIndexPlus1 = endIndex + 1;
                    final String replacement;
                    if ((replacement = placeholders.get(original.substring(beginIndex + 1, endIndex))) != null) result
                            .append(original, substringStart, beginIndex).append(replacement);
                    else result.append(original, substringStart, endIndexPlus1);

                    substringStart = endIndexPlus1;
                }
            }
        }

        if (substringStart != length) result.append(original, substringStart, length);

        return result.toString();
    }
}
