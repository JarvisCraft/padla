package ru.progrm_jarvis.javacommons.util;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.BitSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ClassUtilTest {

    protected static Stream<Arguments> providePrimitives() {
        return Stream.of(
                arguments(boolean.class),
                arguments(byte.class),
                arguments(char.class),
                arguments(short.class),
                arguments(int.class),
                arguments(long.class),
                arguments(float.class),
                arguments(double.class)
        );
    }

    protected static Stream<Arguments> providePrimitiveWrappers() {
        return Stream.of(
                arguments(Boolean.class),
                arguments(Byte.class),
                arguments(Character.class),
                arguments(Short.class),
                arguments(Integer.class),
                arguments(Long.class),
                arguments(Float.class),
                arguments(Double.class)
        );
    }

    protected static Stream<Arguments> provideNonPrimitiveOrWrapperClasses() {
        return Stream.of(
                arguments(Object.class),
                arguments(String.class),
                arguments(BigInteger.class),
                arguments(BigDecimal.class),
                arguments(BitSet.class),
                arguments(ClassUtilTest.class)
        );
    }

    protected static Stream<Arguments> providePrimitivesToPrimitiveWrappers() {
        return Stream.of(
                arguments(boolean.class, Boolean.class),
                arguments(byte.class, Byte.class),
                arguments(char.class, Character.class),
                arguments(short.class, Short.class),
                arguments(int.class, Integer.class),
                arguments(long.class, Long.class),
                arguments(float.class, Float.class),
                arguments(double.class, Double.class)
        );
    }

    @ParameterizedTest
    @MethodSource("providePrimitiveWrappers")
    void testIsPrimitiveWrapperWithPrimitiveWrappers(@NotNull final Class<?> primitiveWrapper) {
        assertTrue(ClassUtil.isPrimitiveWrapper(primitiveWrapper));
    }

    @ParameterizedTest
    @MethodSource("providePrimitives")
    void testIsPrimitiveWrapperWithPrimitives(@NotNull final Class<?> primitive) {
        assertFalse(ClassUtil.isPrimitiveWrapper(primitive));
    }

    @ParameterizedTest
    @MethodSource("provideNonPrimitiveOrWrapperClasses")
    void testIsPrimitiveWrapperWithNonPrimitiveOrWrapperClasses(@NotNull final Class<?> nonPrimitiveOrWrapper) {
        assertFalse(ClassUtil.isPrimitiveWrapper(nonPrimitiveOrWrapper));
    }

    @ParameterizedTest
    @MethodSource("providePrimitivesToPrimitiveWrappers")
    void testToPrimitiveWrapperWithValid(@NotNull final Class<?> primitive, @NotNull final Class<?> wrapper) {
        assertSame(wrapper, ClassUtil.toPrimitiveWrapper(primitive));
    }

    @ParameterizedTest
    @MethodSource("providePrimitiveWrappers")
    void testToPrimitiveWrapperWithPrimitiveWrappers( @NotNull final Class<?> wrapper) {
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.toPrimitiveWrapper(wrapper));
    }

    @ParameterizedTest
    @MethodSource("provideNonPrimitiveOrWrapperClasses")
    void testToPrimitiveWrapperWithNonPrimitiveOrWrapperClasses(@NotNull final Class<?> nonPrimitiveOrWrapper) {
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.toPrimitiveWrapper(nonPrimitiveOrWrapper));
    }

    @ParameterizedTest
    @MethodSource("providePrimitivesToPrimitiveWrappers")
    void testToPrimitiveWithValid(@NotNull final Class<?> primitive, @NotNull final Class<?> wrapper) {
        assertSame(primitive, ClassUtil.toPrimitive(wrapper));
    }

    @ParameterizedTest
    @MethodSource("providePrimitives")
    void testToPrimitiveWithPrimitives(@NotNull final Class<?> primitive) {
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.toPrimitive(primitive));
    }

    @ParameterizedTest
    @MethodSource("provideNonPrimitiveOrWrapperClasses")
    void testToPrimitiveWithNonPrimitiveOrWrapperClasses(@NotNull final Class<?> nonPrimitiveOrWrapper) {
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.toPrimitive(nonPrimitiveOrWrapper));
    }
}