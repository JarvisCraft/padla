package ru.progrm_jarvis.javacommons.classloading;

import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.unchecked.UncheckedCasts;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Utility for class-related stuff.
 */
@UtilityClass
public class ClassUtil {

    /**
     * Comparator for sorting {@link Class classes} by their hash-code
     */
    private final Comparator<Class<?>> CLASS_HASH_CODE_COMPARATOR = Comparator.comparing(Class::hashCode);

    /* *************************************** Sorted by PROgrammer_JARvis :) *************************************** */
    /**
     * Array of primitive classes (those whose {@link Class#isPrimitive()} returns {@code true})
     */
    private final Class<?>[] PRIMITIVE_CLASSES = new Class<?>[]{
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

        {
            final int length;
            PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES
                    = new Class<?>[length = SORTED_PRIMITIVE_CLASSES.length];
            for (var i = 0; i < length; i++) {
                final Class<?> type;
                //noinspection ChainOfInstanceofChecks: done ones and is irreplaceable
                if ((type = SORTED_PRIMITIVE_CLASSES[i])
                        == boolean.class) PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES[i] = Boolean.class;
                else if (type == byte.class) PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES[i] = Byte.class;
                else if (type == char.class) PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES[i] = Character.class;
                else if (type == short.class) PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES[i] = Short.class;
                else if (type == int.class) PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES[i] = Integer.class;
                else if (type == long.class) PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES[i] = Long.class;
                else if (type == float.class) PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES[i] = Float.class;
                else if (type == double.class) PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES[i] = Double.class;
                else throw new Error("Unknown primitive class: " + type); // Failsafe, just in case
            }
        }

        SORTED_PRIMITIVE_WRAPPER_CLASSES = PRIMITIVE_WRAPPER_CLASSES.clone();
        Arrays.sort(SORTED_PRIMITIVE_WRAPPER_CLASSES, CLASS_HASH_CODE_COMPARATOR);

        {
            final int length;
            PRIMITIVE_CLASSES_SORTED_BY_PRIMITIVE_WRAPPER_CLASSES
                    = new Class<?>[length = SORTED_PRIMITIVE_WRAPPER_CLASSES.length];

            for (var i = 0; i < length; i++) {
                final Class<?> type;
                //noinspection ChainOfInstanceofChecks: done ones and is irreplaceable
                if ((type = SORTED_PRIMITIVE_WRAPPER_CLASSES[i])
                        == Boolean.class) PRIMITIVE_CLASSES_SORTED_BY_PRIMITIVE_WRAPPER_CLASSES[i] = boolean.class;
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
     * @param <T> specific class type
     * @return primitive-wrapper class for the given primitive class
     *
     * @throws IllegalArgumentException if the given class is not primitive
     */
    public <T> @NotNull Class<? extends T> toPrimitiveWrapper(final @NotNull Class<? super T> primitiveClass) {
        final int primitiveClassIndex;
        if ((primitiveClassIndex = Arrays.binarySearch(
                SORTED_PRIMITIVE_CLASSES, primitiveClass, CLASS_HASH_CODE_COMPARATOR
        )) < 0) throw new IllegalArgumentException("Given class is not primitive");

        return UncheckedCasts.uncheckedClassCast(
                PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES[primitiveClassIndex]
        );
    }

    /**
     * Either returns a primitive-wrapper class for the given one if it is primitive or the provided class otherwise.
     *
     * @param originalClass class whose wrapper should be returned on demand
     * @param <T> specific class type
     * @return primitive-wrapper class for the given class if it is primitive or the provided class otherwise
     */
    public <T> @NotNull Class<? extends T> toNonPrimitive(final @NotNull Class<? super T> originalClass) {
        final int primitiveClassIndex;
        return UncheckedCasts.uncheckedClassCast(
                (primitiveClassIndex = Arrays.binarySearch(
                        SORTED_PRIMITIVE_CLASSES, originalClass, CLASS_HASH_CODE_COMPARATOR
                )) < 0 ? originalClass : PRIMITIVE_WRAPPER_CLASSES_SORTED_BY_PRIMITIVE_CLASSES[primitiveClassIndex]
        );
    }

    /**
     * Gets a primitive class for the given primitive-wrapper class.
     *
     * @param primitiveWrapperClass primitive-wrapper class whose wrapper is needed
     * @param <T> specific class type
     * @return primitive class for the given primitive-wrapper class
     *
     * @throws IllegalArgumentException if the given class is not a primitive-wrapper
     */
    public <T> @NotNull Class<? extends T> toPrimitive(final @NotNull Class<? super T> primitiveWrapperClass) {
        final int primitiveClassIndex;
        if ((primitiveClassIndex = Arrays.binarySearch(
                SORTED_PRIMITIVE_WRAPPER_CLASSES, primitiveWrapperClass, CLASS_HASH_CODE_COMPARATOR
        )) < 0) throw new IllegalArgumentException("Given class is not a primitive-wrapper");

        return UncheckedCasts.uncheckedClassCast(
                PRIMITIVE_CLASSES_SORTED_BY_PRIMITIVE_WRAPPER_CLASSES[primitiveClassIndex]
        );
    }

    /**
     * <p>Integrates the original type with the target
     * making so that target type should be assignable from integrated one
     * (i.e., attempting to make the original type a sub-class of the target type)</p>
     * <p>This method performs primitive-to-wrapper conversion in oder to attempt integration.</p>
     *
     * @param original original type which should be integrated to the target type
     * @param target target type to which the original type should be integrated
     * @param <T> specific class type
     * @return result of type integration, my be the same as original type
     *
     * @throws IllegalArgumentException if original type cannot be integrated to the target type
     */
    public <T> Class<? extends T> integrateType(final @NotNull Class<? super T> original,
                                                final @NotNull Class<? super T> target) {
        // As `Class#isAssignableFrom()` is an intrinsic candidate
        // there is no need for simple `==` check which is probably included in it
        if (target.isAssignableFrom(original)) return UncheckedCasts.uncheckedClassCast(original);

        if(!original.isPrimitive()) throw new IllegalArgumentException(
                "Original type " + original + " cannot be integrated with the target type " + target
                        + " and is not primitive"
        );

        final Class<?> wrapper;
        if (!target.isAssignableFrom(wrapper = toPrimitiveWrapper(original))) throw new IllegalArgumentException(
                "Wrapper " + wrapper + " of the original type " + original
                        + " cannot be integrated with target type " + target
        );

        return UncheckedCasts.uncheckedClassCast(wrapper);
    }

    /**
     * Creates a class-digger for use with {@link ru.progrm_jarvis.javacommons.recursion.Recursions Recursions API}
     * which digs the class hierarchy.
     *
     * @param interfaces whether interfaces should be considered
     * @return digger which returns a {@link Stream stream} of super-classes and implemented interfaces (optionally)
     * of the provided class
     */
    public @NotNull Function<
            ? super @NotNull Class<?>, @NotNull Stream<? extends @NotNull Class<?>>
            > classDigger(final boolean interfaces) {
        return interfaces ? classDiggerWithInterfaces() : classDiggerWithoutInterfaces();
    }

    /**
     * Creates a class-digger for use with {@link ru.progrm_jarvis.javacommons.recursion.Recursions Recursions API}
     * which digs the class hierarchy only including super-classes.
     *
     * @return digger which returns a {@link Stream stream} of super-classes of the provided class
     */
    public @NotNull Function<
            ? super @NotNull Class<?>, @NotNull Stream<? extends @NotNull Class<?>>
            > classDiggerWithoutInterfaces() {
        return clazz -> {
            final Class<?> superClass;
            return (superClass = clazz.getSuperclass()) == null ? Stream.empty() : Stream.of(superClass);
        };
    }

    /**
     * Creates a class-digger for use with {@link ru.progrm_jarvis.javacommons.recursion.Recursions Recursions API}
     * which digs the class hierarchy including super-classes and parent interfaces.
     *
     * @return digger which returns a {@link Stream stream} of super-classes and implemented interfaces
     * of the provided class
     */
    public @NotNull Function<
            ? super @NotNull Class<?>, @NotNull Stream<? extends @NotNull Class<?>>
            > classDiggerWithInterfaces() {
        return clazz -> {
            val interfaces = clazz.getInterfaces();

            final Class<?> superClass;
            if ((superClass = clazz.getSuperclass()) == null) return interfaces.length == 0
                    ? Stream.empty() : Arrays.stream(interfaces);

            val superClassStream = Stream.<Class<?>>of(superClass);
            return interfaces.length == 0
                    ? superClassStream : Stream.concat(superClassStream, Arrays.stream(interfaces));
        };
    }
}
