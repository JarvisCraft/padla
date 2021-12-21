package ru.progrm_jarvis.javacommons.invoke;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.function.Function;

/**
 * Factory responsible for creating full-access {@link Lookup lookups}.
 */
@FunctionalInterface
public interface LookupFactory extends Function<Class<?>, Lookup> {

    /**
     * Creates a lookup for the given class.
     *
     * @param clazz class for which to create a lookup
     * @return lookup for the given class
     */
    @NotNull Lookup create(final @NonNull Class<?> clazz);

    @Override
    default Lookup apply(Class<?> clazz) {
        return create(clazz);
    }
}
