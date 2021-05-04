package ru.progrm_jarvis.javacommons.util.stream;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.progrm_jarvis.javacommons.collection.MapFiller;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class EnumCollectorsTest {

    static @NotNull Stream<@NotNull Arguments> enumNameMaps() {
        return Stream.of(
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
                        Stream.of(MyEnum.A, MyEnum.B, MyEnum.C),
                        MapFiller.from(new EnumMap<>(MyEnum.class))
                                .put(MyEnum.A, "<A>")
                                .put(MyEnum.B, "<B>")
                                .put(MyEnum.C, "<C>")
                                .map()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("enumNameMaps")
    void toEnumMapFull(final @NonNull Stream<@NotNull String> enumNames,
                       final @NonNull Map<@NotNull MyEnum, @NotNull Integer> result) {
        assertThat(
                enumNames.collect(EnumCollectors.toEnumMap(
                        MyEnum.class, MyEnum::valueOf,
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
    void toEnumMapNoMerger(final @NonNull Stream<@NotNull String> enumNames,
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
    void toEnumMapNoKeyMapper(final @NonNull Stream<@NotNull MyEnum> enumNames,
                              final @NonNull Map<@NotNull MyEnum, @NotNull Integer> result) {
        assertThat(
                enumNames.collect(EnumCollectors.toEnumMap(
                        MyEnum.class,
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
    void toEnumMapNoKeyMapperNoMerger(final @NonNull Stream<@NotNull MyEnum> enumNames,
                                      final @NonNull Map<@NotNull MyEnum, @NotNull Integer> result) {
        assertThat(
                enumNames.collect(EnumCollectors.toEnumMap(
                        MyEnum.class,
                        name -> "<" + name + ">"
                )).entrySet(),
                containsInAnyOrder(result.entrySet().toArray())
        );
    }

    private enum MyEnum {
        A,
        B,
        C
    }
}