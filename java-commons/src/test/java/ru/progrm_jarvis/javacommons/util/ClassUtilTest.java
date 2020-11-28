package ru.progrm_jarvis.javacommons.util;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.progrm_jarvis.javacommons.classloading.ClassUtil;

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
    void testIsPrimitiveWrapperWithPrimitiveWrappers(final @NotNull Class<?> primitiveWrapper) {
        assertTrue(ClassUtil.isPrimitiveWrapper(primitiveWrapper));
    }

    @ParameterizedTest
    @MethodSource("providePrimitives")
    void testIsPrimitiveWrapperWithPrimitives(final @NotNull Class<?> primitive) {
        assertFalse(ClassUtil.isPrimitiveWrapper(primitive));
    }

    @ParameterizedTest
    @MethodSource("provideNonPrimitiveOrWrappers")
    void testIsPrimitiveWrapperWithNonPrimitiveOrWrapperClasses(final @NotNull Class<?> nonPrimitiveOrWrapper) {
        assertFalse(ClassUtil.isPrimitiveWrapper(nonPrimitiveOrWrapper));
    }

    @ParameterizedTest
    @MethodSource("providePrimitivesToPrimitiveWrappers")
    void testToPrimitiveWrapperWithValid(final @NotNull Class<?> primitive, final @NotNull Class<?> wrapper) {
        assertSame(wrapper, ClassUtil.toPrimitiveWrapper(primitive));
    }

    @ParameterizedTest
    @MethodSource("providePrimitiveWrappers")
    void testToPrimitiveWrapperWithPrimitiveWrappers(final @NotNull Class<?> wrapper) {
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.toPrimitiveWrapper(wrapper));
    }

    @ParameterizedTest
    @MethodSource("provideNonPrimitiveOrWrappers")
    void testToPrimitiveWrapperWithNonPrimitiveOrWrapperClasses(final @NotNull Class<?> nonPrimitiveOrWrapper) {
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.toPrimitiveWrapper(nonPrimitiveOrWrapper));
    }

    @ParameterizedTest
    @MethodSource("providePrimitivesToPrimitiveWrappers")
    void testToPrimitiveWithValid(final @NotNull Class<?> primitive, final @NotNull Class<?> wrapper) {
        assertSame(primitive, ClassUtil.toPrimitive(wrapper));
    }

    @ParameterizedTest
    @MethodSource("providePrimitives")
    void testToPrimitiveWithPrimitives(final @NotNull Class<?> primitive) {
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.toPrimitive(primitive));
    }

    @ParameterizedTest
    @MethodSource("provideNonPrimitiveOrWrappers")
    void testToPrimitiveWithNonPrimitiveOrWrapperClasses(final @NotNull Class<?> nonPrimitiveOrWrapper) {
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.toPrimitive(nonPrimitiveOrWrapper));
    }

    @ParameterizedTest
    @MethodSource("providePrimitives")
    void testIntegrateTypeWithPrimitiveTypes(final @NonNull Class<?> primitive) {
        assertTrue(primitive.isAssignableFrom(
                ClassUtil.integrateType(primitive, primitive))
        );
        assertTrue(Object.class.isAssignableFrom(
                ClassUtil.integrateType(primitive, Object.class))
        );
    }

    @ParameterizedTest
    @MethodSource("providePrimitiveWrappers")
    void testIntegrateTypeWithPrimitiveWrapperTypes(final @NonNull Class<?> primitiveWrapper) {
        assertTrue(primitiveWrapper.isAssignableFrom(
                ClassUtil.integrateType(primitiveWrapper, primitiveWrapper)
        ));
        assertTrue(Object.class.isAssignableFrom(
                ClassUtil.integrateType(primitiveWrapper, Object.class)
        ));
    }

    @ParameterizedTest
    @MethodSource("provideNonPrimitiveOrWrappers")
    void testIntegrateTypeWithNonPrimitiveOrWrapperClasses(final @NonNull Class<?> nonPrimitiveOrWrappers) {
        assertTrue(nonPrimitiveOrWrappers.isAssignableFrom(
                ClassUtil.integrateType(nonPrimitiveOrWrappers, nonPrimitiveOrWrappers)
        ));
        assertTrue(Object.class.isAssignableFrom(
                ClassUtil.integrateType(nonPrimitiveOrWrappers, Object.class)
        ));
    }

    @ParameterizedTest
    @MethodSource("providePrimitivesToPrimitiveWrappers")
    void testIntegrateTypeWithPrimitivesToPrimitiveWrappers(final @NonNull Class<?> primitive,
                                                            final @NotNull Class<?> primitiveWrapper) {
        assertTrue(primitiveWrapper.isAssignableFrom(ClassUtil.integrateType(primitive, primitiveWrapper)));
    }

    @ParameterizedTest
    @MethodSource("providePrimitivesToPrimitiveWrappers")
    void testIntegrateTypeWithPrimitiveWrappersToPrimitives(final @NonNull Class<?> primitive,
                                                            final @NotNull Class<?> primitiveWrapper) {
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.integrateType(primitiveWrapper, primitive));
    }

    @ParameterizedTest
    @MethodSource("providePrimitives")
    void testIntegrateTypeWithPrimitivesToString(final @NonNull Class<?> primitive) {
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.integrateType(primitive, String.class));
    }

    @ParameterizedTest
    @MethodSource("provideDifferentPrimitivePairs")
    void testIntegrateTypeWithDifferentPrimitivePairs(final @NonNull Class<?> first,
                                                      final @NotNull Class<?> second) {
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.integrateType(first, second));
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.integrateType(second, first));
    }

    @ParameterizedTest
    @MethodSource("provideDifferentPrimitiveWrapperPairs")
    void testIntegrateTypeWithDifferentPrimitiveWrapperPairs(final @NonNull Class<?> first,
                                                             final @NotNull Class<?> second) {
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.integrateType(first, second));
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.integrateType(second, first));
    }

    @ParameterizedTest
    @MethodSource("provideOneSideIntegrableNonPrimitivePairs")
    void testIntegrateTypeWithOneSideIntegrableNonPrimitivePairs(final @NonNull Class<?> original,
                                                                 final @NotNull Class<?> target) {
        assertTrue(target.isAssignableFrom(ClassUtil.integrateType(original, target)));
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.integrateType(target, original));
    }

    @ParameterizedTest
    @MethodSource("provideUnassignablePairs")
    void testIntegrateTypeWithUnassignablePairs(final @NonNull Class<?> first, final @NotNull Class<?> second) {
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.integrateType(first, second));
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.integrateType(second, first));
    }
}