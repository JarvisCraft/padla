package ru.progrm_jarvis.javacommons.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Utility for class-related stuff.
 */
@UtilityClass
public class ClassUtil {

    /**
     * Comparator for sorting {@link Class classes} by their hash-code
     */
    private static final Comparator<Class<?>> CLASS_HASH_CODE_COMPARATOR = Comparator.comparing(Class::hashCode);

    /* *************************************** Sorted by PROgrammer_JARvis :) *************************************** */
    /**
     * Array of primitive classes (those whose {@link Class#isPrimitive()} returns {@code true})
     */
    private static final Class<?>[] PRIMITIVE_CLASSES = new Class<?>[]{
            boolean.class, byte.class, char.class, short.class,
            int.class, long.class, float.class, double.class
    },
    /**
     * Array of primitive-wrapper classes in the same order
     * as the corresponding primitive classes in {@link #PRIMITIVE_CLASSES}
     */
    PRIMITIVE_WRAPPER_CLASSES = new Class<?>[]{
            Boolean.class, Byte.class, Character.class, Short.class,
            Integer.class, Long.class, Float.class, Double.class
    },
    /* ******************************************** Sorted by primitives ******************************************** */
    /**
     * Sorted variant of {@link #PRIMITIVE_CLASSES}
     */
    SORTED_PRIMITIVE_CLASSES,
    /**
     * Array of primitive-wrapper classes in the same order
     * as the corresponding primitive classes in {@link #SORTED_PRIMITIVE_CLASSES}
     */
    PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES,
    /* ********************************************* Sorted by wrappers ********************************************* */
    /**
     * Sorted variant of {@link #PRIMITIVE_WRAPPER_CLASSES}
     */
    SORTED_PRIMITIVE_WRAPPER_CLASSES,
    /**
     * Array of primitive classes in the same order
     * as the corresponding primitive-wrapper classes in {@link #SORTED_PRIMITIVE_WRAPPER_CLASSES}
     */
    PRIMITIVE_CLASSES_SORTED_BY_PRIMITIVE_WRAPPER_CLASSES;

    static {
        SORTED_PRIMITIVE_CLASSES = PRIMITIVE_CLASSES.clone();
        Arrays.sort(SORTED_PRIMITIVE_CLASSES, CLASS_HASH_CODE_COMPARATOR);

        PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES = new Class<?>[SORTED_PRIMITIVE_CLASSES.length];
        for (var i = 0; i < SORTED_PRIMITIVE_CLASSES.length; i++) {
            val type = SORTED_PRIMITIVE_CLASSES[i];

            if (type == boolean.class) PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES[i] = Boolean.class;
            else if (type == byte.class) PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES[i] = Byte.class;
            else if (type == char.class) PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES[i] = Character.class;
            else if (type == short.class) PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES[i] = Short.class;
            else if (type == int.class) PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES[i] = Integer.class;
            else if (type == long.class) PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES[i] = Long.class;
            else if (type == float.class) PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES[i] = Float.class;
            else if (type == double.class) PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES[i] = Double.class;
            else throw new Error("Unknown primitive class: " + type); // Failsafe, just in case
        }

        SORTED_PRIMITIVE_WRAPPER_CLASSES = PRIMITIVE_WRAPPER_CLASSES.clone();
        Arrays.sort(SORTED_PRIMITIVE_WRAPPER_CLASSES, CLASS_HASH_CODE_COMPARATOR);

        PRIMITIVE_CLASSES_SORTED_BY_PRIMITIVE_WRAPPER_CLASSES = new Class<?>[SORTED_PRIMITIVE_WRAPPER_CLASSES.length];
        for (var i = 0; i < SORTED_PRIMITIVE_WRAPPER_CLASSES.length; i++) {
            val type = SORTED_PRIMITIVE_WRAPPER_CLASSES[i];

            if (type == Boolean.class) PRIMITIVE_CLASSES_SORTED_BY_PRIMITIVE_WRAPPER_CLASSES[i] = boolean.class;
            else if (type == Byte.class) PRIMITIVE_CLASSES_SORTED_BY_PRIMITIVE_WRAPPER_CLASSES[i] = byte.class;
            else if (type == Character.class) PRIMITIVE_CLASSES_SORTED_BY_PRIMITIVE_WRAPPER_CLASSES[i] = char.class;
            else if (type == Short.class) PRIMITIVE_CLASSES_SORTED_BY_PRIMITIVE_WRAPPER_CLASSES[i] = short.class;
            else if (type == Integer.class) PRIMITIVE_CLASSES_SORTED_BY_PRIMITIVE_WRAPPER_CLASSES[i] = int.class;
            else if (type == Long.class) PRIMITIVE_CLASSES_SORTED_BY_PRIMITIVE_WRAPPER_CLASSES[i] = long.class;
            else if (type == Float.class) PRIMITIVE_CLASSES_SORTED_BY_PRIMITIVE_WRAPPER_CLASSES[i] = float.class;
            else if (type == Double.class) PRIMITIVE_CLASSES_SORTED_BY_PRIMITIVE_WRAPPER_CLASSES[i] = double.class;
            else throw new Error("Unknown primitive-wrapper class: " + type); // Failsafe, just in case
        }
    }

    /**
     * Checks if the given class is a primitive wrapper.
     *
     * @param clazz class to check
     * @return {@code true} if the class is a primitive wrapper and {@code false} otherwise
     */
    public boolean isPrimitiveWrapper(final Class<?> clazz) {
        return Arrays.binarySearch(SORTED_PRIMITIVE_WRAPPER_CLASSES, clazz, CLASS_HASH_CODE_COMPARATOR) >= 0;
    }

    /**
     * Gets a primitive-wrapper class for the given primitive class.
     *
     * @param primitiveClass primitive class whose wrapper is needed
     * @return primitive-wrapper class for the given primitive class
     *
     * @throws IllegalArgumentException if the given class is not primitive
     */
    public Class<?> toPrimitiveWrapper(@NotNull /* hot spot */ final Class<?> primitiveClass) {
        val primitiveClassIndex = Arrays.binarySearch(
                SORTED_PRIMITIVE_CLASSES, primitiveClass, CLASS_HASH_CODE_COMPARATOR
        );
        checkArgument(primitiveClassIndex >= 0, "Given class is not primitive");

        return PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES[primitiveClassIndex];
    }

    /**
     * Gets a primitive class for the given primitive-wrapper class.
     *
     * @param primitiveWrapperClass primitive-wrapper class whose wrapper is needed
     * @return primitive class for the given primitive-wrapper class
     *
     * @throws IllegalArgumentException if the given class is not a primitive-wrapper
     */
    public Class<?> toPrimitive(@NotNull /* hot spot */ final Class<?> primitiveWrapperClass) {
        val primitiveClassIndex = Arrays.binarySearch(
                SORTED_PRIMITIVE_WRAPPER_CLASSES, primitiveWrapperClass, CLASS_HASH_CODE_COMPARATOR
        );
        checkArgument(primitiveClassIndex >= 0, "Given class is not a primitive-wrapper");

        return PRIMITIVE_CLASSES_SORTED_BY_PRIMITIVE_WRAPPER_CLASSES[primitiveClassIndex];
    }

    /**
     * <p>Integrates the original type with the target
     * making so that target type should be assignable from integrated one
     * (i.e., attempting to make the original type a sub-class of the target type)</p>
     * <p>This method performs primitive-to-wrapper conversion in oder to attempt integration.</p>
     *
     * @param original original type which should be integrated to the target type
     * @param target target type to which the original type should be integrated
     * @return result of type integration, my be the same as original type
     *
     * @throws IllegalArgumentException if original type cannot be integrated to the target type
     */
    public Class<?> integrateType(@NotNull /* hot spot */ final Class<?> original,
                                  @NotNull /* hot spot */ final Class<?> target) {
        // As `Class#isAssignableFrom()` is an intrinsic candidate
        // there is no need for simple `==` check which is probably included in it
        if (target.isAssignableFrom(original)) return original;

        checkArgument(
                original.isPrimitive(),
                "Original type %s cannot be integrated with the target type %s and is not primitive", original, target
        );

        val wrapper = toPrimitiveWrapper(original);

        checkArgument(
                target.isAssignableFrom(wrapper),
                "Wrapper %s of the original type %s cannot be integrated with target type %s", wrapper, original, target
        );

        return wrapper;
    }
}
