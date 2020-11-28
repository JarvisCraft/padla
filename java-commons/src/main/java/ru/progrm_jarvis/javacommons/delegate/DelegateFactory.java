package ru.progrm_jarvis.javacommons.delegate;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

/**
 * <p>Factory capable of creating type-safe delegating wrappers.</p>
 * <p>A good use-case is wrapping {@link ru.progrm_jarvis.javacommons.lazy.Lazy} so that the object becomes Lazy
 * yet being of its original type</p>
 */
@FunctionalInterface
public interface DelegateFactory {

    /**
     * Creates a delegating wrapper of the given type using the provided {@link Supplier} for its backend.
     *
     * @param <T> type of implemented wrapper
     * @param targetType type of implemented wrapper
     * @param supplier supplier to handle access to the underlying object
     * @return created delegating wrapper for the given supplier
     *
     * @throws IllegalArgumentException if {@code targetType} cannot be directly implemented or inherited,
     * i.e. if it is {@code private} or it is not an interface
     * and is {@code final} or does not contain an empty non-{@code private} constructor,
     * {@link #verifyTargetType(Class)} should be used for this verification
     *
     * @apiNote if the original type declares fields or its non-{@code public} methods are visible
     * to the owner of this object than access to them will lead to undefined behaviour
     * and most probably will cause logical issues
     */
    <T> @NotNull T createWrapper(@NonNull Class<T> targetType, @NonNull Supplier<T> supplier);

    /**
     * Verifies the target type is valid as a target type for {@link #createWrapper(Class, Supplier) wrapper creation}.
     *
     * @param targetType target type of {@link #createWrapper(Class, Supplier) wrapper creation}
     * which should be verified against method's contract
     *
     * @throws IllegalArgumentException if {@code targetType}
     * is not a valid type for {@link #createWrapper(Class, Supplier) wrapper creation}
     */
    static void verifyTargetType(final @NonNull Class<?> targetType) {
        final int targetTypeModifiers;
        if (Modifier.isPrivate(targetTypeModifiers = targetType.getModifiers())) throw new IllegalArgumentException(
                "target-type cannot be private"
        );

        if (!targetType.isInterface()) {
            if (Modifier.isFinal(targetTypeModifiers)) throw new IllegalArgumentException(
                    "target-type cannot be final"
            );

            {
                final Constructor<?> constructor;
                try {
                    constructor = targetType.getConstructor();
                } catch (final NoSuchMethodException e) {
                    throw new IllegalArgumentException("target-type does not have an empty constructor", e);
                }
                if (Modifier.isPrivate(constructor.getModifiers())) throw new IllegalArgumentException(
                        "target-type's empty constructor cannot be private"
                );
            }
        }
    }
}
