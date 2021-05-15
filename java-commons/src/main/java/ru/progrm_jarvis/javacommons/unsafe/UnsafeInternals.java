package ru.progrm_jarvis.javacommons.unsafe;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.javacommons.classloading.ClassLoadingUtil;
import ru.progrm_jarvis.javacommons.object.Result;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

import static java.lang.invoke.MethodType.methodType;

/**
 * Commonly used unsafe Java internals.
 */
@UtilityClass
public class UnsafeInternals {

    /**
     * {@code Unsafe} class object
     */
    public final @Nullable Class<?> UNSAFE_CLASS;

    /**
     * {@code MagicAccessorImpl} class object
     */
    public final @Nullable Class<?> MAGIC_ACCESSOR_IMPL_CLASS;

    /**
     * Name of {@link #UNSAFE_CLASS} class
     */
    public final @Nullable String UNSAFE_CLASS_NAME;

    /**
     * Name of {@link #MAGIC_ACCESSOR_IMPL_CLASS} class
     */
    public final @Nullable String MAGIC_ACCESSOR_IMPL_CLASS_NAME;

    /**
     * Method handle of {@code Unsafe staticFieldBase(}{@link Field}{@code )} method
     * being {@code null} if this method is unavailable.
     */
    private final @Nullable MethodHandle UNSAFE__STATIC_FIELD_BASE__METHOD_HANDLE;

    /**
     * Method handle of {@code Unsafe staticFieldBase(}{@link Field}{@code )} method
     * being {@code null} if this method is unavailable.
     */
    private final @Nullable MethodHandle UNSAFE__STATIC_FIELD_OFFSET__METHOD_HANDLE;

    /**
     * Method handle of {@code Unsafe staticFieldBase(}{@link Object}{@code , long)} method
     * being {@code null} if this method is unavailable.
     */
    private final @Nullable MethodHandle UNSAFE__GET_OBJECT__METHOD_HANDLE;

    static {
        { // sun.misc.Unsafe or jdk.internal.misc.Unsafe
            // note: sun.misc variant has higher priority as it has the higher chances to be available
            UNSAFE_CLASS = ClassLoadingUtil.getClass("sun.misc.Unsafe")
                    .orElseGet(() -> ClassLoadingUtil.getNullableClass("jdk.internal.misc.Unsafe"));

            if (UNSAFE_CLASS == null) { // no Unsafe is available
                UNSAFE_CLASS_NAME = null;

                UNSAFE__STATIC_FIELD_BASE__METHOD_HANDLE = null;
                UNSAFE__STATIC_FIELD_OFFSET__METHOD_HANDLE = null;
                UNSAFE__GET_OBJECT__METHOD_HANDLE = null;
            } else { // there is a (poss
                UNSAFE_CLASS_NAME = UNSAFE_CLASS.getName();

                Object theUnsafe;
                {
                    final Optional<Field> optionalTheUnsafeField;
                    if ((optionalTheUnsafeField = Arrays.stream(UNSAFE_CLASS.getDeclaredFields())
                            .filter(field -> Modifier.isStatic(field.getModifiers())
                                    && field.getName().equals("theUnsafe")
                                    && UNSAFE_CLASS.isAssignableFrom(field.getType())
                            ).findAny()).isPresent()) {
                        final Field theUnsafeField;
                        val accessible = (theUnsafeField = optionalTheUnsafeField.get()).isAccessible();
                        theUnsafeField.setAccessible(true);
                        try {
                            theUnsafe = theUnsafeField.get(null);
                        } catch (final IllegalAccessException e) {
                            theUnsafe = null;
                        } finally {
                            theUnsafeField.setAccessible(accessible);
                        }
                    } else theUnsafe = null;
                }

                if (theUnsafe == null) {
                    UNSAFE__STATIC_FIELD_BASE__METHOD_HANDLE = null;
                    UNSAFE__STATIC_FIELD_OFFSET__METHOD_HANDLE = null;
                    UNSAFE__GET_OBJECT__METHOD_HANDLE = null;
                } else {
                    val lookup = MethodHandles.lookup();

                    UNSAFE__STATIC_FIELD_BASE__METHOD_HANDLE = tryCreateUnsafeMethodHandle(
                            lookup, "staticFieldBase", methodType(Object.class, Field.class), theUnsafe
                    );
                    UNSAFE__STATIC_FIELD_OFFSET__METHOD_HANDLE = tryCreateUnsafeMethodHandle(
                            lookup, "staticFieldOffset", methodType(long.class, Field.class), theUnsafe
                    );
                    UNSAFE__GET_OBJECT__METHOD_HANDLE = tryCreateUnsafeMethodHandle(
                            lookup, "getObject", methodType(Object.class, Object.class, long.class), theUnsafe
                    );
                }
            }
        }
        { // jdk.internal.reflect.MagicAccessorImpl
            MAGIC_ACCESSOR_IMPL_CLASS = ClassLoadingUtil.getClass("jdk.internal.reflect.MagicAccessorImpl")
                    .orElseGet(() -> ClassLoadingUtil.getNullableClass("sun.reflect.MagicAccessorImpl"));
            MAGIC_ACCESSOR_IMPL_CLASS_NAME = MAGIC_ACCESSOR_IMPL_CLASS == null
                    ? null : MAGIC_ACCESSOR_IMPL_CLASS.getName();
        }
    }

    private static @Nullable MethodHandle tryCreateUnsafeMethodHandle(final @NotNull MethodHandles.Lookup lookup,
                                                                      final @NotNull String name,
                                                                      final @NotNull MethodType type,
                                                                      final @NotNull Object unsafe) {
        final MethodHandle methodHandle;
        try {
            methodHandle = lookup.findVirtual(UNSAFE_CLASS, name, type);
        } catch (final NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
        return methodHandle.bindTo(unsafe);
    }

    /**
     * Gets the value of the static field using {@code Unsafe} if it is possible.
     *
     * @param field field whose value should be read
     * @return result containing the value of the static field
     * or an empty result if this unsafe feature is unavailable
     */
    @SneakyThrows // calls to `MethodHandle#invokeExact(...)`
    public static @NotNull Result<Object, @Nullable Void> staticFieldValue(final @NonNull Field field) {
        if (!Modifier.isStatic(field.getModifiers())) throw new IllegalArgumentException(
                "Field " + field + " is not static"
        );

        return UNSAFE__STATIC_FIELD_BASE__METHOD_HANDLE == null
                || UNSAFE__STATIC_FIELD_OFFSET__METHOD_HANDLE == null
                || UNSAFE__GET_OBJECT__METHOD_HANDLE == null
                ? Result.nullError()
                : Result.success(UNSAFE__GET_OBJECT__METHOD_HANDLE.invokeExact(
                        UNSAFE__STATIC_FIELD_BASE__METHOD_HANDLE.invokeExact(field),
                        (long) UNSAFE__STATIC_FIELD_OFFSET__METHOD_HANDLE.invokeExact(field)
                ));
    }
}
