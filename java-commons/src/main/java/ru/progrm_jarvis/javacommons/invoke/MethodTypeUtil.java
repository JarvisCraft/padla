package ru.progrm_jarvis.javacommons.invoke;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.classloading.ClassUtil;

import java.lang.invoke.MethodType;

/**
 * Utilities related to {@link java.lang.invoke.MethodType method types}.
 */
@UtilityClass
public class MethodTypeUtil {

    /**
     * <p>Integrates the original {@link MethodType method-type} with the target
     * making so that specified {@link MethodType target-type}'s parameters
     * should be assignable from the corresponding ones of the {@link MethodType integrated type}.</p>
     *
     * <p>Note that indices are used to specify which parameter types should be integrated,
     * e.g. if {@code originalStartIndex} is {@code 2}, {@code targetStartIndex} is {@code 4}
     * and {@code parameterCount} is {@code 3} then
     * the 2'nd original parameter will be integrated with the 4'th target parameter,
     * the 3'rd original parameter will be integrated with the 5'th target parameter
     * and the 4'th original parameter will be integrated with the 6'th target parameter.</p>
     *
     * @param original original type which should be integrated to the target type
     * @param originalStartIndex index from which to start in the original
     * @param target target type to which the original type should be integrated
     * @param targetStartIndex index from which to start in the target
     * @param parameterCount amount of the parameters to integrate
     * @return result of type integration, my be the same as original type
     *
     * @throws IllegalArgumentException if any index (including calculated last used indices) is out of its bounds
     * @throws IllegalArgumentException if any original parameter type cannot be integrated to the target parameter type
     *
     * @see ClassUtil#integrateType(Class, Class) for specification of integration process
     * @see #integrateTypes(MethodType, MethodType) shorthand for full integration of types with same parameter counts
     */
    public @NotNull MethodType integrateTypes(/* mutable */ @NonNull MethodType original,
                                              /* mutable */ int originalStartIndex,
                                              final @NonNull MethodType target,
                                              /* mutable */ int targetStartIndex,
                                              final int parameterCount) {
        if (parameterCount < 0) throw new IllegalArgumentException("Parameter count should be non-negative");

        {
            final int originalParameterCount;
            if (originalStartIndex < 0
                    || originalStartIndex > (originalParameterCount = original.parameterCount())
                    || originalStartIndex + parameterCount > originalParameterCount
            ) throw new IllegalArgumentException(
                        "Original index should not exceed amount of original parameters or be negative"
            );
        }
        {
            final int targetParameterCount;
            if (targetStartIndex < 0
                    || targetStartIndex > (targetParameterCount = target.parameterCount())
                    || targetStartIndex + parameterCount > targetParameterCount
            ) throw new IllegalArgumentException(
                    "Target index should not exceed amount of target parameters or be negative"
            );
        }

        for (var i = 0; i < parameterCount; i++, originalStartIndex++, targetStartIndex++) original = original
                .changeParameterType(originalStartIndex, ClassUtil.integrateType(
                        original.parameterType(originalStartIndex), target.parameterType(targetStartIndex)
                ));

        return original;
    }

    /**
     * Shorthand for {@link #integrateTypes(MethodType, int, MethodType, int, int)}
     * for both types having the same amount of parameters each of which should be integrated.
     *
     * @param original original type which should be integrated to the target type
     * @param target target type to which the original type should be integrated
     * @return result of type integration, my be the same as original type
     *
     * @see #integrateTypes(MethodType, int, MethodType, int, int) full form of this method
     */
    public @NotNull MethodType integrateTypes(final @NonNull MethodType original,
                                              final @NonNull MethodType target) {
        val originalParameterCount = original.parameterCount();
        if (originalParameterCount != target.parameterCount()) throw new IllegalArgumentException(
                "Original and target should have the same amount of parameters"
        );

        return integrateTypes(original, 0, target, 0, originalParameterCount);
    }
}
