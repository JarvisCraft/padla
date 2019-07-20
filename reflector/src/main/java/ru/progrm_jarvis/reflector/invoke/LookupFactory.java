package ru.progrm_jarvis.reflector.invoke;

import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.annotation.DontOverrideEqualsAndHashCode;
import ru.progrm_jarvis.javacommons.lazy.Lazy;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.lang.invoke.MethodType.methodType;

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

    @NotNull Lookup create(@NonNull final Class<?> clazz);

    @Override
    default Lookup apply(Class<?> clazz) {
        return create(clazz);
    }

    /**
     * Lookup factory which returns an internal singleton of lookup
     * <p>
     * <b>Nullable</b> if this JVM's lookup class doesn't have an internal field named {@code "IMPL_LOOKUP"}
     */
    Lazy<LookupFactory> TRUSTED_LOOKUP_FACTORY = Lazy.createThreadSafe(
            () -> Arrays.stream(Lookup.class.getDeclaredFields())
                    .filter(field -> field.getName().equals("IMPL_LOOKUP"))
                    .findAny()
                    .map(field -> {
                        val accessible = field.isAccessible();
                        field.setAccessible(true);
                        try {
                            return (Lookup) field.get(null);
                        } catch (final IllegalAccessException e) {
                            throw new IllegalStateException("Unable to create a strusted lookup factory", e);
                        } finally {
                            field.setAccessible(accessible);
                        }
                    })
                    .map(lookup -> (LookupFactory) lookup::in)
                    .orElse(null)
    );

    /**
     * Lookup factory instantiating new lookups for each class
     * <p>
     * <b>Nullable</b> if this JVM's lookup class doesn't have a private constructor of signature {@code Class, int}
     */
    Lazy<LookupFactory> INSTANTIATING_LOOKUP_FACTORY = Lazy.createThreadSafe(() -> {
        @SuppressWarnings("unchecked") val lookupConstructor = (Constructor<Lookup>) Arrays
                .stream(Lookup.class.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterCount() == 2)
                .filter(constructor -> {
                    val parameterTypes = constructor.getParameterTypes();
                    return parameterTypes[0] == Class.class && parameterTypes[1] == int.class;
                })
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Unable to create an instantiating lookup factory"));

        val accessible = lookupConstructor.isAccessible();
        lookupConstructor.setAccessible(true);
        try {
            // allocate new root lookup
            val rootLookup = lookupConstructor.newInstance(Lookup.class, ALL_LOOKUP_MODES);
            // implement a functional interface using this lookup
            // use JDK functional interface not to have problems with NoClassDefFoundError
            @SuppressWarnings("unchecked") val biFunction = ((BiFunction<Class, Integer, Lookup>) LambdaMetafactory
                    .metafactory(
                            rootLookup, "apply", methodType(BiFunction.class),
                            methodType(Object.class, Object.class, Object.class),
                            rootLookup.unreflectConstructor(lookupConstructor),
                            methodType(Lookup.class, Class.class, int.class)
                    ).getTarget().invokeExact());

            return clazz -> biFunction.apply(clazz, ALL_LOOKUP_MODES);
        } catch (final Throwable e) {
            throw new IllegalStateException("Unable to create an instantiating lookup factory", e);
        } finally {
            lookupConstructor.setAccessible(accessible);
        }
    });

    /**
     * Default lookup factory to use
     */
    Lazy<LookupFactory> COMMON_FACTORY = Lazy.createThreadSafe(() -> {
        try {
            return TRUSTED_LOOKUP_FACTORY.get();
        } catch (final Throwable ignored) {}
        try {
            return INSTANTIATING_LOOKUP_FACTORY.get();
        } catch (final Throwable ignored) {}

        throw new IllegalStateException("Unable to create any lookup factory");
    });

    @FunctionalInterface
    interface InstantiatingLookupFactory extends LookupFactory {
        @NotNull Lookup create(@NotNull Class<?> clazz, int modes);

        @Override
        @NotNull default Lookup create(@NonNull final Class<?> clazz) {
            return create(clazz, ALL_LOOKUP_MODES);
        }
    }
}
