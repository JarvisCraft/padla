package ru.progrm_jarvis.javacommons.util;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ClassUtilTest {

    protected static Stream<Class<?>> primitiveClassesStream() {
        return Stream.of(
                boolean.class, byte.class, char.class, short.class,
                int.class, long.class, float.class, double.class
        );
    }

    protected static Stream<Class<?>> primitiveWrapperClassesStream() {
        return Stream.of(
                Boolean.class, Byte.class, Character.class, Short.class,
                Integer.class, Long.class, Float.class, Double.class
        );
    }

    protected static Stream<Arguments> providePrimitives() {
        return primitiveClassesStream().map(Arguments::arguments);
    }

    protected static Stream<Arguments> providePrimitiveWrappers() {
        return primitiveWrapperClassesStream().map(Arguments::arguments);
    }

    protected static Stream<Arguments> provideNonPrimitiveOrWrappers() {
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

    protected static Stream<Arguments> provideDifferentPrimitivePairs() {
        return primitiveClassesStream()
                .flatMap(primitive -> primitiveClassesStream()
                        .flatMap(otherPrimitive -> primitive == otherPrimitive
                                ? Stream.empty() : Stream.of(arguments(primitive, otherPrimitive))
                        )
                );
    }

    protected static Stream<Arguments> provideDifferentPrimitiveWrapperPairs() {
        return primitiveWrapperClassesStream()
                .flatMap(primitiveWrapper -> primitiveWrapperClassesStream()
                        .flatMap(otherPrimitive -> primitiveWrapper == otherPrimitive
                                ? Stream.empty() : Stream.of(arguments(primitiveWrapper, otherPrimitive))
                        )
                );
    }

    protected static Stream<Arguments> provideOneSideIntegrableNonPrimitivePairs() {
        return Stream.of(
                arguments(String.class, CharSequence.class),
                arguments(StringBuilder.class, CharSequence.class),
                arguments(StringBuffer.class, CharSequence.class),
                arguments(ArrayList.class, List.class),
                arguments(ArrayList.class, Collection.class),
                arguments(HashSet.class, Set.class),
                arguments(HashSet.class, Collection.class),
                arguments(HashMap.class, Map.class)
        );
    }

    protected static Stream<Arguments> provideUnassignablePairs() {
        return Stream.of(
                arguments(String.class, ArrayList.class),
                arguments(String.class, HashSet.class),
                arguments(String.class, Map.class),
                arguments(ClassUtilTest.class, String.class),
                arguments(ClassUtilTest.class, ArrayList.class),
                arguments(ClassUtilTest.class, HashSet.class),
                arguments(ClassUtilTest.class, Map.class)
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
    @MethodSource("provideNonPrimitiveOrWrappers")
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
    void testToPrimitiveWrapperWithPrimitiveWrappers(@NotNull final Class<?> wrapper) {
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.toPrimitiveWrapper(wrapper));
    }

    @ParameterizedTest
    @MethodSource("provideNonPrimitiveOrWrappers")
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
    @MethodSource("provideNonPrimitiveOrWrappers")
    void testToPrimitiveWithNonPrimitiveOrWrapperClasses(@NotNull final Class<?> nonPrimitiveOrWrapper) {
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.toPrimitive(nonPrimitiveOrWrapper));
    }

    @ParameterizedTest
    @MethodSource("providePrimitives")
    void testIntegrateTypeWithPrimitiveTypes(@NonNull final Class<?> primitive) {
        assertTrue(primitive.isAssignableFrom(
                ClassUtil.integrateType(primitive, primitive))
        );
        assertTrue(Object.class.isAssignableFrom(
                ClassUtil.integrateType(primitive, Object.class))
        );
    }

    @ParameterizedTest
    @MethodSource("providePrimitiveWrappers")
    void testIntegrateTypeWithPrimitiveWrapperTypes(@NonNull final Class<?> primitiveWrapper) {
        assertTrue(primitiveWrapper.isAssignableFrom(
                ClassUtil.integrateType(primitiveWrapper, primitiveWrapper)
        ));
        assertTrue(Object.class.isAssignableFrom(
                ClassUtil.integrateType(primitiveWrapper, Object.class)
        ));
    }

    @ParameterizedTest
    @MethodSource("provideNonPrimitiveOrWrappers")
    void testIntegrateTypeWithNonPrimitiveOrWrapperClasses(@NonNull final Class<?> nonPrimitiveOrWrappers) {
        assertTrue(nonPrimitiveOrWrappers.isAssignableFrom(
                ClassUtil.integrateType(nonPrimitiveOrWrappers, nonPrimitiveOrWrappers)
        ));
        assertTrue(Object.class.isAssignableFrom(
                ClassUtil.integrateType(nonPrimitiveOrWrappers, Object.class)
        ));
    }

    @ParameterizedTest
    @MethodSource("providePrimitivesToPrimitiveWrappers")
    void testIntegrateTypeWithPrimitivesToPrimitiveWrappers(@NonNull final Class<?> primitive,
                                                            @NotNull final Class<?> primitiveWrapper) {
        assertTrue(primitiveWrapper.isAssignableFrom(ClassUtil.integrateType(primitive, primitiveWrapper)));
    }

    @ParameterizedTest
    @MethodSource("providePrimitivesToPrimitiveWrappers")
    void testIntegrateTypeWithPrimitiveWrappersToPrimitives(@NonNull final Class<?> primitive,
                                                            @NotNull final Class<?> primitiveWrapper) {
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.integrateType(primitiveWrapper, primitive));
    }

    @ParameterizedTest
    @MethodSource("providePrimitives")
    void testIntegrateTypeWithPrimitivesToString(@NonNull final Class<?> primitive) {
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.integrateType(primitive, String.class));
    }

    @ParameterizedTest
    @MethodSource("provideDifferentPrimitivePairs")
    void testIntegrateTypeWithDifferentPrimitivePairs(@NonNull final Class<?> first,
                                                      @NotNull final Class<?> second) {
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.integrateType(first, second));
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.integrateType(second, first));
    }

    @ParameterizedTest
    @MethodSource("provideDifferentPrimitiveWrapperPairs")
    void testIntegrateTypeWithDifferentPrimitiveWrapperPairs(@NonNull final Class<?> first,
                                                             @NotNull final Class<?> second) {
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.integrateType(first, second));
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.integrateType(second, first));
    }

    @ParameterizedTest
    @MethodSource("provideOneSideIntegrableNonPrimitivePairs")
    void testIntegrateTypeWithOneSideIntegrableNonPrimitivePairs(@NonNull final Class<?> original,
                                                                 @NotNull final Class<?> target) {
        assertTrue(target.isAssignableFrom(ClassUtil.integrateType(original, target)));
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.integrateType(target, original));
    }

    @ParameterizedTest
    @MethodSource("provideUnassignablePairs")
    void testIntegrateTypeWithUnassignablePairs(@NonNull final Class<?> first, @NotNull final Class<?> second) {
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.integrateType(first, second));
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.integrateType(second, first));
    }
}