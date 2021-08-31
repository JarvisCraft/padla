package ru.progrm_jarvis.javacommons.invoke;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.cache.Cache;
import ru.progrm_jarvis.javacommons.cache.Caches;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.invoke.LambdaMetafactory.metafactory;
import static java.lang.invoke.MethodType.methodType;

/**
 * Utility for common scenarios related to Java Invoke API.
 * <p>
 * Note that OpenJDK's default {@link java.lang.invoke.LambdaMetafactory} is not capable of implementing
 * functional interfaces for fields' {@link MethodHandle method-handle getters / setters}
 */
@UtilityClass
public class InvokeUtil {

    /**
     * Lookup factory used by this utility
     */
    private final @NotNull LookupFactory LOOKUP_FACTORY = FullAccessLookupFactories.getDefault()
            .orElseThrow(() -> new IllegalStateException("LookupFactory is unavailable"));

    /**
     * Method of {@link Runnable} functional method
     */
    private final @NotNull String RUNNABLE_FUNCTIONAL_METHOD_NAME = "run";
    /**
     * Method of {@link Supplier} functional method
     */
    private final @NotNull String SUPPLIER_FUNCTIONAL_METHOD_NAME = "get";

    /**
     * Method type of signature: <code>{@code void}()</code>
     */
    public final @NotNull MethodType VOID__METHOD_TYPE = methodType(void.class);
    /**
     * Method type of signature: <code>{@link Object}()</code>
     */
    public final @NotNull MethodType OBJECT__METHOD_TYPE = methodType(Object.class);
    /**
     * Method type of signature: <code>{@link Runnable}()</code>
     */
    public final @NotNull MethodType RUNNABLE__METHOD_TYPE = methodType(Runnable.class);
    /**
     * Method type of signature: <code>{@link Supplier}()</code>
     */
    public final @NotNull MethodType SUPPLIER__METHOD_TYPE = methodType(Supplier.class);
    /**
     * Method type of signature: <code>{@link Runnable}({@link Object})</code>
     */
    public final @NotNull MethodType RUNNABLE_OBJECT__METHOD_TYPE = methodType(Runnable.class, Object.class);
    /**
     * Method type of signature: <code>{@link Supplier}({@link Object})</code>
     */
    public final @NotNull MethodType SUPPLIER_OBJECT__METHOD_TYPE = methodType(Supplier.class, Object.class);

    // because there is no need to GC lookups which may be expansive to create
    private final @NonNull Cache<@NotNull Class<?>, @NotNull Lookup> LOOKUPS = Caches.softValuesCache();

    /**
     * Lookup factory which delegated its calls to {@link InvokeUtil#lookup(Class)}
     */
    private final @NotNull LookupFactory DELEGATING_LOOKUP_FACTORY = InvokeUtil::lookup;

    /**
     * Gets the proxy lookup factory which delegated its calls to {@link InvokeUtil#lookup(Class)}
     *
     * @return proxy lookup factory
     */
    public @NotNull LookupFactory getDelegatingLookupFactory() {
        return DELEGATING_LOOKUP_FACTORY;
    }

    /**
     * Creates a cache {@link Lookup} for the given class.
     *
     * @param clazz class for which to create a lookup
     * @return created cached lookup for the given class
     */
    public @NotNull Lookup lookup(final @NonNull Class<?> clazz) {
        //noinspection ConstantConditions: the result cannot be null as `create(Class<?>)` is non-null
        return LOOKUPS.get(clazz, LOOKUP_FACTORY::create);
    }

    /**
     * Creates new {@link InvokeFactory invoke factory} using {@link #DELEGATING_LOOKUP_FACTORY}.
     *
     * @param <F> type of functional interface implemented
     * @param <T> type of target value
     * @return created invoke factory
     */
    public <F, T> @NotNull InvokeFactory<F, T> invokeFactory() {
        return SimpleInvokeFactory
                .<F, T>newInstance()
                .using(DELEGATING_LOOKUP_FACTORY);
    }

    /**
     * Converts the given method to a {@link MethodHandle}.
     *
     * @param method method to convert to {@link MethodHandle}
     * @return {@link MethodHandle} created from the given method
     */
    @SneakyThrows(IllegalAccessException.class)
    public @NotNull MethodHandle toMethodHandle(final @NonNull Method method) {
        return lookup(method.getDeclaringClass()).unreflect(method);
    }

    /**
     * Converts the given constructor to a {@link MethodHandle}.
     *
     * @param constructor constructor to convert to {@link MethodHandle}
     * @return {@link MethodHandle} created from the given constructor
     */
    @SneakyThrows(IllegalAccessException.class)
    public @NotNull MethodHandle toMethodHandle(final @NonNull Constructor<?> constructor) {
        return lookup(constructor.getDeclaringClass()).unreflectConstructor(constructor);
    }

    /**
     * Converts the given field to a getter-{@link MethodHandle}.
     *
     * @param field field to convert to getter-{@link MethodHandle}
     * @return getter-{@link MethodHandle} created from the given field
     */
    @SneakyThrows(IllegalAccessException.class)
    public @NotNull MethodHandle toGetterMethodHandle(final @NonNull Field field) {
        return lookup(field.getDeclaringClass()).unreflectGetter(field);
    }

    /**
     * Converts the given field to a setter-{@link MethodHandle}.
     *
     * @param field field to convert to setter-{@link MethodHandle}
     * @return setter-{@link MethodHandle} created from the given field
     */
    @SneakyThrows(IllegalAccessException.class)
    public @NotNull MethodHandle toSetterMethodHandle(final @NonNull Field field) {
        return lookup(field.getDeclaringClass()).unreflectSetter(field);
    }

    /**
     * Creates a {@link Runnable} to invoke the given static method.
     *
     * @param method static method from which to create a {@link Runnable}
     * @return runnable invoking the given method
     * @throws IllegalArgumentException if the given method requires parameters
     * @throws IllegalArgumentException if the given method is not static
     */
    public @NotNull Runnable toStaticRunnable(final @NonNull Method method) {
        Check.hasNoParameters(method);
        Check.isStatic(method);

        val lookup = lookup(method.getDeclaringClass());
        try {
            val methodHandle = lookup.unreflect(method);
            return (Runnable) metafactory(
                    lookup, RUNNABLE_FUNCTIONAL_METHOD_NAME, RUNNABLE__METHOD_TYPE,
                    VOID__METHOD_TYPE, methodHandle, VOID__METHOD_TYPE
            ).getTarget().invokeExact();
        } catch (final Throwable x) {
            throw new RuntimeException(
                    "An exception occurred while trying to convert method " + method + " to Runnable", x
            );
        }
    }

    /**
     * Creates a {@link Runnable} to invoke the given non-static method on the given target.
     *
     * @param method non-static method from which to create a {@link Runnable}
     * @param target object on which the method should be invoked
     * @return runnable invoking the given method
     * @throws IllegalArgumentException if the given method requires parameters
     * @throws IllegalArgumentException if the given method is static
     */
    public @NotNull Runnable toBoundRunnable(final @NonNull Method method, final @NonNull Object target) {
        Check.hasNoParameters(method);
        Check.isNotStatic(method);

        val lookup = lookup(method.getDeclaringClass());
        try {
            val methodHandle = lookup.unreflect(method);
            return (Runnable) metafactory(
                    lookup, RUNNABLE_FUNCTIONAL_METHOD_NAME,
                    RUNNABLE_OBJECT__METHOD_TYPE.changeParameterType(0, target.getClass()),
                    VOID__METHOD_TYPE, methodHandle, VOID__METHOD_TYPE
            ).getTarget().invoke(target);
        } catch (final Throwable x) {
            throw new RuntimeException(
                    "An exception occurred while trying to convert method " + method + " to Runnable", x
            );
        }
    }

    /**
     * Creates a {@link Supplier} to invoke the given static method getting its returned value.
     *
     * @param method static method from which to create a {@link Supplier}
     * @param <R> return type of the method
     * @return runnable invoking the given method
     * @throws IllegalArgumentException if the given method requires parameters
     * @throws IllegalArgumentException if the given method is not static
     */
    public <R> @NotNull Supplier<R> toStaticSupplier(final @NonNull Method method) {
        Check.hasNoParameters(method);
        Check.isStatic(method);

        val lookup = lookup(method.getDeclaringClass());
        try {
            val methodHandle = lookup.unreflect(method);
            //noinspection unchecked: generic type of returned object
            return (Supplier<R>) metafactory(
                    lookup, SUPPLIER_FUNCTIONAL_METHOD_NAME, SUPPLIER__METHOD_TYPE,
                    OBJECT__METHOD_TYPE, methodHandle, methodHandle.type()
            ).getTarget().invokeExact();
        } catch (final Throwable x) {
            throw new RuntimeException(
                    "An exception occurred while trying to convert method " + method + " to Supplier", x
            );
        }
    }

    /**
     * Creates a {@link Supplier} to invoke the given non-static method on the given target getting its returned value.
     *
     * @param method non-static method from which to create a {@link Supplier}
     * @param target object on which the method should be invoked
     * @param <R> return type of the method
     * @return supplier invoking the given method
     * @throws IllegalArgumentException if the given method requires parameters
     * @throws IllegalArgumentException if the given method is static
     */
    public <R> @NotNull Supplier<R> toBoundSupplier(final @NonNull Method method, final @NonNull Object target) {
        Check.hasNoParameters(method);
        Check.isNotStatic(method);

        val lookup = lookup(method.getDeclaringClass());
        try {
            val methodHandle = lookup.unreflect(method);
            //noinspection unchecked: generic type of returned object
            return (Supplier<R>) metafactory(
                    lookup, SUPPLIER_FUNCTIONAL_METHOD_NAME,
                    SUPPLIER_OBJECT__METHOD_TYPE.changeParameterType(0, target.getClass()),
                    OBJECT__METHOD_TYPE, methodHandle, OBJECT__METHOD_TYPE.changeReturnType(method.getReturnType())
            ).getTarget().invoke(target);
        } catch (final Throwable x) {
            throw new RuntimeException(
                    "An exception occurred while trying to convert method " + method + " to Supplier", x
            );
        }
    }

    /**
     * Creates a {@link Supplier} to invoke the given constructor.
     *
     * @param constructor constructor from which to create a {@link Supplier}
     * @param <T> type of object instantiated by the constructor
     * @return supplier invoking the given constructor
     * @throws IllegalArgumentException if the given constructor requires parameters
     */
    public <T> @NotNull Supplier<T> toSupplier(final @NonNull Constructor<T> constructor) {
        Check.hasNoParameters(constructor);

        val lookup = lookup(constructor.getDeclaringClass());
        try {
            val methodHandle = lookup.unreflectConstructor(constructor);
            //noinspection unchecked: generic type of returned object
            return (Supplier<T>) metafactory(
                    lookup, SUPPLIER_FUNCTIONAL_METHOD_NAME, SUPPLIER__METHOD_TYPE,
                    OBJECT__METHOD_TYPE, methodHandle, methodHandle.type()
            ).getTarget().invokeExact();
        } catch (final Throwable x) {
            throw new RuntimeException(
                    "An exception occurred while trying to convert constructor " + constructor + " to Supplier", x
            );
        }
    }

    /**
     * Creates a {@link Supplier} to get the value of the given static field.
     *
     * @param field static field from which to create a {@link Supplier}
     * @param <V> type of field value
     * @return supplier getting the value of the field
     * @throws IllegalArgumentException if the given field is not static
     */
    public <V> @NotNull Supplier<V> toStaticGetterSupplier(final @NonNull Field field) {
        Check.isStatic(field);

        final MethodHandle methodHandle;
        try {
            methodHandle = lookup(field.getDeclaringClass()).unreflectGetter(field);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Unable to create a MethodHandle for getter of field " + field, e);
        }

        return () -> {
            try {
                //noinspection unchecked
                return (V) methodHandle.invoke();
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }

    /**
     * Creates a {@link Supplier} to get the value of the given non-static field of the given target.
     *
     * @param field static field from which to create a {@link Supplier}
     * @param target object whose field value should be got
     * @param <V> type of field value
     * @return supplier getting the value of the field
     * @throws IllegalArgumentException if the given field is static
     */
    public <V> @NotNull Supplier<V> toBoundGetterSupplier(final @NonNull Field field, final @NonNull Object target) {
        Check.isNotStatic(field);

        final MethodHandle methodHandle;
        try {
            methodHandle = lookup(field.getDeclaringClass()).unreflectGetter(field).bindTo(target);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Unable to create a MethodHandle for getter of field " + field, e);
        }

        return () -> {
            try {
                //noinspection unchecked
                return (V) methodHandle.invoke();
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }

    /**
     * Creates a {@link Function} to get the value of the given non-static field.
     *
     * @param field static field from which to create a {@link Function}
     * @param <T> type of target class
     * @param <V> type of field value
     * @return function getting the value of the field
     * @throws IllegalArgumentException if the given field is static
     */
    public <T, V> @NotNull Function<T, V> toGetterFunction(final @NonNull Field field) {
        Check.isNotStatic(field);

        final MethodHandle methodHandle;
        try {
            methodHandle = lookup(field.getDeclaringClass()).unreflectGetter(field);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Unable to create a MethodHandle for getter of field " + field, e);
        }

        return target -> {
            try {
                //noinspection unchecked
                return (V) methodHandle.invoke(target);
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }

    /**
     * Creates a {@link Consumer} to set the value of the given static field.
     *
     * @param field static field from which to create a {@link Consumer}
     * @param <V> type of field value
     * @return consumer setting the value of the field
     * @throws IllegalArgumentException if the given field is not static
     */
    public <V> @NotNull Consumer<V> toStaticSetterConsumer(final @NonNull Field field) {
        val methodHandle = createSetterMethodHandle(field, Check.isStatic(field));

        return value -> {
            try {
                methodHandle.invoke(value);
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }

    /**
     * Creates a {@link Consumer} to set the value of the given non-static field of the given target.
     *
     * @param field static field from which to create a {@link Consumer}
     * @param target object whose field value should be set
     * @param <V> type of field value
     * @return consumer setting the value of the field
     * @throws IllegalArgumentException if the given field is static
     */
    public <V> @NotNull Consumer<V> toBoundSetterConsumer(final @NonNull Field field, final @NonNull Object target) {
        final MethodHandle methodHandle;
        if (Modifier.isFinal(Check.isNotStatic(field))) {
            val accessible = field.isAccessible();
            field.setAccessible(true);
            try {
                methodHandle = lookup(field.getDeclaringClass()).unreflectSetter(field).bindTo(target);
            } catch (final IllegalAccessException e) {
                throw new RuntimeException("Unable to create a MethodHandle for setter of field " + field, e);
            } finally {
                field.setAccessible(accessible);
            }
        } else try {
            methodHandle = lookup(field.getDeclaringClass()).unreflectSetter(field).bindTo(target);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Unable to create a MethodHandle for setter of field " + field, e);
        }

        return value -> {
            try {
                methodHandle.invoke(value);
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }

    /**
     * Creates a {@link BiConsumer} to set the value of the given non-static field.
     *
     * @param field static field from which to create a {@link BiConsumer}
     * @param <T> type of target class
     * @param <V> type of field value
     * @return bi-consumer setting the value of the field
     * @throws IllegalArgumentException if the given field is static
     */
    public <T, V> @NotNull BiConsumer<T, V> toSetterBiConsumer(final @NonNull Field field) {
        val methodHandle = createSetterMethodHandle(field, Check.isNotStatic(field));

        return (target, value) -> {
            try {
                methodHandle.invoke(target, value);
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }

    private static @NotNull MethodHandle createSetterMethodHandle(final @NonNull Field field, final int modifiers) {
        final MethodHandle methodHandle;
        if (Modifier.isFinal(modifiers)) {
            val accessible = field.isAccessible();
            field.setAccessible(true);
            try {
                methodHandle = lookup(field.getDeclaringClass()).unreflectSetter(field);
            } catch (final IllegalAccessException e) {
                throw new RuntimeException("Unable to create a MethodHandle for setter of field " + field, e);
            } finally {
                field.setAccessible(accessible);
            }
        } else try {
            methodHandle = lookup(field.getDeclaringClass()).unreflectSetter(field);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Unable to create a MethodHandle for setter of field " + field, e);
        }
        return methodHandle;
    }

    @UtilityClass
    @SuppressWarnings("TypeMayBeWeakened") // the error message are specific to classes
    private static final class Check {

        private static void hasNoParameters(final @NotNull Method method) {
            final int parameterCount;
            if ((parameterCount = method.getParameterCount()) != 0) throw new IllegalArgumentException(
                    "Method should have no parameters but it has " + parameterCount
            );
        }

        private static void hasNoParameters(final @NotNull Constructor<?> constructor) {
            final int parameterCount;
            if ((parameterCount = constructor.getParameterCount()) != 0) throw new IllegalArgumentException(
                    "Constructor should have no parameters but it has " + parameterCount
            );
        }

        private int isStatic(final @NotNull Method method) {
            final int modifiers;
            if (!Modifier.isStatic(modifiers = method.getModifiers())) throw new IllegalArgumentException(
                    "Method should be static"
            );

            return modifiers;
        }

        private int isStatic(final @NotNull Field field) {
            final int modifiers;
            if (!Modifier.isStatic(modifiers = field.getModifiers())) throw new IllegalArgumentException(
                    "Field should be static"
            );

            return modifiers;
        }

        private int isNotStatic(final @NotNull Method method) {
            final int modifiers;
            if (Modifier.isStatic(modifiers = method.getModifiers())) throw new IllegalArgumentException(
                    "Method should be static"
            );

            return modifiers;
        }

        private int isNotStatic(final @NotNull Field field) {
            final int modifiers;
            if (Modifier.isStatic(modifiers = field.getModifiers())) throw new IllegalArgumentException(
                    "Field should be static"
            );

            return modifiers;
        }
    }
}
