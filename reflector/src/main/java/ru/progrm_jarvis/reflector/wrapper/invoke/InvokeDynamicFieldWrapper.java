package ru.progrm_jarvis.reflector.wrapper.invoke;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.invoke.InvokeUtil;
import ru.progrm_jarvis.reflector.wrapper.AbstractFieldWrapper;
import ru.progrm_jarvis.reflector.wrapper.DynamicFieldWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;

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
     * Name of the property responsible for concurrency level of {@link #CACHE}
     */
    public static final @NotNull String CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME
            = InvokeDynamicFieldWrapper.class.getCanonicalName() + ".cache-concurrency-level";
    /**
     * Weak cache of allocated instance of this dynamic field wrapper
     */
    protected static final @NotNull Cache<@NotNull Field, @NotNull DynamicFieldWrapper<?, ?>> CACHE
            = CacheBuilder.newBuilder()
            .weakValues()
            .concurrencyLevel(Math.max(1, Integer.getInteger(CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME, 4)))
            .build();

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
    @SneakyThrows(ExecutionException.class)
    public static <@NotNull T, V> @NotNull DynamicFieldWrapper<T, V> from(
            final @NonNull Field field
    ) {
        return (DynamicFieldWrapper<T, V>) CACHE.get(field, () -> {
            checkArgument(!Modifier.isStatic(field.getModifiers()), "field should be non-static");

            return new InvokeDynamicFieldWrapper<>(
                    (Class<? extends T>) field.getDeclaringClass(), field,
                    InvokeUtil.toGetterFunction(field), InvokeUtil.toSetterBiConsumer(field)
            );
        });
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
