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

import lombok.NonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Represents a predicate (boolean-valued function) of one argument.
 * This is the primitive type specialization of {@link Predicate} for {@code ${primitiveType}}.
 *
 * @see Predicate non-primitive generic equivalent
 */
@FunctionalInterface
public interface ${className} extends Predicate${'<@NotNull ${wrapperType}>'}<#if isCommonPrimitive>,
        java.util.function.${capitalizedPrimitiveType}Predicate</#if> {

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param value the input argument
     * @return {@code true} if the input argument matches the predicate or {@code false} otherwise
     */
    boolean testAs${capitalizedPrimitiveType}(${primitiveType} value);
<#if isCommonPrimitive>

    @Override
    default boolean test(final ${primitiveType} value) {
        return testAs${capitalizedPrimitiveType}(value);
    }
</#if>

    @Override
    @Contract("null -> fail")
    default boolean test(final @NotNull ${wrapperType} value) {
        return testAs${capitalizedPrimitiveType}(value.${primitiveType}Value());
    }

    /**
     * <p>Returns a composed predicate that represents a short-circuiting logical AND of this predicate and another.
     * When evaluating the composed predicate, if this predicate is {@code false},
     * then the {@code other} predicate is not evaluated.</p>
     * <p>Any exceptions thrown during evaluation of either predicate are relayed
     * to the caller; if evaluation of this predicate throws an exception, the
     * {@code other} predicate will not be evaluated.</p>
     *
     * @param other a predicate that will be logically-ANDed with this predicate
     * @return a composed predicate that represents the short-circuiting logical AND
     * of this predicate and the {@code other} predicate
     *
     * @throws NullPointerException if other is {@code null}
     */
    @Contract(value = "null -> fail; _ -> _", pure = true)
    default @NotNull ${className} and(final @NonNull ${className} other) {
        return value -> testAs${capitalizedPrimitiveType}(value) && other.testAs${capitalizedPrimitiveType}(value);
    }

    @Override
    @Contract(value = "null -> fail; _ -> _", pure = true)
    default @NotNull ${className} and(final @NonNull Predicate< @NotNull ? super ${wrapperType}> other) {
        return value -> testAs${capitalizedPrimitiveType}(value) && other.test(value);
    }
<#if isCommonPrimitive>

    @Override
    @Contract(value = "null -> fail; _ -> _", pure = true)
    default @NotNull ${className} and(final @NonNull java.util.function.${capitalizedPrimitiveType}Predicate other) {
        return value -> testAs${capitalizedPrimitiveType}(value) && other.test(value);
    }
</#if>

    /**
     * <p>Returns a composed predicate that represents a short-circuiting logical OR of this predicate and another.
     * When evaluating the composed predicate, if this predicate is {@code true},
     * then the {@code other} predicate is not evaluated.</p>
     * <p>Any exceptions thrown during evaluation of either predicate are relayed
     * to the caller; if evaluation of this predicate throws an exception, the
     * {@code other} predicate will not be evaluated.</p>
     *
     * @param other a predicate that will be logically-ORed with this predicate
     * @return a composed predicate that represents the short-circuiting logical OR
     * of this predicate and the {@code other} predicate
     *
     * @throws NullPointerException if other is {@code null}
     */
    @Contract(value = "null -> fail; _ -> _", pure = true)
    default @NotNull ${className} or(final @NonNull ${className} other) {
        return value -> testAs${capitalizedPrimitiveType}(value) || other.testAs${capitalizedPrimitiveType}(value);
    }

    @Override
    @Contract(value = "null -> fail; _ -> _", pure = true)
    default @NotNull ${className} or(final @NonNull Predicate< @NotNull ? super ${wrapperType}> other) {
        return value -> testAs${capitalizedPrimitiveType}(value) || other.test(value);
    }
<#if isCommonPrimitive>

    @Override
    @Contract(value = "null -> fail; _ -> _", pure = true)
    default @NotNull ${className} or(final @NonNull java.util.function.${capitalizedPrimitiveType}Predicate other) {
        return value -> testAs${capitalizedPrimitiveType}(value) || other.test(value);
    }
</#if>

    @Override
    @Contract(value = "-> _", pure = true)
    default @NotNull ${className} negate() {
        return value -> !testAs${capitalizedPrimitiveType}(value);
    }

    /**
     * Returns a predicate that tests if two arguments are equal.
     *
     * @param value value with which to compare the tested one
     * @return a predicate that tests if two arguments are equal
     */
    @Contract(value = "_ -> _", pure = true)
    static @NotNull ${className} isEqual(final ${primitiveType} value) {
        return tested -> tested == value;
    }

    /**
     * Returns a predicate that tests if two arguments are not equal.
     *
     * @param value value with which to compare the tested one
     * @return a predicate that tests if two arguments are not equal
     */
    @Contract(value = "_ -> _", pure = true)
    static @NotNull ${className} isNotEqual(final ${primitiveType} value) {
        return tested -> tested != value;
    }

    /**
     * Creates a predicate which is always {@code true}.
     *
     * @return predicate which is always {@code true}
     */
    @Contract(value = "-> _", pure = true)
    static @NotNull DoublePredicate alwaysTrue() {
        return value -> true;
    }

    /**
     * Creates a predicate which is always {@code false}.
     *
     * @return predicate which is always {@code false}
     */
    @Contract(value = "-> _", pure = true)
    static @NotNull DoublePredicate alwaysFalse() {
        return value -> false;
    }
<#if primitiveType == 'boolean'>

    /**
     * Returns a composed operator that first applies this operator and and then inverts the result.
     *
     * @return a composed operator that first applies this operator and then inverts the result
     */
    @Contract(value = "-> _", pure = true)
    default @NotNull ${className} invert() {
        return operand -> !testAs${capitalizedPrimitiveType}(operand);
    }

    /**
     * Returns an unary operator that always returns its input argument.
     *
     * @return an unary operator that always returns its input argument
     */
    @Contract(value = "-> _", pure = true)
    static @NotNull ${className} identity() {
        return operand -> operand;
    }

    /**
     * Returns an unary operator that always returns inverted input argument.
     *
     * @return an unary operator that always returns inverted input argument
     */
    @Contract(value = "-> _", pure = true)
    static @NotNull ${className} inversion() {
        return operand -> !operand;
    }
</#if>
}
