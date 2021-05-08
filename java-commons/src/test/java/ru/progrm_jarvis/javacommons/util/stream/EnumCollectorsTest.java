package ru.progrm_jarvis.javacommons.util.stream;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.progrm_jarvis.javacommons.collection.MapFiller;

import java.util.*;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class EnumCollectorsTest {

    static @NotNull Stream<@NotNull Arguments> enumNameMaps() {
        return Stream.of(
                arguments(
                        Stream.empty(),
                        Collections.emptyMap()
                ),
                arguments(
                        Stream.of("A", "B", "C"),
                        MapFiller.from(new EnumMap<>(MyEnum.class))
                                .put(MyEnum.A, "<A>")
                                .put(MyEnum.B, "<B>")
                                .put(MyEnum.C, "<C>")
                                .map()
                )
        );
    }

    static @NotNull Stream<@NotNull Arguments> enumMaps() {
        return Stream.of(
                arguments(
                        Stream.empty(),
                        Collections.emptyMap()
                ),
                arguments(
                        Stream.of(MyEnum.A, MyEnum.B, MyEnum.C),
                        MapFiller.from(new EnumMap<>(MyEnum.class))
                                .put(MyEnum.A, "<A>")
                                .put(MyEnum.B, "<B>")
                                .put(MyEnum.C, "<C>")
                                .map()
                )
        );
    }

    static @NotNull Stream<@NotNull Arguments> enumNameSets() {
        return Stream.of(
                arguments(
                        Stream.empty(),
                        Collections.emptySet()
                ),
                arguments(
                        Stream.of("A", "B", "C"),
                        EnumSet.of(MyEnum.A, MyEnum.B, MyEnum.C)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("enumNameMaps")
    void toEnumMap_full(final @NonNull Stream<@NotNull String> enumNames,
                        final @NonNull Map<@NotNull MyEnum, @NotNull Integer> result) {
        assertThat(
                enumNames.collect(EnumCollectors.toEnumMap(
                        MyEnum.class, MyEnum::valueOf, name -> "<" + name + ">", (left, right) -> {
                            throw new IllegalArgumentException(
                                    "Duplicate values provided in test case " + left + " and " + right);
                        }
                )).entrySet(),
                containsInAnyOrder(result.entrySet().toArray())
        );
    }

    @ParameterizedTest
    @MethodSource("enumNameMaps")
    void toEnumMap_full_viaHint(final @NonNull Stream<@NotNull String> enumNames,
                        final @NonNull Map<@NotNull MyEnum, @NotNull Integer> result) {
        assertThat(
                enumNames.collect(EnumCollectors.toEnumMap(
                        MyEnum::valueOf,
                        name -> "<" + name + ">",
                        (left, right) -> {
                            throw new IllegalArgumentException(
                                    "Duplicate values provided in test case " + left + " and " + right);
                        }
                )).entrySet(),
                containsInAnyOrder(result.entrySet().toArray())
        );
    }

    @ParameterizedTest
    @MethodSource("enumNameMaps")
    void toEnumMap_noMerger(final @NonNull Stream<@NotNull String> enumNames,
                            final @NonNull Map<@NotNull MyEnum, @NotNull Integer> result) {
        assertThat(
                enumNames.collect(EnumCollectors.toEnumMap(
                        MyEnum.class, MyEnum::valueOf,
                        name -> "<" + name + ">"
                )).entrySet(),
                containsInAnyOrder(result.entrySet().toArray())
        );
    }

    @ParameterizedTest
    @MethodSource("enumMaps")
    void toEnumMap_noKeyMapper(final @NonNull Stream<@NotNull MyEnum> enumNames,
                               final @NonNull Map<@NotNull MyEnum, @NotNull Integer> result) {
        assertThat(
                enumNames.collect(EnumCollectors.toEnumMap(
                        MyEnum.class, name -> "<" + name + ">", (left, right) -> {
                            throw new IllegalArgumentException(
                                    "Duplicate values provided in test case " + left + " and " + right);
                        }
                )).entrySet(),
                containsInAnyOrder(result.entrySet().toArray())
        );
    }

    @ParameterizedTest
    @MethodSource("enumMaps")
    void toEnumMap_noKeyMapper_viaHint(final @NonNull Stream<@NotNull MyEnum> enumNames,
                                       final @NonNull Map<@NotNull MyEnum, @NotNull Integer> result) {
        assertThat(
                enumNames.collect(EnumCollectors.toEnumMap(
                        name -> "<" + name + ">",
                        (left, right) -> {
                            throw new IllegalArgumentException(
                                    "Duplicate values provided in test case " + left + " and " + right);
                        }
                )).entrySet(),
                containsInAnyOrder(result.entrySet().toArray())
        );
    }

    @ParameterizedTest
    @MethodSource("enumMaps")
    void toEnumMap_noKeyMapperNoMerger(final @NonNull Stream<@NotNull MyEnum> enumNames,
                                       final @NonNull Map<@NotNull MyEnum, @NotNull Integer> result) {
        assertThat(
                enumNames.collect(EnumCollectors.toEnumMap(
                        MyEnum.class, name -> "<" + name + ">"
                )).entrySet(),
                containsInAnyOrder(result.entrySet().toArray())
        );
    }

    @ParameterizedTest
    @MethodSource("enumNameSets")
    void toEnumSet(final @NonNull Stream<@NotNull String> enumNames,
                   final @NonNull Set<@NotNull MyEnum> result) {
        assertEquals(
                enumNames.map(MyEnum::valueOf).collect(EnumCollectors.toEnumSet(MyEnum.class)),
                result
        );
    }

    @ParameterizedTest
    @MethodSource("enumNameSets")
    void toEnumSet_viaHint(final @NonNull Stream<@NotNull String> enumNames,
                           final @NonNull Set<@NotNull MyEnum> result) {
        assertEquals(
                enumNames.map(MyEnum::valueOf).collect(EnumCollectors.toEnumSet()),
                result
        );
    }

    private enum MyEnum {
        A,
        B,
        C
    }
}