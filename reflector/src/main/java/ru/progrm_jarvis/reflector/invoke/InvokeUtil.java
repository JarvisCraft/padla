/*
 * Copyright 2019 Feather Core
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.progrm_jarvis.reflector.invoke;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
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
     * Name of a system property responsible for {@link #LOOKUPS} concurrency level.
     */
    @NonNull public final String LOOKUP_CACHE_CONCURRENCY_LVELSYSTEM_PROPERTY_NAME
            = InvokeUtil.class.getCanonicalName() + ".lookup-cache-concurrency-level";

    /**
     * Lookup factory used by this utility
     */
    private final LookupFactory LOOKUP_FACTORY = LookupFactory.TRUSTED_LOOKUP_FACTORY.get();

    /**
     * Method of {@link Runnable} functional method
     */
    private final String RUNNABLE_FUNCTIONAL_METHOD_NAME = "run",
    /**
     * Method of {@link Supplier} functional method
     */
    SUPPLIER_FUNCTIONAL_METHOD_NAME = "get";

    /**
     * Method type of signature: <code>{@code void}()</code>
     */
    private final MethodType VOID__METHOD_TYPE = methodType(void.class),
    /**
     * Method type of signature: <code>{@link Object}()</code>
     */
    OBJECT__METHOD_TYPE = methodType(Object.class),
    /**
     * Method type of signature: <code>{@link Runnable}()</code>
     */
    RUNNABLE__METHOD_TYPE = methodType(Runnable.class),
    /**
     * Method type of signature: <code>{@link Supplier}()</code>
     */
    SUPPLIER__METHOD_TYPE = methodType(Supplier.class),
    /**
     * Method type of signature: <code>{@link Runnable}({@link Object})</code>
     */
    RUNNABLE_OBJECT__METHOD_TYPE = methodType(Runnable.class, Object.class),
    /**
     * Method type of signature: <code>{@link Supplier}({@link Object})</code>
     */
    SUPPLIER_OBJECT__METHOD_TYPE = methodType(Supplier.class, Object.class);

    @SuppressWarnings("UnstableApiUsage") @NonNull private Cache<Class<?>, Lookup> LOOKUPS
            = CacheBuilder.newBuilder()
            .softValues() // because there is no need to GC lookups which may be expansive to create
            .concurrencyLevel(Math.max(1, Integer.getInteger(LOOKUP_CACHE_CONCURRENCY_LVELSYSTEM_PROPERTY_NAME, 4)))
            .build();

    /**
     * Creates a cache {@link Lookup} for the given class.
     *
     * @param clazz class for which to create a lookup
     * @return created cached lookup fir the given class
     */
    @SneakyThrows(ExecutionException.class)
    public Lookup lookup(@NonNull final Class<?> clazz) {
        return LOOKUPS.get(clazz, () -> LOOKUP_FACTORY.create(clazz));
    }

    /**
     * Converts the given method to a {@link MethodHandle}.
     *
     * @param method method to convert to {@link MethodHandle}
     * @param lookup lookup to use for conversion
     * @return {@link MethodHandle} created from the given method
     */
    public MethodHandle toMethodHandle(@NonNull final Method method, @NonNull final Lookup lookup) {
        final MethodHandle methodHandle;
        {
            val accessible = method.isAccessible();
            method.setAccessible(true);
            try {
                methodHandle = lookup.unreflect(method);
            } catch (final IllegalAccessException e) {
                throw new RuntimeException("Method " + method + " cannot be accessed", e);
            } finally {
                method.setAccessible(accessible);
            }
        }
        return methodHandle;
    }

    /**
     * Converts the given constructor to a {@link MethodHandle}.
     *
     * @param constructor constructor to convert to {@link MethodHandle}
     * @param lookup lookup to use for conversion
     * @return {@link MethodHandle} created from the given constructor
     */
    public MethodHandle toMethodHandle(@NonNull final Constructor<?> constructor, @NonNull final Lookup lookup) {
        final MethodHandle methodHandle;
        {
            val accessible = constructor.isAccessible();
            constructor.setAccessible(true);
            try {
                methodHandle = lookup.unreflectConstructor(constructor);
            } catch (final IllegalAccessException e) {
                throw new RuntimeException("Constructor " + constructor + " cannot be accessed", e);
            } finally {
                constructor.setAccessible(accessible);
            }
        }
        return methodHandle;
    }

    /**
     * Converts the given field to a getter-{@link MethodHandle}.
     *
     * @param field field to convert to getter-{@link MethodHandle}
     * @param lookup lookup to use for conversion
     * @return getter-{@link MethodHandle} created from the given field
     */
    public MethodHandle toGetterMethodHandle(@NonNull final Field field, @NonNull final Lookup lookup) {
        final MethodHandle methodHandle;
        {
            val accessible = field.isAccessible();
            field.setAccessible(true);
            try {
                methodHandle = lookup.unreflectGetter(field);
            } catch (final IllegalAccessException e) {
                throw new RuntimeException("Method " + field + " cannot be accessed", e);
            } finally {
                field.setAccessible(accessible);
            }
        }
        return methodHandle;
    }

    /**
     * Converts the given field to a setter-{@link MethodHandle}.
     *
     * @param field field to convert to setter-{@link MethodHandle}
     * @param lookup lookup to use for conversion
     * @return setter-{@link MethodHandle} created from the given field
     */
    public MethodHandle toSetterMethodHandle(@NonNull final Field field, @NonNull final Lookup lookup) {
        final MethodHandle methodHandle;
        {
            val accessible = field.isAccessible();
            field.setAccessible(true);
            try {
                methodHandle = lookup.unreflectSetter(field);
            } catch (final IllegalAccessException e) {
                throw new RuntimeException("Method " + field + " cannot be accessed", e);
            } finally {
                field.setAccessible(accessible);
            }
        }
        return methodHandle;
    }

    /**
     * Creates a {@link Runnable} to invoke the given static method.
     *
     * @param method static method from which to create a {@link Runnable}
     * @return runnable invoking the given method
     * @throws IllegalArgumentException if the given method requires parameters
     * @throws IllegalArgumentException if the given method is not static
     */
    public Runnable toStaticRunnable(@NonNull final Method method) {
        {
            val parameterCount = method.getParameterCount();
            checkArgument(parameterCount == 0, "method should have no parameters, got " + parameterCount);
        }
        checkArgument(Modifier.isStatic(method.getModifiers()), "method should be static");

        val lookup = lookup(method.getDeclaringClass());
        val methodHandle = toMethodHandle(method, lookup);
        try {
            return (Runnable) metafactory(
                    lookup, RUNNABLE_FUNCTIONAL_METHOD_NAME, RUNNABLE__METHOD_TYPE,
                    VOID__METHOD_TYPE, methodHandle, VOID__METHOD_TYPE
            ).getTarget().invokeExact();
        } catch (final Throwable throwable) {
            throw new RuntimeException(
                    "An exception occurred while trying to convert method " + method + "to Runnable"
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
    public Runnable toBoundRunnable(@NonNull final Method method, @NonNull final Object target) {
        {
            val parameterCount = method.getParameterCount();
            checkArgument(parameterCount == 0, "method should have no parameters, got " + parameterCount);
        }
        checkArgument(!Modifier.isStatic(method.getModifiers()), "method should be non-static");

        val lookup = lookup(method.getDeclaringClass());
        val methodHandle = toMethodHandle(method, lookup);
        try {
            return (Runnable) metafactory(
                    lookup, RUNNABLE_FUNCTIONAL_METHOD_NAME,
                    RUNNABLE_OBJECT__METHOD_TYPE.changeParameterType(0, target.getClass()),
                    VOID__METHOD_TYPE, methodHandle, VOID__METHOD_TYPE
            ).getTarget().invoke(target);
        } catch (final Throwable throwable) {
            throw new RuntimeException(
                    "An exception occurred while trying to convert method " + method + "to Runnable"
            );
        }
    }

    /**
     * Creates a {@link Supplier} to invoke the given static method getting its returned value.
     *
     * @param method static method from which to create a {@link Supplier}
     * @return runnable invoking the given method
     * @throws IllegalArgumentException if the given method requires parameters
     * @throws IllegalArgumentException if the given method is not static
     */
    public <T> Supplier<T> toStaticSupplier(@NonNull final Method method) {
        {
            val parameterCount = method.getParameterCount();
            checkArgument(parameterCount == 0, "method should have no parameters, got " + parameterCount);
        }
        checkArgument(Modifier.isStatic(method.getModifiers()), "method should be static");

        val lookup = lookup(method.getDeclaringClass());
        val methodHandle = toMethodHandle(method, lookup);
        try {
            //noinspection unchecked: generic type o returned object
            return (Supplier<T>) metafactory(
                    lookup, SUPPLIER_FUNCTIONAL_METHOD_NAME, SUPPLIER__METHOD_TYPE,
                    OBJECT__METHOD_TYPE, methodHandle, methodHandle.type()
            ).getTarget().invokeExact();
        } catch (final Throwable throwable) {
            throw new RuntimeException(
                    "An exception occurred while trying to convert method " + method + "to Supplier"
            );
        }
    }

    /**
     * Creates a {@link Supplier} to invoke the given non-static method on the given target getting its returned value.
     *
     * @param method non-static method from which to create a {@link Supplier}
     * @param target object on which the method should be invoked
     * @return supplier invoking the given method
     * @throws IllegalArgumentException if the given method requires parameters
     * @throws IllegalArgumentException if the given method is static
     */
    public <T> Supplier<T> toBoundSupplier(@NonNull final Method method, @NonNull final Object target) {
        {
            val parameterCount = method.getParameterCount();
            checkArgument(parameterCount == 0, "method should have no parameters, got " + parameterCount);
        }
        checkArgument(!Modifier.isStatic(method.getModifiers()), "method should be non-static");

        val lookup = lookup(method.getDeclaringClass());
        val methodHandle = toMethodHandle(method, lookup);
        try {
            //noinspection unchecked: generic type o returned object
            return (Supplier<T>) metafactory(
                    lookup, SUPPLIER_FUNCTIONAL_METHOD_NAME,
                    SUPPLIER_OBJECT__METHOD_TYPE.changeParameterType(0, target.getClass()),
                    OBJECT__METHOD_TYPE, methodHandle, methodType(method.getReturnType())
            ).getTarget().invoke(target);
        } catch (final Throwable throwable) {
            throw new RuntimeException(
                    "An exception occurred while trying to convert method " + method + "to Supplier"
            );
        }
    }

    /**
     * Creates a {@link Supplier} to invoke the given constructor.
     *
     * @param constructor constructor from which to create a {@link Supplier}
     * @return supplier invoking the given constructor
     * @throws IllegalArgumentException if the given constructor requires parameters
     */
    public <T> Supplier<T> toSupplier(@NonNull final Constructor<T> constructor) {
        {
            val parameterCount = constructor.getParameterCount();
            checkArgument(parameterCount == 0, "method should have no parameters, got " + parameterCount);
        }

        val lookup = lookup(constructor.getDeclaringClass());
        val methodHandle = toMethodHandle(constructor, lookup);
        try {
            //noinspection unchecked: generic type o returned object
            return (Supplier<T>) metafactory(
                    lookup, SUPPLIER_FUNCTIONAL_METHOD_NAME, SUPPLIER__METHOD_TYPE,
                    OBJECT__METHOD_TYPE, methodHandle, methodHandle.type()
            ).getTarget().invokeExact();
        } catch (final Throwable throwable) {
            throw new RuntimeException(
                    "An exception occurred while trying to convert constructor " + constructor + "to Supplier"
            );
        }
    }

    /**
     * Creates a {@link Supplier} to get the value of the given static field.
     *
     * @param field static field from which to create a {@link Supplier}
     * @return supplier getting the value of the field
     * @throws IllegalArgumentException if the given field is not static
     */
    public <V> Supplier<V> toStaticGetterSupplier(@NonNull final Field field) {
        checkArgument(Modifier.isStatic(field.getModifiers()), "field should be static");

        val lookup = lookup(field.getDeclaringClass());
        val methodHandle = toGetterMethodHandle(field, lookup);

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
     * @return supplier getting the value of the field
     * @throws IllegalArgumentException if the given field is static
     */
    public <V> Supplier<V> toBoundGetterSupplier(@NonNull final Field field, @NonNull final Object target) {
        checkArgument(!Modifier.isStatic(field.getModifiers()), "field should be non-static");

        val lookup = lookup(field.getDeclaringClass());
        val methodHandle = toGetterMethodHandle(field, lookup);

        return () -> {
            try {
                //noinspection unchecked
                return (V) methodHandle.invoke(target);
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }

    /**
     * Creates a {@link Function} to get the value of the given non-static field.
     *
     * @param field static field from which to create a {@link Function}
     * @return function getting the value of the field
     * @throws IllegalArgumentException if the given field is static
     */
    public <T, V> Function<T, V> toGetterFunction(@NonNull final Field field) {
        checkArgument(!Modifier.isStatic(field.getModifiers()), "field should be non-static");

        val lookup = lookup(field.getDeclaringClass());
        val methodHandle = toGetterMethodHandle(field, lookup);

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
     * @return consumer setting the value of the field
     * @throws IllegalArgumentException if the given field is not static
     */
    public <V> Consumer<V> toStaticSetterConsumer(@NonNull final Field field) {
        checkArgument(Modifier.isStatic(field.getModifiers()), "field should be static");

        val lookup = lookup(field.getDeclaringClass());
        val methodHandle = toSetterMethodHandle(field, lookup);

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
     * @return consumer setting the value of the field
     * @throws IllegalArgumentException if the given field is static
     */
    public <V> Consumer<V> toBoundSetterConsumer(@NonNull final Field field, @NonNull final Object target) {
        checkArgument(!Modifier.isStatic(field.getModifiers()), "field should be non-static");

        val lookup = lookup(field.getDeclaringClass());
        val methodHandle = toSetterMethodHandle(field, lookup);

        return value -> {
            try {
                methodHandle.invoke(target, value);
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }

    /**
     * Creates a {@link BiConsumer} to set the value of the given non-static field.
     *
     * @param field static field from which to create a {@link BiConsumer}
     * @return bi-consumer setting the value of the field
     * @throws IllegalArgumentException if the given field is static
     */
    public <T, V> BiConsumer<T, V> toSetterBiConsumer(@NonNull final Field field) {
        checkArgument(!Modifier.isStatic(field.getModifiers()), "field should be non-static");

        val lookup = lookup(field.getDeclaringClass());
        val methodHandle = toSetterMethodHandle(field, lookup);

        return (target, value) -> {
            try {
                methodHandle.invoke(target, value);
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }
}
