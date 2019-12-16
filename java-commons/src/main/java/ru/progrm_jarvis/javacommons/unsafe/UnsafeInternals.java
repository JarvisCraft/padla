package ru.progrm_jarvis.javacommons.unsafe;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.javacommons.classload.ClassUtil;

/**
 * List of commonly used unsafe internals.
 */
@UtilityClass
public class UnsafeInternals {

    /**
     * Name of {@code Unsafe} class
     */
    public static final String UNSAFE_CLASS_NAME = "sun.misc.Unsafe",

    /**
     * Name of {@code MagicAccessorImpl} class
     */
    MAGIC_ACCESSOR_IMPL_CLASS_NAME = "sun.reflect.MagicAccessorImpl";

    /**
     * {@value #UNSAFE_CLASS_NAME} Class object
     */
    @Nullable public Class<?> UNSAFE_CLASS = ClassUtil.getNullableClass(UNSAFE_CLASS_NAME),
    /**
     * {@value #MAGIC_ACCESSOR_IMPL_CLASS_NAME} Class object
     */
    MAGIC_ACCESSOR_IMPL_CLASS = ClassUtil.getNullableClass(MAGIC_ACCESSOR_IMPL_CLASS_NAME);

    /**
     * Flag indicating whether or not {@value #UNSAFE_CLASS_NAME} class is available
     */
    public static final boolean UNSAFE_AVAILABLE = UNSAFE_CLASS != null,
    /**
     * Flag indicating whether or not {@value #MAGIC_ACCESSOR_IMPL_CLASS_NAME} class is available
     */
    MAGIC_ACCESSOR_IMPL_AVAILABLE = MAGIC_ACCESSOR_IMPL_CLASS != null;
}
