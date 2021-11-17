package ru.progrm_jarvis.reflector.wrapper.invoke;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.cache.Cache;
import ru.progrm_jarvis.javacommons.cache.Caches;
import ru.progrm_jarvis.javacommons.invoke.InvokeUtil;
import ru.progrm_jarvis.javacommons.object.Pair;
import ru.progrm_jarvis.reflector.wrapper.AbstractFieldWrapper;
import ru.progrm_jarvis.reflector.wrapper.StaticFieldWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * {@link StaticFieldWrapper} based on {@link java.lang.invoke Invoke API}.
 *
 * @param <T> type of the object containing the wrapped method
 * @param <V> type of the field's value
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public final class InvokeStaticFieldWrapper<T, V>
        extends AbstractFieldWrapper<T, V> implements StaticFieldWrapper<T, V> {

    /**
     * Weak cache of allocated instance of this static field wrappers of static fields
     */
    private static final @NotNull Cache<@NotNull Field, @NotNull StaticFieldWrapper<?, ?>> STATIC_WRAPPER_CACHE
            = Caches.weakValuesCache();

    /**
     * Weak cache of allocated instance of this static field wrappers of non-static bound fields
     */
    private static final @NotNull Cache<
            @NotNull Pair<@NotNull Field, @NotNull ?>, @NotNull StaticFieldWrapper<?, ?>
            > BOUND_WRAPPER_CACHE = Caches.weakValuesCache();

    /**
     * Supplier performing the field get operation
     */
    @NonNull Supplier<V> getter;

    /**
     * Consumer performing the field set operation
     */
    @NonNull Consumer<V> setter;

    /**
     * Creates a new static field wrapper.
     *
     * @param containingClass class containing the wrapped object
     * @param wrapped wrapped object
     * @param getter supplier performing the field get operation
     * @param setter consumer performing the field set operation
     */
    private InvokeStaticFieldWrapper(final @NotNull Class<? extends T> containingClass,
                                     final @NotNull Field wrapped,
                                     final @NotNull Supplier<V> getter, final @NotNull Consumer<V> setter) {
        super(containingClass, wrapped);
        this.getter = getter;
        this.setter = setter;
    }

    /**
     * Creates a new cached static field wrapper for the given static field.
     *
     * @param field static field to wrap
     * @param <T> type of the object containing the field
     * @param <V> type of the field's value
     * @return cached field wrapper for the given constructor
     */
    @SuppressWarnings("unchecked")
    public static <@NonNull T, V> @NotNull StaticFieldWrapper<T, V> from(
            final @NonNull Field field
    ) {
        if (!Modifier.isStatic(field.getModifiers())) throw new IllegalArgumentException(
                "Field should be static"
        );

        return (StaticFieldWrapper<T, V>) STATIC_WRAPPER_CACHE.get(field,
                checkedField -> new InvokeStaticFieldWrapper<>(
                        checkedField.getDeclaringClass(), checkedField,
                        InvokeUtil.toStaticGetterSupplier(checkedField), InvokeUtil.toStaticSetterConsumer(checkedField)
                )
        );
    }

    /**
     * Creates a new cached static field wrapper for the given non-static field bound to the object.
     *
     * @param field static field to wrap
     * @param target target object to whom the wrapper should be bound
     * @param <T> type of the object containing the field
     * @param <V> type of the field's value
     * @return cached static field wrapper for the given constructor
     */
    @SuppressWarnings("unchecked")
    public static <@NonNull T, V> @NotNull StaticFieldWrapper<T, V> from(
            final @NonNull Field field, final @NonNull T target
    ) {
        if (Modifier.isStatic(field.getModifiers())) throw new IllegalArgumentException(
                "Field should be non-static"
        );

        return (StaticFieldWrapper<T, V>) BOUND_WRAPPER_CACHE.get(Pair.of(field, target), pair -> {
            val checkedField = pair.getFirst();

            return new InvokeStaticFieldWrapper<>(
                    checkedField.getDeclaringClass(), checkedField,
                    InvokeUtil.toBoundGetterSupplier(checkedField, target),
                    InvokeUtil.toBoundSetterConsumer(checkedField, target)
            );
        });
    }

    @Override
    public V get() {
        return getter.get();
    }

    @Override
    public void set(final V value) {
        setter.accept(value);
    }
}
