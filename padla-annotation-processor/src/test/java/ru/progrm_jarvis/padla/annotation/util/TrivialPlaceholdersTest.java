package ru.progrm_jarvis.padla.annotation.util;

import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TrivialPlaceholdersTest {

    private static @NotNull Map<@NotNull String, @NotNull String> placeholders() {
        return new HashMap<>();
    }

    private static @NotNull Map<@NotNull String, @NotNull String> placeholders(
            final @NonNull String key, final @NonNull String value
    ) {
        val placeholders = new HashMap<String, String>();
        placeholders.put(key, value);

        return placeholders;
    }

    private static @NotNull Map<@NotNull String, @NotNull String> placeholders(
            final @NonNull String key1, final @NonNull String value1,
            final @NonNull String key2, final @NonNull String value2
    ) {
        val placeholders = new HashMap<String, String>();
        placeholders.put(key1, value1);
        placeholders.put(key2, value2);

        return placeholders;
    }

    static @NotNull Stream<@NotNull Arguments> provideStringsWithPlaceholders() {
        return Stream.of(
                arguments("foo", "foo", placeholders()),
                arguments("foo{x}bar", "foo{x}bar", placeholders()),
                arguments("foo{x}bar", "foo bar", placeholders("x", " ")),
                arguments("abc{insertion}ghi{insertion}", "abcdefghidef", placeholders("insertion", "def")),
                arguments("okay {rnd}", "okay {rnd}", placeholders()),
                arguments("oh, {unbalanced", "oh, {unbalanced", placeholders()),
                arguments("oh, {unbalanced", "oh, {unbalanced", placeholders("unbalanced", "nope")),
                arguments("a->{a}, b->{b}, c->{c}", "a->A, b->B, c->{c}", placeholders("a", "A", "b", "B")),
                arguments("a->{a}, x->{x, b->{B}", "a->A, x->{x, b->{B}", placeholders("a", "A", "b", "B"))
        );
    }

    @ParameterizedTest
    @MethodSource("provideStringsWithPlaceholders")
    void testReplacement(
            final @NotNull String original,
            final @NotNull String expected,
            final @NotNull Map<@NotNull String, @NotNull String> placeholders
    ) {
        assertEquals(expected, TrivialPlaceholders.replace(original, placeholders));
    }
}