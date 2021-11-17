package ru.progrm_jarvis.javacommons.invoke;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Function;

/**
 * Simple implementation of {@link InvokeFactory}.
 *
 * @param <F> type of functional interface implemented
 * @param <T> type of target value
 */
@ToString
@FieldDefaults(level = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleInvokeFactory<F, T> implements InvokeFactory<F, T> {

    /**
     * Used lookup factory
     */
    LookupFactory lookupFactory;
    /**
     * Implemented functional interface
     */
    MethodType functionalInterface,
    /**
     * Signature of the functional interface's functional method
     */
    functionalMethodSignature;
    /**
     * Name of the functional interface's functional method
     */
    String functionalMethodName;
    /**
     * Factory creating implementation method handle using the given lookup
     */
    Function<MethodHandles.Lookup, MethodHandle> methodHandleCreator;
    /**
     * Target class containing the implementation method
     */
    Class<? extends T> targetClass;
    /**
     * Target
     */
    @Nullable Object target;

    /**
     * Creates a new simple {@link InvokeFactory invoke factory}.
     *
     * @param <F> type of functional interface implemented
     * @param <T> type of target value
     * @return created invoke factory
     */
    public static <F, T> @NotNull InvokeFactory<F, T> newInstance() {
        return new SimpleInvokeFactory<>();
    }

    @Override
    public InvokeFactory<F, T> using(final @NonNull LookupFactory lookupFactory) {
        this.lookupFactory = lookupFactory;

        return this;
    }

    @Override
    public InvokeFactory<F, T> implementing(final @NonNull MethodType functionalInterface,
                                            final @NonNull String functionalMethodName,
                                            final @NonNull MethodType functionalMethodSignature) {
        if (functionalInterface.parameterCount() != 0) throw new IllegalArgumentException(
                "Functional interface should have no parameters"
        );

        this.functionalInterface = functionalInterface;
        this.functionalMethodName = functionalMethodName;
        this.functionalMethodSignature = functionalMethodSignature;

        return this;
    }

    @Override
    public InvokeFactory<F, T> via(final @NonNull Class<? extends T> targetClass,
                                   final @NonNull Function<MethodHandles.Lookup, MethodHandle> methodHandleCreator) {
        this.targetClass = targetClass;
        this.methodHandleCreator = methodHandleCreator;

        return this;
    }

    @Override
    public InvokeFactory<F, T> boundTo(final @Nullable T target) {
        this.target = target;

        return this;
    }

    @Override
    public InvokeFactory<F, T> unbound() {
        target = null;

        return this;
    }

    @Override
    @SuppressWarnings("unchecked") // cast of the value to `F`
    public F create() throws Throwable {
        val lookupFactory = checkSet(this.lookupFactory, "Lookup factory");
        val functionalInterface = checkSet(this.functionalInterface, "Functional interface");
        val functionalMethodSignature = checkSet(this.functionalMethodSignature, "Functional method signature");
        val functionalMethodName = checkSet(this.functionalMethodName, "Functional method name");
        val methodHandleCreator = checkSet(this.methodHandleCreator, "Method handle creator");

        val lookup = lookupFactory.create(targetClass);
        val methodHandle = methodHandleCreator.apply(lookup);

        if (target == null) {
            /* Not bound */

            val targetMethodHandle = LambdaMetafactory.metafactory(
                    lookup, functionalMethodName,
                    functionalInterface,
                    functionalMethodSignature, methodHandle,
                    MethodTypeUtil.integrateTypes(methodHandle.type(), functionalMethodSignature)
            ).getTarget();

            return (F) targetMethodHandle.invoke();
        }

        /* Bound */

        val targetMethodHandle = LambdaMetafactory.metafactory(
                lookup, functionalMethodName,
                functionalInterface.appendParameterTypes(target.getClass()),
                functionalMethodSignature, methodHandle,
                MethodTypeUtil.integrateTypes(methodHandle.type().dropParameterTypes(0, 1), functionalMethodSignature)
        ).getTarget();

        return (F) targetMethodHandle.invoke(target);
    }

    private static <T> @NotNull T checkSet(final @Nullable T value, final @NotNull String identifier) {
        if (value == null) throw new IllegalStateException(identifier + " is not set");

        return value;
    }
}
