package ru.progrm_jarvis.reflector.invoke;

import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.invoke.MethodType.methodType;

/**
 * Factory for implementing functional interfaces at runtime.
 *
 * @param <F> type of functional interface implemented
 * @param <T> type of target value
 */
public interface InvokeFactory<F, T> {

    /**
     * Specifies the lookup factory to be used for creation of lookup
     *
     * @param lookupFactory lookup factory to be used for creation of lookup
     * @return <b>this</b> invoke factory
     */
    InvokeFactory<F, T> using(@NonNull LookupFactory lookupFactory);

    /**
     * Specifies the functional interface implemented.
     *
     * @param functionalInterface type of the implemented functional interface
     * (always has return type and no parameters)
     * @param functionalMethodName name of the implemented functional method
     * @param functionalMethodSignature signature of the implemented functional method
     * @return <b>this</b> invoke factory
     * @throws IllegalArgumentException if {@code functionalInterface} has parameters
     *
     * @apiNote even if the generated functional interface is going to be bound instance, {@code functionalInterface}
     * should have no parameters
     */
    InvokeFactory<F, T> implementing(@NonNull MethodType functionalInterface,
                                     @NonNull String functionalMethodName,
                                     @NonNull MethodType functionalMethodSignature);

    /**
     * Specifies the functional interface implemented.
     *
     * @param functionalInterface type of the implemented functional interface
     * @param functionalMethodName name of the implemented functional method
     * @param functionalMethodReturnType return type of the functional method
     * @param functionalMethodParameterTypes parameter types of the functional method
     * @return <b>this</b> invoke factory
     */
    default InvokeFactory<F, T> implementing(@NonNull Class<? super F> functionalInterface,
                                             @NonNull String functionalMethodName,
                                             @NonNull Class<?> functionalMethodReturnType,
                                             @NonNull Class<?>... functionalMethodParameterTypes) {
        return implementing(
                methodType(functionalInterface), functionalMethodName,
                methodType(functionalMethodReturnType, functionalMethodParameterTypes)
        );
    }

    /**
     * Specifies the functional interface implemented.
     *
     * @param functionalInterface type of the implemented functional interface which will be searched
     * for a single abstract method
     * @return <b>this</b> invoke factory
     * @throws IllegalArgumentException if the given class contains not 1 abstract (aka functional) method
     */
    default InvokeFactory<F, T> implementing(@NonNull Class<? super F> functionalInterface) {
        checkArgument(functionalInterface.isInterface(), "%s should be an interface", functionalInterface);

        final Method method;
        {
            val methods = Arrays.stream(functionalInterface.getMethods())
                    .filter(m -> Modifier.isAbstract(m.getModifiers()))
                    .collect(Collectors.toList());
            checkArgument(methods.size() == 1, "There should only be one abstract method in %s", functionalInterface);

            method = methods.get(0);
        }

        return implementing(functionalInterface, method.getName(), method.getReturnType(), method.getParameterTypes());
    }

    /**
     * Specifies the target class with function which will be used to create a related {@link MethodHandle}.
     *
     * @param targetClass class to whom the method-handle relates
     * @param methodHandleCreator function creating a {@link MethodHandle} using the given lookup
     * @return <b>this</b> invoke factory
     *
     * @see #via(Method)
     * @see #via(Constructor)
     */
    InvokeFactory<F, T> via(@NonNull Class<? extends T> targetClass,
                            @NonNull Function<MethodHandles.Lookup, MethodHandle> methodHandleCreator);

    /**
     * Specifies the method to be invoked by the call to functional interface's method.
     *
     * @param method method to call
     * @return <b>this</b> invoke factory
     *
     * @see #via(Class, Function)
     * @see #via(Constructor)
     */
    default InvokeFactory<F, T> via(@NonNull final Method method) {
        //noinspection unchecked
        return via((Class<? extends T>) method.getDeclaringClass(), lookup -> {
            try {
                return lookup.unreflect(method);
            } catch (final IllegalAccessException e) {
                throw new RuntimeException("Unable to unreflect method " + method, e);
            }
        });
    }

    /**
     * Specifies the constructor to be invoked by the call to functional interface's method.
     *
     * @param constructor constructor to call
     * @return <b>this</b> invoke factory
     *
     * @see #via(Class, Function)
     * @see #via(Method)
     */
    default InvokeFactory<F, T> via(@NonNull final Constructor<? extends T> constructor) {
        return via(constructor.getDeclaringClass(), lookup -> {
            try {
                return lookup.unreflectConstructor(constructor);
            } catch (final IllegalAccessException e) {
                throw new RuntimeException("Unable to unreflect method " + constructor, e);
            }
        });
    }

    /*
    // Those methods *could* hav been implemented but JDK's default LambdaMetaFactory is not capable
    // of generating functional interfaces for <put|get>field instructions ;-(
    // and the approach of manual creation of functional interfaces is not applicable as from side of Reflector API
    // the type of passed functional interface is not known at compile-time (of the very API)

    InvokeFactory<F, T> viaGetter(@NonNull Field field);

    InvokeFactory<F, T> viaSetter(@NonNull Field field);
     */

    /**
     * Binds the method call to the specified instance.
     *
     * @param target instance of the target class to use for non-static invocation or {@code null}
     * to unbound (make the call static)
     * @return <b>this</b> invoke factory
     */
    InvokeFactory<F, T> boundTo(@Nullable final T target);

    /**
     * Unbinds the method call from instance (making it static).
     *
     * @return <b>this</b> invoke factory
     */
    InvokeFactory<F, T> unbound();

    /**
     * Creates the implementation of the functional interface.
     *
     * @return implemented  functional interface
     * @throws IllegalStateException if any of the required factory properties has not been set
     * @throws Throwable if an exception occurs while creating an implementation of the functional interface
     */
    F create() throws Throwable;
}
