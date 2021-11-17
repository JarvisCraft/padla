package ru.progrm_jarvis.javacommons.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.*;

/**
 * Utilities related to special hacks around Java's types, especially generics.
 */
@UtilityClass
public class TypeHints {

    /**
     * Resolves the generic type being the component of the provided var-arg array.
     * This is intended for automatic type-deduction in generic context without explicit class object specification.
     *
     * @param typeHint array used to discover the type, its contents don't matter and it can be empty
     * @param <T> generic type whose class object should be resolved
     * @return class object for the given array
     */
    @SuppressWarnings("unchecked")
    public <T> @NotNull Class<T> resolve(final @Nullable T... typeHint) {
        return UncheckedCasts.uncheckedClassCast(typeHint.getClass().getComponentType());
    }

    /**
     * Marks the specified method argument as a type hint.
     */
    @Inherited
    @Documented
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.SOURCE)
    public @interface TypeHint {}
}
