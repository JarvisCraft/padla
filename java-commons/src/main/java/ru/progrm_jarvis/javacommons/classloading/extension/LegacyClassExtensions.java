package ru.progrm_jarvis.javacommons.classloading.extension;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;

import static java.lang.invoke.MethodType.methodType;

/**
 * Extensions to provide new {@link Class} methods on older Java versions.
 */
@UtilityClass
public class LegacyClassExtensions {

    /**
     * Method handle referring to {@link Class}{@code .arrayType()} method
     */
    private final @Nullable MethodHandle ARRAY_TYPE__METHOD_HANDLE;

    static {
        val lookup = MethodHandles.publicLookup();
        MethodHandle methodHandle;
        try {
            methodHandle = lookup.findVirtual(Class.class, "arrayType", methodType(Class.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            methodHandle = null;
        }
        ARRAY_TYPE__METHOD_HANDLE = methodHandle;
    }

    /**
     * Gets the class-object corresponding to an array of elements of the given type.
     *
     * @param elementType class-object of array elements
     * @param <T> type of array elements
     * @return array-type of the specified type
     *
     * @apiNote this method is available on {@link Class} itself since Java 12
     */
    @SneakyThrows // call to `MethodHandle#invokeExact(...)`
    public static <T> @NotNull Class<T[]> arrayType(final @NotNull Class<?> elementType) {
        return uncheckedArrayClassCast(ARRAY_TYPE__METHOD_HANDLE == null
                ? Array.newInstance(elementType, 0).getClass() // this is considered the standard implementation
                : (Class<?>) ARRAY_TYPE__METHOD_HANDLE.invokeExact(elementType)
        );
    }

    /**
     * Casts the given array-class object into the specific one.
     *
     * @param type raw-typed array-class object
     * @param <T> exact wanted type of array-class object
     * @return the provided array-class object with its type case to the specific one
     *
     * @apiNote this is effectively no-op
     */
    // note: no nullability annotations are present on parameter and return type as cast of `null` is also safe
    @Contract("_ -> param1")
    @SuppressWarnings("unchecked")
    private <T> Class<T[]> uncheckedArrayClassCast(final Class<?> type) {
        return (Class<T[]>) type;
    }
}
