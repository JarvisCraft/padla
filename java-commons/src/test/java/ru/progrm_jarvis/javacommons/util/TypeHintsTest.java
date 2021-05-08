package ru.progrm_jarvis.javacommons.util;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.annotation.RetentionPolicy;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertSame;

class TypeHintsTest {

    @SafeVarargs
    private static <T> @NotNull Arguments typeHintWithType(final @NonNull Class<? extends T> type,
                                                           @TypeHints.TypeHint final @NonNull T... typeHint) {
        return Arguments.of(typeHint, type);
    }

    static Stream<Arguments> typeHintsWithTypes() {
        return Stream.of(
                // note: primitive types themselves are not applicable as they get wrapped
                typeHintWithType(Boolean.class),
                typeHintWithType(Byte.class),
                typeHintWithType(Short.class),
                typeHintWithType(Character.class),
                typeHintWithType(Integer.class),
                typeHintWithType(Long.class),
                typeHintWithType(Float.class),
                typeHintWithType(Double.class),
                typeHintWithType(String.class),
                typeHintWithType(RetentionPolicy.class)
        );
    }

    @ParameterizedTest
    @MethodSource("typeHintsWithTypes")
    void resolve(final @NonNull Object[] typeHint,
                 final @NonNull Class<?> type) {
        assertSame(type, TypeHints.resolve(typeHint));
    }
}