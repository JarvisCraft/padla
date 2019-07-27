package ru.progrm_jarvis.reflector.wrapper.invoke;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import ru.progrm_jarvis.reflector.invoke.InvokeUtil;
import ru.progrm_jarvis.reflector.wrapper.AbstractFieldWrapper;
import ru.progrm_jarvis.reflector.wrapper.StaticFieldWrapper;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class InvokeStaticFieldWrapper<T, V> extends AbstractFieldWrapper<T, V> implements StaticFieldWrapper<T, V> {

    /**
     * Name of the property responsible for concurrency level of {@link #CACHE}
     */
    @NonNull public static final String CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME
            = InvokeStaticFieldWrapper.class.getCanonicalName() + ".cache-concurrency-level";

    @NonNull Supplier<V> getter;
    @NonNull Consumer<V> setter;

    /**
     * Weak cache of allocated instance of this static field wrapper
     */
    protected static final Cache<Field, InvokeStaticFieldWrapper<?, ?>> CACHE
            = CacheBuilder.newBuilder()
            .weakValues()
            .concurrencyLevel(Math.max(1, Integer.getInteger(CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME, 4)))
            .build();

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

    @SuppressWarnings("unchecked")
    @SneakyThrows(ExecutionException.class)
    public static <T, R> InvokeStaticFieldWrapper<T, R> from(@NonNull final Field field) {
        return (InvokeStaticFieldWrapper<T, R>) CACHE.get(field, () -> new InvokeStaticFieldWrapper<>(
                field.getDeclaringClass(), field,
                InvokeUtil.toStaticGetterSupplier(field), InvokeUtil.toStaticSetterConsumer(field)
        ));
    }

    @SuppressWarnings("unchecked")
    public static <T, V> InvokeStaticFieldWrapper<T, V> from(@NonNull final Field field,
                                                             @NonNull final T target) {
        return new InvokeStaticFieldWrapper<>(
                (Class<? extends T>) field.getDeclaringClass(), field,
                InvokeUtil.toBoundGetterSupplier(field, target), InvokeUtil.toBoundSetterConsumer(field, target)
        );
    }
}
