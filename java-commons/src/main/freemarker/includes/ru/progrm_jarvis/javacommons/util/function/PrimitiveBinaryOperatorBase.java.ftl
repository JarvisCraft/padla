<#-- @ftlvariable name="primitiveType" type="java.lang.String" -->
<#-- @ftlvariable name="capitalizedPrimitiveType" type="java.lang.String" -->
<#-- @ftlvariable name="wrapperType" type="java.lang.String" -->
<#-- @ftlvariable name="className" type="java.lang.String" -->
package ru.progrm_jarvis.javacommons.util.function;

import org.jetbrains.annotations.NotNull;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/**
 * Represents an operation on a single {@code ${primitiveType}} operand that produces a {@code ${primitiveType}} result.
 * This is the primitive type specialization of {@link UnaryOperator} for {@code ${primitiveType}}.
 *
 * @see UnaryOperator non-primitive generic equivalent
 */
@FunctionalInterface
public interface ${className} extends BinaryOperator< @NotNull ${wrapperType}> {

    /**
     * Applies this operator to the given operands.
     *
     * @param left the first operand
     * @param right the second operand
     * @return result of applying this operation to the operands
     */
    ${primitiveType} applyAs${capitalizedPrimitiveType}(${primitiveType} left, ${primitiveType} right);

    @Override
    default ${wrapperType} apply(final @NotNull ${wrapperType} left, final @NotNull ${wrapperType} right) {
        // note: there is no need for explicit null-checkins as it will be done when unwrapping the values
        return applyAs${capitalizedPrimitiveType}(left, right);
    }
}
