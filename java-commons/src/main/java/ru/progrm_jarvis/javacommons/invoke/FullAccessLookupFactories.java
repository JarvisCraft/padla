package ru.progrm_jarvis.javacommons.invoke;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.javacommons.lazy.Lazy;
import ru.progrm_jarvis.javacommons.object.ObjectUtil;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;

import static java.lang.invoke.MethodType.methodType;

/**
 * Utility for accessing {@link LookupFactory lookup factories} giving practically full access.
 */
@UtilityClass
public class FullAccessLookupFactories {

    /**
     * Lookup factory which returns an internal singleton of lookup
     * <p>
     * <b>Nullable</b> if this JVM's lookup class doesn't have an internal field named {@code "IMPL_LOOKUP"}
     */
    private final Lazy<@Nullable LookupFactory> TRUSTED_LOOKUP_FACTORY = Lazy.createThreadSafe(
            () -> {
                try {
                    return Arrays.stream(MethodHandles.Lookup.class.getDeclaredFields())
                            .filter(field -> field.getName().equals("IMPL_LOOKUP"))
                            .findAny()
                            .map(field -> {
                                @SuppressWarnings("deprecation") val accessible = field.isAccessible();
                                field.setAccessible(true);
                                try {
                                    return (MethodHandles.Lookup) field.get(null);
                                } catch (final IllegalAccessException e) {
                                    throw new IllegalStateException("Unable to create a trusted lookup factory", e);
                                } finally {
                                    field.setAccessible(accessible);
                                }
                            })
                            .map(lookup -> (LookupFactory) lookup::in)
                            .orElse(null);
                } catch (final Throwable x) {
                    return null;
                }
            }
    );

    /**
     * Lookup factory instantiating new lookups for each class
     * <p>
     * <b>Nullable</b> if this JVM's lookup class doesn't have a private constructor of signature {@code Class, int}
     */
    private final Lazy<@Nullable LookupFactory> INSTANTIATING_LOOKUP_FACTORY = Lazy.createThreadSafe(() -> {
        @SuppressWarnings("unchecked") val lookupConstructor = (Constructor<MethodHandles.Lookup>) Arrays
                .stream(MethodHandles.Lookup.class.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterCount() == 2)
                .filter(constructor -> {
                    val parameterTypes = constructor.getParameterTypes();
                    return parameterTypes[0] == Class.class && parameterTypes[1] == int.class;
                })
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Unable to create an instantiating lookup factory"));

        @SuppressWarnings("deprecation") val accessible = lookupConstructor.isAccessible();
        lookupConstructor.setAccessible(true);
        try {
            // allocate new root lookup
            val rootLookup = lookupConstructor.newInstance(MethodHandles.Lookup.class, LookupFactory.ALL_LOOKUP_MODES);
            // implement a functional interface using this lookup
            // use JDK functional interface not to have problems with NoClassDefFoundError
            @SuppressWarnings("unchecked") val biFunction = ((BiFunction<Class<?>, Integer, MethodHandles.Lookup>)
                    LambdaMetafactory
                            .metafactory(rootLookup, "apply", methodType(BiFunction.class),
                                    methodType(Object.class, Object.class, Object.class),
                                    rootLookup.unreflectConstructor(lookupConstructor),
                                    methodType(MethodHandles.Lookup.class, Class.class, int.class)
                            ).getTarget().invokeExact());

            return clazz -> biFunction.apply(clazz, LookupFactory.ALL_LOOKUP_MODES);
        } catch (final Throwable e) {
            return null;
        } finally {
            lookupConstructor.setAccessible(accessible);
        }
    });

    /**
     * Default lookup factory to use
     */
    private final Lazy<Optional<LookupFactory>> DEFAULT_LOOKUP_FACTORY = Lazy.createThreadSafe(() -> Optional.ofNullable(
            ObjectUtil.nonNull(TRUSTED_LOOKUP_FACTORY, INSTANTIATING_LOOKUP_FACTORY)
    ));

    /**
     * Gets the default {@link LookupFactory lookup factory}.
     *
     * @return the default optional {@link LookupFactory lookup factory}.
     */
    public Optional<LookupFactory> getDefault() {
        return DEFAULT_LOOKUP_FACTORY.get();
    }

    // TODO: 06.09.2019 find a way to use it with LambdaMetafactory
    @FunctionalInterface
    interface InstantiatingLookupFactory extends LookupFactory {
        @NotNull MethodHandles.Lookup create(@NotNull Class<?> clazz, int modes);

        @Override
        @NotNull default MethodHandles.Lookup create(@NonNull final Class<?> clazz) {
            return create(clazz, ALL_LOOKUP_MODES);
        }
    }
}
