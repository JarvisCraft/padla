package ru.progrm_jarvis.reflector.wrapper.invoke;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.reflector.invoke.InvokeUtil;
import ru.progrm_jarvis.reflector.wrapper.AbstractFieldWrapper;
import ru.progrm_jarvis.reflector.wrapper.DynamicFieldWrapper;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Function;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class InvokeDynamicFieldWrapper<T, V> extends AbstractFieldWrapper<T, V> implements DynamicFieldWrapper<T, V> {

    /**
     * Name of the property responsible for concurrency level of {@link #CACHE}
     */
    @NonNull public static final String CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME
            = InvokeDynamicFieldWrapper.class.getCanonicalName() + ".cache-concurrency-level";

    @NonNull Function<T, V> getter;
    @NonNull BiConsumer<T, V> setter;

    /**
     * Weak cache of allocated instance of this static field wrapper
     */
    protected static final Cache<Field, InvokeDynamicFieldWrapper<?, ?>> CACHE
            = CacheBuilder.newBuilder()
            .weakValues()
            .concurrencyLevel(Math.max(1, Integer.getInteger(CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME, 4)))
            .build();

    protected InvokeDynamicFieldWrapper(@NonNull final Class<? extends T> containingClass,
                                        @NonNull final Field wrapped,
                                        @NonNull Function<T, V> getter, @NonNull BiConsumer<T, V> setter) {
        super(containingClass, wrapped);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public V get(@NotNull final T instance) {
        return getter.apply(instance);
    }

    @Override
    public void set(@NotNull final T instance, final V value) {
        setter.accept(instance, value);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows(ExecutionException.class)
    public static <T, V> InvokeDynamicFieldWrapper<T, V> from(@NonNull final Field field) {
        return (InvokeDynamicFieldWrapper<T, V>) CACHE.get(field, () -> new InvokeDynamicFieldWrapper<>(
                (Class<? extends T>) field.getDeclaringClass(), field,
                InvokeUtil.toGetterFunction(field), InvokeUtil.toSetterBiConsumer(field)
        ));
    }
}
