package ru.progrm_jarvis.javacommons.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * Utilities for working with arrays.
 */
@UtilityClass
public class ArrayUtils {

    /**
     * Maps the given array by-element using the provided function.
     *
     * @param source source array
     * @param mappingFunction function used to map elements
     * @param arrayFactory function used to create a new array by its length
     * @return created array
     * @param <T> the type of source elements
     * @param <R> the type of target elements
     */
    public static <T, R> R @NotNull [] map(
            final T @NonNull [] source,
            final @NonNull Function<? super T, ? extends R> mappingFunction,
            final @NonNull IntFunction<? extends R @NotNull []> arrayFactory
    ) {
        final int length;
        val target = arrayFactory.apply(length = source.length);
        for (var i = 0; i < length; i++) target[i] = mappingFunction.apply(source[i]);

        return target;
    }
    /**
     * Maps the given array by-element using the provided function.
     *
     * @param source source array
     * @param mappingFunction function used to map elements
     * @param typeHint pseudo-array of the same type as the target array
     * @return created array
     * @param <T> the type of source elements
     * @param <R> the type of target elements
     */
    public static <T, R> R @NotNull [] map(
            final T @NonNull [] source,
            final @NonNull Function<? super T, ? extends R> mappingFunction,
            @TypeHints.TypeHint
            final @NonNull R... typeHint
    ) {
        final int length;
        @SuppressWarnings("unchecked")
        val target = (R[]) Array.newInstance(TypeHints.resolve(typeHint), length = source.length);
        for (var i = 0; i < length; i++) target[i] = mappingFunction.apply(source[i]);

        return target;
    }
}
