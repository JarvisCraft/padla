package ru.progrm_jarvis.reflector.wrapper.invoke;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.cache.Cache;
import ru.progrm_jarvis.javacommons.cache.Caches;
import ru.progrm_jarvis.javacommons.invoke.InvokeUtil;
import ru.progrm_jarvis.reflector.wrapper.AbstractFieldWrapper;
import ru.progrm_jarvis.reflector.wrapper.DynamicFieldWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * {@link DynamicFieldWrapper} based on {@link java.lang.invoke Invoke API}.
 *
 * @param <T> type of the object containing the wrapped method
 * @param <V> type of the field's value
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class InvokeDynamicFieldWrapper<@NotNull T, V>
        extends AbstractFieldWrapper<T, V> implements DynamicFieldWrapper<T, V> {

    /**
     * Weak cache of allocated instance of this dynamic field wrapper
     */
    protected static final @NotNull Cache<@NotNull Field, @NotNull DynamicFieldWrapper<?, ?>> CACHE
            = Caches.weakValuesCache();

    /**
     * Function performing the field get operation
     */
    @NotNull Function<@NotNull T, V> getter;
    /**
     * Bi-consumer performing the field set operation
     */
    @NotNull BiConsumer<@NotNull T, V> setter;

    /**
     * Creates a new dynamic field wrapper.
     *
     * @param containingClass class containing the wrapped object
     * @param wrapped wrapped object
     * @param getter function performing the field get operation
     * @param setter bi-consumer performing the field set operation
     */
    protected InvokeDynamicFieldWrapper(final @NotNull Class<? extends T> containingClass,
                                        final @NotNull Field wrapped,
                                        final @NotNull Function<@NotNull T, V> getter,
                                        final @NotNull BiConsumer<@NotNull T, V> setter) {
        super(containingClass, wrapped);
        this.getter = getter;
        this.setter = setter;
    }

    /**
     * Creates a new cached dynamic field wrapper for the given non-static field.
     *
     * @param field field to wrap
     * @param <T> type of the object containing the field
     * @param <V> type of the field's value
     * @return cached dynamic field wrapper for the given constructor
     */
    @SuppressWarnings("unchecked")
    public static <@NotNull T, V> @NotNull DynamicFieldWrapper<T, V> from(
            final @NonNull Field field
    ) {
        if (Modifier.isStatic(field.getModifiers())) throw new IllegalArgumentException("Field should be non-static");

        return (DynamicFieldWrapper<T, V>) CACHE.get(field, checkedField -> new InvokeDynamicFieldWrapper<>(
                (Class<? extends T>) checkedField.getDeclaringClass(), checkedField,
                InvokeUtil.toGetterFunction(checkedField), InvokeUtil.toSetterBiConsumer(checkedField)
        ));
    }

    @Override
    public V get(final @NotNull T instance) {
        return getter.apply(instance);
    }

    @Override
    public void set(final @NotNull T instance, final V value) {
        setter.accept(instance, value);
    }
}
