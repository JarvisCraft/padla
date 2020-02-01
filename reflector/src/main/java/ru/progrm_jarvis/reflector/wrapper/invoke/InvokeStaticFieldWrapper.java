package ru.progrm_jarvis.reflector.wrapper.invoke;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import ru.progrm_jarvis.javacommons.pair.Pair;
import ru.progrm_jarvis.javacommons.pair.SimplePair;
import ru.progrm_jarvis.javacommons.invoke.InvokeUtil;
import ru.progrm_jarvis.reflector.wrapper.AbstractFieldWrapper;
import ru.progrm_jarvis.reflector.wrapper.StaticFieldWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * {@link StaticFieldWrapper} based on {@link java.lang.invoke Invoke API}.
 *
 * @param <T> type of the object containing the wrapped method
 * @param <V> type of the field's value
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class InvokeStaticFieldWrapper<T, V> extends AbstractFieldWrapper<T, V> implements StaticFieldWrapper<T, V> {

    /**
     * Name of the property responsible for concurrency level of {@link #STATIC_WRAPPER_CACHE}
     */
    @NonNull public static final String STATIC_WRAPPER_CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME
            = InvokeStaticFieldWrapper.class.getCanonicalName() + ".static-wrapper-cache-concurrency-level",
    /**
     * Name of the property responsible for concurrency level of {@link #BOUND_WRAPPER_CACHE}
     */
    BOUND_WRAPPER_CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME
            = InvokeStaticFieldWrapper.class.getCanonicalName() + ".bound-wrapper-cache-concurrency-level";
    /**
     * Weak cache of allocated instance of this static field wrappers of static fields
     */
    protected static final Cache<Field, InvokeStaticFieldWrapper<?, ?>> STATIC_WRAPPER_CACHE
            = CacheBuilder.newBuilder()
            .weakValues()
            .concurrencyLevel(
                    Math.max(1, Integer.getInteger(STATIC_WRAPPER_CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME, 4))
            )
            .build();
    /**
     * Weak cache of allocated instance of this static field wrappers of non-static bound fields
     */
    protected static final Cache<Pair<Field, ?>, InvokeStaticFieldWrapper<?, ?>> BOUND_WRAPPER_CACHE
            = CacheBuilder.newBuilder()
            .weakValues()
            .concurrencyLevel(
                    Math.max(1, Integer.getInteger(BOUND_WRAPPER_CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME, 4))
            )
            .build();
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
    protected InvokeStaticFieldWrapper(@NonNull final Class<? extends T> containingClass,
                                       @NonNull final Field wrapped,
                                       @NonNull Supplier<V> getter, @NonNull Consumer<V> setter) {
        super(containingClass, wrapped);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public V get() {
        return getter.get();
    }

    @Override
    public void set(final V value) {
        setter.accept(value);
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
    @SneakyThrows(ExecutionException.class)
    public static <T, V> InvokeStaticFieldWrapper<T, V> from(@NonNull final Field field) {
        return (InvokeStaticFieldWrapper<T, V>) STATIC_WRAPPER_CACHE.get(field, () -> {
            checkArgument(Modifier.isStatic(field.getModifiers()), "field should be static");

            return new InvokeStaticFieldWrapper<>(
                    field.getDeclaringClass(), field,
                    InvokeUtil.toStaticGetterSupplier(field), InvokeUtil.toStaticSetterConsumer(field)
            );
        });
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
    @SneakyThrows(ExecutionException.class)
    public static <T, V> InvokeStaticFieldWrapper<T, V> from(@NonNull final Field field,
                                                             @NonNull final T target) {
        return (InvokeStaticFieldWrapper<T, V>) BOUND_WRAPPER_CACHE.get(SimplePair.of(field, target), () -> {
            checkArgument(!Modifier.isStatic(field.getModifiers()), "field should be non-static");

            return new InvokeStaticFieldWrapper<>(
                    field.getDeclaringClass(), field,
                    InvokeUtil.toBoundGetterSupplier(field, target),
                    InvokeUtil.toBoundSetterConsumer(field, target)
            );
        });
    }
}
