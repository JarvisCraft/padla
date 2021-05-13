package ru.progrm_jarvis.javacommons.invoke;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.javacommons.object.Result;
import ru.progrm_jarvis.javacommons.unsafe.UnsafeInternals;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

/**
 * Utility for accessing {@link LookupFactory lookup factories} giving practically full access.
 */
@UtilityClass
public class FullAccessLookupFactories {

    private final @Nullable LookupFactory LOOKUP_FACTORY;

    static {
        final Lookup rootLookup;
        LOOKUP_FACTORY = (rootLookup = rootFullAccessLookup()) == null ? null : rootLookup::in;
    }

    /**
     * Gets the default {@link LookupFactory lookup factory}.
     *
     * @return the default optional {@link LookupFactory lookup factory}.
     */
    public Optional<LookupFactory> getDefault() {
        return Optional.ofNullable(LOOKUP_FACTORY);
    }

    /**
     * Attempts to create the <i>root</i> lookup with full access.
     *
     * @return optional containing the created full-access lookup
     * or an {@link Optional#empty() empty optional} if it cannot be created
     */
    private @Nullable Lookup rootFullAccessLookup() {
        // Method 1: Find Lookup#IMPL_LOOKUP
        {
            final Field implLookupField = Arrays.stream(Lookup.class.getDeclaredFields())
                    .filter(field -> Modifier.isStatic(field.getModifiers())
                            && field.getName().equals("IMPL_LOOKUP")
                            && Lookup.class.isAssignableFrom(field.getType())
                    )
                    .findAny()
                    .orElse(null);

            if (implLookupField == null) return null;

            // Get the field

            // Attempt 1: legacy variant: via `setAccessible`, start from it as it is does not require Unsafe
            {
                @SuppressWarnings("deprecation") val accessible = implLookupField.isAccessible();
                try {
                    try {
                        implLookupField.setAccessible(true);
                    } catch (final RuntimeException e) {
                        // InaccessibleObjectException reports inaccessibility via reflection, thus go to #2
                        // all other RuntimeExceptions should just be rethrown
                        // the known error class is available only since Java 9
                        if (!e.getClass().getName().equals("java.lang.reflect.InaccessibleObjectException"))
                            return null;
                    }
                    try {
                        return (Lookup) implLookupField.get(null);
                    } catch (final IllegalAccessException ignored) {} // at least we tried
                } finally {
                    implLookupField.setAccessible(accessible);
                }
            }

            // Attempt 2: more up-to-date variant which yet relies on Unsafe
            final Result<Object, Void> implLookup;
            if ((implLookup = UnsafeInternals.staticFieldValue(implLookupField))
                    .isSuccess()) return (Lookup) implLookup.unwrap();
        }

        // Method 2: invoke Lookup's constructor
        {
            // Attempt 1: `Lookup(Class<?>, Class<?>, int)`
            final Constructor<Lookup>[] constructors;
            Optional<Constructor<Lookup>> optionalLookupConstructor;
            if ((optionalLookupConstructor = Arrays
                    .stream(constructors = uncheckedConstructorArrayCast(Lookup.class.getDeclaredConstructors()))
                    .filter(constructor -> {
                        if (constructor.getParameterCount() != 3) return false;
                        final Class<?>[] parameterTypes;
                        return (parameterTypes = constructor.getParameterTypes())[0] == Class.class
                                && parameterTypes[1] == Class.class
                                && parameterTypes[2] == int.class;
                    })
                    .findAny()).isPresent()
            ) return tryInvokeLookupConstructor(optionalLookupConstructor.get(), Object.class, null, -1);

            // Attempt 1: `Lookup(Class<?>, int)`
            if ((optionalLookupConstructor = Arrays
                    .stream(constructors)
                    // Lookup(Class<?>, int)
                    .filter(constructor -> {
                        if (constructor.getParameterCount() != 2) return false;
                        final Class<?>[] parameterTypes;
                        return (parameterTypes = constructor.getParameterTypes())[0] == Class.class
                                && parameterTypes[1] == int.class;
                    })
                    .findAny()).isPresent()
            ) return tryInvokeLookupConstructor(optionalLookupConstructor.get(), Object.class, -1);
        }

        // worst scenario: there is no known way of getting the full-access root lookup
        return null;
    }

    private static @Nullable Lookup tryInvokeLookupConstructor(final @NotNull Constructor<Lookup> constructor,
                                                               final Object @NotNull... parameters) {
        @SuppressWarnings("deprecation") val accessible = constructor.isAccessible();
        try {
            constructor.setAccessible(true);
        } catch (final RuntimeException e) {
            // the known error class is available only since Java 9
            if (!e.getClass().getName().equals("java.lang.reflect.InaccessibleObjectException")) return null;
        }
        try {
            return constructor.newInstance(parameters);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return null;
        } finally {
            constructor.setAccessible(accessible);
        }
    }

    /**
     * Casts the given constructor array into the specific one.
     *
     * @param type raw-typed constructor array
     * @param <T> exact wanted type of constructor array
     * @return the provided constructor array with its type case to the specific one
     *
     * @apiNote this is effectively no-op
     */
    // note: no nullability annotations are present on parameter and return type as cast of `null` is also safe
    @Contract("_ -> param1")
    @SuppressWarnings("unchecked")
    private <T> Constructor<T>[] uncheckedConstructorArrayCast(final Constructor<?>[] type) {
        return (Constructor<T>[]) type;
    }
}
