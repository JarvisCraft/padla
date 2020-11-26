package ru.progrm_jarvis.javacommons.invoke;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.javacommons.annotation.DontOverrideEqualsAndHashCode;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Simple implementation of {@link InvokeFactory}.
 *
 * @param <F> type of functional interface implemented
 * @param <T> type of target value
 */
@ToString
@DontOverrideEqualsAndHashCode("Class is more of an utility then a POJO")
@NoArgsConstructor(access = AccessLevel.PROTECTED, staticName = "newInstance")
@FieldDefaults(level = AccessLevel.PROTECTED)
public class SimpleInvokeFactory<F, T> implements InvokeFactory<F, T> {

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

    @Override
    public InvokeFactory<F, T> using(final @NonNull LookupFactory lookupFactory) {
        this.lookupFactory = lookupFactory;

        return this;
    }

    @Override
    public InvokeFactory<F, T> implementing(final @NonNull MethodType functionalInterface,
                                            final @NonNull String functionalMethodName,
                                            final @NonNull MethodType functionalMethodSignature) {
        checkArgument(functionalInterface.parameterCount() == 0, "functionalInterface should have no parameters");

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
        this.target = null;

        return this;
    }

    @Override
    public F create() throws Throwable {
        checkState(lookupFactory != null, "lookupFactory is not set");
        checkState(functionalInterface != null, "functionalInterface is not set");
        checkState(functionalMethodSignature != null, "functionalMethodSignature is not set");
        checkState(functionalMethodName != null, "functionalMethodName is not set");
        checkState(methodHandleCreator != null, "methodHandleCreator is not set");

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

            //noinspection unchecked
            return (F) targetMethodHandle.invoke();
        }

        /* Bound */

        /*
        var signature = methodHandle.type();
        {
            val functionalParameterCount = functionalMethodSignature.parameterCount();

            checkState(
                    signature.parameterCount() == functionalParameterCount + 1,
                    "Malformed parameter count: actual - %s, implemented - %s",
                    signature.parameterCount(), functionalParameterCount
            );

            // use indexed iteration not to create short-living collections
            for (var i = 1; i <= functionalParameterCount; i++) {
                final Class<?>
                        actualType = signature.parameterType(i),
                        functionalType = functionalMethodSignature.parameterType(i - 1);

                if (!functionalType.isAssignableFrom(actualType)) {
                    checkState(
                            actualType.isPrimitive(), "Parameter types are incompatible at index [%s]", i - 1
                    );

                    val wrapperType = ClassUtil.toPrimitiveWrapper(actualType);
                    checkState(
                            functionalType.isAssignableFrom(wrapperType),
                            "Parameter types are incompatible at index [%s] even with wrapping", i - 1
                    );
                    signature = signature.changeParameterType(i, wrapperType);
                }
            }
        }
        */

        val targetMethodHandle = LambdaMetafactory.metafactory(
                lookup, functionalMethodName,
                functionalInterface.appendParameterTypes(target.getClass()),
                functionalMethodSignature, methodHandle,
                MethodTypeUtil.integrateTypes(methodHandle.type().dropParameterTypes(0, 1), functionalMethodSignature)

        ).getTarget();

        //noinspection unchecked
        return (F) targetMethodHandle.invoke(target);
    }
}
