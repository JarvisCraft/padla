package ru.progrm_jarvis.javacommons.classloading.extension;

import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtensionMethod(LegacyClassExtensions.class)
class LegacyClassExtensionsTest {

    protected static Stream<Arguments> provideArraysAndElementTypes() {
        return Stream.of(
                boolean.class, byte.class, short.class, char.class, int.class, long.class,
                float.class, double.class,
                Object.class, String.class, RetentionPolicy.class, List.class, Set.class, Map.class, HashMap.class
        ).flatMap(arrayType -> {// make arrays
            Class<?> deeper;
            return Stream.of(
                    deeper = Array.newInstance(arrayType, 0).getClass(), // T[]
                    deeper = Array.newInstance(deeper, 0).getClass(), // T[][]
                    deeper = Array.newInstance(deeper, 0).getClass(), // T[][][]
                    deeper = Array.newInstance(deeper, 0).getClass(), // T[][][][]
                    Array.newInstance(deeper, 0).getClass() // T[][][][][]
            );
        }).map(arrayType -> arguments(arrayType, arrayType.getComponentType()));
    }

    @ParameterizedTest
    @MethodSource("provideArraysAndElementTypes")
    <T> void testArrayType(final @NotNull Class<T[]> arrayType, final @NotNull Class<T> elementType) {
        assertSame(arrayType, LegacyClassExtensions.arrayType(elementType));
    }
}