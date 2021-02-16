<#-- @ftlvariable name="primitiveType" type="java.lang.String" -->
<#-- @ftlvariable name="capitalizedPrimitiveType" type="java.lang.String" -->
<#-- @ftlvariable name="wrapperType" type="java.lang.String" -->
<#-- @ftlvariable name="className" type="java.lang.String" -->
package ru.progrm_jarvis.javacommons.util.function;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Represents a predicate (boolean-valued function) of one argument.
 * This is the primitive type specialization of {@link Predicate} for {@code ${primitiveType}}.
 *
 * @see Predicate non-primitive generic equivalent
 */
@FunctionalInterface
public interface ${className} /*extends Predicate< @NotNull ${wrapperType}> */{

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param value the input argument
     * @return {@code true} if the input argument matches the predicate or {@code false} otherwise
     */
    boolean test(${primitiveType} value);

    //@Override
    //default boolean test(final @NotNull ${wrapperType} value) {
    //    return test(value.${primitiveType}Value());
    //}

    //@Override
    default @NotNull ${className} and(final @NonNull ${className} other) {
        return value -> test(value) && other.test(value);
    }

    //@Override
    default @NotNull ${className} negate() {
        return value -> !test(value);
    }

    //@Override
    default @NotNull ${className} or(final @NonNull ${className} other) {
        return value -> test(value) || other.test(value);
    }

    /**
     * Returns a predicate that tests if two arguments are equal.
     *
     * @param value value with which to compare the tested one
     * @return a predicate that tests if two arguments are equal
     */
    static @NotNull ${className} isEqual(final ${primitiveType} value) {
        return tested -> tested == value;
    }

    /**
     * Returns a predicate that tests if two arguments are not equal.
     *
     * @param value value with which to compare the tested one
     * @return a predicate that tests if two arguments are not equal
     */
    static @NotNull ${className} isNotEqual(final ${primitiveType} value) {
        return tested -> tested != value;
    }
}
