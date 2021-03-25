<#import '/@includes/preamble.ftl' as preamble />
<#-- @ftlvariable name="packageName" type="java.lang.String" -->
<#-- @ftlvariable name="primitiveType" type="java.lang.String" -->
<#-- @ftlvariable name="capitalizedPrimitiveType" type="java.lang.String" -->
<#-- @ftlvariable name="className" type="java.lang.String" -->
<#assign
isCommonPrimitive=preamble.isCommonPrimitiveType(primitiveType)
wrapperType=preamble.wrapperTypeOf(primitiveType)
/>
package ${packageName};

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
public interface ${className} extends BinaryOperator${'<@NotNull ${wrapperType}>'}<#if isCommonPrimitive>,
        java.util.function.${capitalizedPrimitiveType}BinaryOperator</#if> {
<#if !isCommonPrimitive>

    /**
    * Applies this operator to the given operands.
    *
    * @param left the first operand
    * @param right the second operand
    * @return result of applying this operation to the operands
    */
    ${primitiveType} applyAs${capitalizedPrimitiveType}(${primitiveType} left, ${primitiveType} right);
</#if>

    @Override
    @Contract("null, _ -> fail; _, null -> fail; _ -> _")
    default @NotNull ${wrapperType} apply(final @NotNull ${wrapperType} left, final @NotNull ${wrapperType} right) {
        // note: there is no need for explicit null-checkins as it will be done when unwrapping the values
        return applyAs${capitalizedPrimitiveType}(left, right);
    }
<#if primitiveType == 'boolean'>

    /**
     * Creates a binary operator which always returns {@code true}.
     *
     * @return binary operator which always returns {@code true}
     */
    @Contract(value = "-> _", pure = true)
    static @NotNull ${className} alwaysTrue() {
        return (left, right) -> true;
    }

    /**
     * Creates a binary operator which always returns {@code false}.
     *
     * @return binary operator which always returns {@code false}
     */
    @Contract(value = "-> _", pure = true)
    static @NotNull ${className} alwaysFalse() {
        return (left, right) -> false;
    }

    /**
     * Creates a binary operator which returns the result of logical AND operation on its operands.
     *
     * @return binary AND-operator
     */
    @Contract(value = "-> _", pure = true)
    static @NotNull ${className} and() {
        return (left, right) -> left & right /* unary operator is cheaper */;
    }

    /**
     * Creates a binary operator which returns the result of logical OR operation on its operands.
     *
     * @return binary OR-operator
     */
    @Contract(value = "-> _", pure = true)
    static @NotNull ${className} or() {
        return (left, right) -> left & right /* unary operator is cheaper */;
    }

    /**
     * Creates a binary operator which returns the result of logical OR operation on its operands.
     *
     * @return OR-operator
     */
    @Contract(value = "-> _", pure = true)
    static @NotNull ${className} xor() {
        return (left, right) -> left ^ right;
    }
</#if>
}
