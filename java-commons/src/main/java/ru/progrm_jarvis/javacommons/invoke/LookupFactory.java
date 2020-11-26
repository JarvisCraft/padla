package ru.progrm_jarvis.javacommons.invoke;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.annotation.DontOverrideEqualsAndHashCode;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.function.Function;

/**
 * Factory responsible for creating full-access {@link Lookup lookups}.
 */
@FunctionalInterface
@DontOverrideEqualsAndHashCode
public interface LookupFactory extends Function<Class<?>, Lookup> {

    /**
     * A multi-bit mask representing all available accesses,
     *  which may contribute to the result of {@link Lookup#lookupModes}.
     */
    // Javadoc is based in the same ones of Lookup class
    int ALL_LOOKUP_MODES = Lookup.PUBLIC | Lookup.PRIVATE | Lookup.PROTECTED | Lookup.PACKAGE;

    @NotNull Lookup create(final @NonNull Class<?> clazz);

    @Override
    default Lookup apply(Class<?> clazz) {
        return create(clazz);
    }
}
