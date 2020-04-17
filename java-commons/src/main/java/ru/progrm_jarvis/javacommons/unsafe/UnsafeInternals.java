package ru.progrm_jarvis.javacommons.unsafe;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.javacommons.classloading.ClassLoadingUtil;

/**
 * List of commonly used unsafe internals.
 */
@UtilityClass
public class UnsafeInternals {

    /**
     * {@code Unsafe} Class object
     */
    @Nullable public final Class<?> UNSAFE_CLASS,
    /**
     * {@code MagicAccessorImpl} Class object
     */
    MAGIC_ACCESSOR_IMPL_CLASS;

    /**
     * Name of {@link #UNSAFE_CLASS} class
     */
    public final String UNSAFE_CLASS_NAME,
    /**
     * Name of {@link #MAGIC_ACCESSOR_IMPL_CLASS} class
     */
    MAGIC_ACCESSOR_IMPL_CLASS_NAME;

    /**
     * Flag indicating whether or not {@link #UNSAFE_CLASS} class is available
     */
    public final boolean UNSAFE_AVAILABLE,
    /**
     * Flag indicating whether or not {@link #MAGIC_ACCESSOR_IMPL_CLASS} class is available
     */
    MAGIC_ACCESSOR_IMPL_AVAILABLE;

    static {
        {
            UNSAFE_CLASS = ClassLoadingUtil.getClass("jdk.internal.misc.Unsafe")
                    .orElseGet(() -> ClassLoadingUtil.getNullableClass("sun.misc.Unsafe"));
            if (UNSAFE_CLASS == null) {
                UNSAFE_CLASS_NAME = null;
                UNSAFE_AVAILABLE = false;
            } else  {
                UNSAFE_CLASS_NAME = UNSAFE_CLASS.getName();
                UNSAFE_AVAILABLE = true;
            }
        }
        {

            MAGIC_ACCESSOR_IMPL_CLASS = ClassLoadingUtil.getClass("jdk.internal.reflect.MagicAccessorImpl")
                    .orElseGet(() -> ClassLoadingUtil.getNullableClass("sun.reflect.MagicAccessorImpl"));
            if (MAGIC_ACCESSOR_IMPL_CLASS == null) {
                MAGIC_ACCESSOR_IMPL_CLASS_NAME = null;
                MAGIC_ACCESSOR_IMPL_AVAILABLE = false;
            } else  {
                MAGIC_ACCESSOR_IMPL_CLASS_NAME = MAGIC_ACCESSOR_IMPL_CLASS.getName();
                MAGIC_ACCESSOR_IMPL_AVAILABLE = true;
            }
        }
    }
}
