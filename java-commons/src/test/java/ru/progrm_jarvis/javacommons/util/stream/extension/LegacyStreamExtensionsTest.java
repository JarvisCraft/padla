package ru.progrm_jarvis.javacommons.util.stream.extension;

import lombok.experimental.ExtensionMethod;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtensionMethod(LegacyStreamExtensions.class)
class LegacyStreamExtensionsTest {

    static @NotNull Stream<@NotNull Arguments> provideLists() {
        //noinspection DynamicRegexReplaceableByCompiledPattern: called in test for
        return Stream.of(
                Collections.emptyList(),
                Collections.singletonList("something"),
                Collections.singletonList(null),
                Arrays.asList("one", "two", "three"),
                Arrays.asList(
                        "Hello static world full of errors and exceptions through which you can pass through"
                                .split("\\s")
                ),
                Arrays.asList(
                        null, "some value", null, null, "one", "always", null, "if", "one is", "one"
                )
        ).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("provideLists")
    void toList_fromOrderedSource(final @NotNull Collection<?> original) {
        assertEquals(original.stream().toList(), original);
    }

    @ParameterizedTest
    @MethodSource("provideLists")
    void toList_fromUnorderedSource(final @NotNull Collection<?> original) {
        val set = new HashSet<>(original);
        assertThat(set.stream().toList(), containsInAnyOrder(set.toArray()));
    }
}