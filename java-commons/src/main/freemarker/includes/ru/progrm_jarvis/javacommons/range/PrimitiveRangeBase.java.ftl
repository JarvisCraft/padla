<#import '/@includes/preamble.ftl' as preamble />
<#-- @ftlvariable name="packageName" type="java.lang.String" -->
<#-- @ftlvariable name="primitiveType" type="java.lang.String" -->
<#-- @ftlvariable name="capitalizedPrimitiveType" type="java.lang.String" -->
<#-- @ftlvariable name="className" type="java.lang.String" -->
<#assign wrapperType=preamble.wrapperTypeOf(primitiveType) />
package ${packageName};

import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.collection.Iterables;
import ru.progrm_jarvis.javacommons.ownership.annotation.Own;
import ru.progrm_jarvis.javacommons.ownership.annotation.Ref;
import ${preamble.rootPackage}.util.function.${capitalizedPrimitiveType}Predicate;

import java.util.*;

/**
 * {@link Range} specialization for {@code ${primitiveType}}.
 */
@FunctionalInterface
public interface ${className} extends ${capitalizedPrimitiveType}Predicate {

    /* ************************************************* Factories ************************************************* */

    /**
     * Creates a range <i>[-&infin; ; +&infin;]</i>.
     *
     * @return created range
     */
    static @NotNull ${className} any() {
        return value -> true;
    }

    /**
     * Creates a range <i>&empty;</i>.
     *
     * @return created range
     */
    static @NotNull ${className} none() {
        return value -> false;
    }

    /**
     * Creates a range <i>{{@code value}}</i>.
     *
     * @param value the only value contained by the range
     * @return created range
     */
    static @NotNull ${className} only(final ${primitiveType} value) {
        return tested -> tested == value;
    }

    /**
     * Creates a range <i>{x: x &isin; {@code values}}</i>.
     *
     * @param values the only values contained by the range
     * @return created range
     *
     * @apiNote this takes ownership over {@code values}
     */
    static @NotNull ${className} only(final ${primitiveType} @Own @NonNull ... values) {
        Arrays.sort(values);

        return value -> Arrays.binarySearch(values, value) >= 0;
    }

    /**
     * Creates a range <i>{x: x &isin; {@code values}}</i>.
     *
     * @param values the only values contained by the range
     * @return created range
     *
     * @apiNote this takes ownership over {@code values}
     */
    static @NotNull ${className} only(final @Ref @NonNull Collection${'<@NotNull ${wrapperType}>'} values) {
        return values::contains;
    }

    /**
     * Creates a range <i>{x: x &isin; {@code values}}</i>.
     *
     * @param values the only values contained by the range
     * @return created range
     *
     * @apiNote this copies {@code values} not preserving order
     */
    static @NotNull ${className} onlyCopy(final ${primitiveType} @Ref @NonNull ... values) {
        return only(Arrays.copyOf(values, values.length));
    }

    /**
     * Creates a range <i>{x: x &isin; {@code values}}</i>.
     *
     * @param values the only values contained by the range
     * @return created range
     *
     * @apiNote this copies {@code values} not preserving order
     */
    static @NotNull ${className} onlyCopy(final @Ref @NonNull Collection${'<@NotNull ${wrapperType}>'} values) {
        return only(new HashSet<>(values));
    }

    /**
     * Creates a range <i>{x: x &isin; {@code values}}</i>.
     *
     * @param values the only values contained by the range
     * @return created range
     *
     * @apiNote this copies {@code values} preserving order
     */
    static @NotNull ${className} onlyCopyOrdered(final @Ref @NonNull Collection${'<@NotNull ${wrapperType}>'} values) {
        return only(new ArrayList<>(values));
    }

    /**
     * Creates a range <i>&not;{{@code value}}</i>.
     *
     * @param value the only value not contained by the range
     * @return created range
     */
    static @NotNull ${className} except(final ${primitiveType} value) {
        return tested -> tested != value;
    }

    /**
     * Creates a range <i>{x: x &notin; {@code values}}</i>.
     *
     * @param values the only values not contained by the range
     * @return created range
     *
     * @apiNote this takes ownership over {@code values}
     */
    static @NotNull ${className} except(final ${primitiveType} @Own @NonNull ... values) {
        Arrays.sort(values);

        return value -> Arrays.binarySearch(values, value) < 0;
    }

    /**
     * Creates a range <i>{x: x &notin; {@code values}}</i>.
     *
     * @param values the only values not contained by the range
     * @return created range
     *
     * @apiNote this takes ownership over {@code values}
     */
    static @NotNull ${className} except(final @Ref @NonNull Collection${'<@NotNull ${wrapperType}>'} values) {
        return value -> !values.contains(value);
    }

    /**
     * Creates a range <i>{x: x &notin; {@code values}}</i>.
     *
     * @param values the only values not contained by the range
     * @return created range
     *
     * @apiNote this copies {@code values} not preserving order
     */
    static @NotNull ${className} exceptCopy(final ${primitiveType} @Own @NonNull ... values) {
        return except(Arrays.copyOf(values, values.length));
    }

    /**
     * Creates a range <i>{x: x &notin; {@code values}}</i>.
     *
     * @param values the only values not contained by the range
     * @return created range
     *
     * @apiNote this copies {@code values} not preserving order
     */
    static @NotNull ${className} exceptCopy(final @Ref @NonNull Collection${'<@NotNull ${wrapperType}>'} values) {
        return except(new HashSet<>(values));
    }

    /**
     * Creates a range <i>{x: x &notin; {@code values}}</i>.
     *
     * @param values the only values not contained by the range
     * @return created range
     *
     * @apiNote this copies {@code values} preserving order
     */
    static @NotNull ${className} exceptCopyOrdered(final @Ref @NonNull Collection${'<@NotNull ${wrapperType}>'} values) {
        return except(new ArrayList<>(values));
    }

    /* ************************************************* Intervals ************************************************* */

    /**
     * Creates a range <i>({@code lowerBound}; +&infin;)</i>.
     *
     * @param lowerBound lower exclusive bound of the range
     * @return created range
     */
    static @NotNull ${className} greater(final ${primitiveType} lowerBound) {
        return value -> lowerBound < value;
    }

    /**
     * Creates a range <i>[{@code lowerBound}; +&infin;)</i>.
     *
     * @param lowerBound lower inclusive bound of the range
     * @return created range
     */
    static @NotNull ${className} greaterOrEqual(final ${primitiveType} lowerBound) {
        return value -> lowerBound <= value;
    }

    /**
     * Creates a range <i>(&infin;; {@code upperBound})</i>.
     *
     * @param upperBound upper inclusive bound of the range
     * @return created range
     */
    static @NotNull ${className} less(final ${primitiveType} upperBound) {
        return value -> upperBound > value;
    }

    /**
     * Creates a range <i>(&infin;; {@code upperBound}]</i>.
     *
     * @param upperBound upper exclusive bound of the range
     * @return created range
     */
    static @NotNull ${className} lessOrEqual(final ${primitiveType} upperBound) {
        return value -> upperBound >= value;
    }

    /**
     * Creates a range <i>({@code lowerBound}; {@code upperBound})</i>.
     *
     * @param lowerBound lower exclusive bound of the range
     * @param upperBound upper exclusive bound of the range
     * @return created range
     */
    static @NotNull ${className} between(final ${primitiveType} lowerBound, final ${primitiveType} upperBound) {
        return value -> lowerBound < value && upperBound > value;
    }

    /**
     * Creates a range <i>[{@code lowerBound}; {@code upperBound}]</i>.
     *
     * @param lowerBound lower inclusive bound of the range
     * @param upperBound upper inclusive bound of the range
     * @return created range
     */
    static @NotNull ${className} betweenOrEqual(final ${primitiveType} lowerBound, final ${primitiveType} upperBound) {
        return value -> lowerBound <= value && upperBound >= value;
    }

    /**
     * Creates a range <i>({@code lowerBound}; {@code upperBound}]</i>.
     *
     * @param lowerBound lower exclusive bound of the range
     * @param upperBound upper inclusive bound of the range
     * @return created range
     */
    static @NotNull ${className} fromExclusiveTo(final ${primitiveType} lowerBound, final ${primitiveType} upperBound) {
        return value -> lowerBound < value && upperBound >= value;
    }

    /**
     * Creates a range <i>[{@code lowerBound}; {@code upperBound})</i>.
     *
     * @param lowerBound lower inclusive bound of the range
     * @param upperBound upper exclusive bound of the range
     * @return created range
     */
    static @NotNull ${className} fromToExclusive(final ${primitiveType} lowerBound, final ${primitiveType} upperBound) {
        return value -> lowerBound <= value && upperBound > value;
    }

    /* ************************************************** Joiners ************************************************** */

    /**
     * Creates a range <i>{x: &exist; range &isin; ranges: x &isin; range}</i>.
     *
     * @param ranges matched disjunctive ranges
     * @return created range
     *
     * @apiNote this takes ownership over {@code ranges}
     */
    static @NotNull ${className} anyOf(final @Ref @NotNull ${className} @NonNull ... ranges) {
        return value -> {
            for (val range : ranges) if (range.testAs${capitalizedPrimitiveType}(value)) return true;

            return false;
        };
    }

    /**
     * Creates a range <i>{x: &exist; range &isin; ranges: x &isin; range}</i>.
     *
     * @param ranges matched disjunctive ranges
     * @return created range
     *
     * @apiNote this takes ownership over {@code ranges}
     */
    static @NotNull ${className} anyOf(final @Ref @NonNull Iterable${'<@NotNull ${className}>'} ranges) {
        return value -> {
            for (val range : ranges) if (range.testAs${capitalizedPrimitiveType}(value)) return true;

            return false;
        };
    }

    /**
     * Creates a range <i>{x: &exist; range &isin; ranges: x &isin; range}</i>.
     *
     * @param ranges matched disjunctive ranges
     * @return created range
     *
     * @apiNote this copies {@code ranges} preserving order
     */
    static @NotNull ${className} anyOfCopy(final @Ref @NotNull ${className} @NonNull ... ranges) {
        return anyOf(Arrays.copyOf(ranges, ranges.length));
    }

    /**
     * Creates a range <i>{x: &exist; range &isin; ranges: x &isin; range}</i>.
     *
     * @param ranges matched disjunctive ranges
     * @return created range
     *
     * @apiNote this copies {@code ranges} not preserving order
     */
    static @NotNull ${className} anyOfCopy(final @Ref @NonNull Iterable${'<@NotNull ${className}>'} ranges) {
        return anyOf(Iterables.toSet(ranges));
    }

    /**
     * Creates a range <i>{x: &exist; range &isin; ranges: x &isin; range}</i>.
     *
     * @param ranges matched disjunctive ranges
     * @return created range
     *
     * @apiNote this copies {@code ranges} preserving order
     */
    static @NotNull ${className} anyOfCopyOrdered(final @Ref @NonNull Iterable${'<@NotNull ${className}>'} ranges) {
        return anyOf(Iterables.toList(ranges));
    }

    /**
     * Creates a range <i>{x: &forall; range &isin; ranges, x &isin; range}</i>.
     *
     * @param ranges matched conjunctive ranges
     * @return created range
     *
     * @apiNote this takes ownership over {@code ranges}
     */
    static @NotNull ${className} allOf(final @Ref @NotNull ${className} @NonNull ... ranges) {
        return value -> {
            for (val range : ranges) if (!range.testAs${capitalizedPrimitiveType}(value)) return false;

            return true;
        };
    }

    /**
     * Creates a range <i>{x: &forall; range &isin; ranges, x &isin; range}</i>.
     *
     * @param ranges matched conjunctive ranges
     * @return created range
     *
     * @apiNote this takes ownership over {@code ranges}
     */
    static @NotNull ${className} allOf(final @Ref @NonNull Iterable${'<@NotNull ${className}>'} ranges) {
        return value -> {
            for (val range : ranges) if (!range.testAs${capitalizedPrimitiveType}(value)) return false;

            return true;
        };
    }

    /**
     * Creates a range <i>{x: &forall; range &isin; ranges, x &isin; range}</i>.
     *
     * @param ranges matched conjunctive ranges
     * @return created range
     *
     * @apiNote this copies {@code ranges} preserving order
     */
    static @NotNull ${className} allOfCopy(final @Ref @NotNull ${className} @NonNull ... ranges) {
        return allOf(Arrays.copyOf(ranges, ranges.length));
    }

    /**
     * Creates a range <i>{x: &forall; range &isin; ranges, x &isin; range}</i>.
     *
     * @param ranges matched conjunctive ranges
     * @return created range
     *
     * @apiNote this copies {@code ranges} not preserving order
     */
    static @NotNull ${className} allOfCopy(final @Ref @NonNull Iterable${'<@NotNull ${className}>'} ranges) {
        return allOf(Iterables.toSet(ranges));
    }

    /**
     * Creates a range <i>{x: &forall; range &isin; ranges, x &isin; range}</i>.
     *
     * @param ranges matched conjunctive ranges
     * @return created range
     *
     * @apiNote this copies {@code ranges} preserving order
     */
    static @NotNull ${className} allOfCopyOrdered(final @Ref @NonNull Iterable${'<@NotNull ${className}>'} ranges) {
        return allOf(Iterables.toList(ranges));
    }

    /**
     * Creates a range <i>{x: &forall; range &isin; ranges, x &notin; range}</i>.
     *
     * @param ranges not matched conjunctive ranges
     * @return created range
     *
     * @apiNote this takes ownership over {@code ranges}
     */
    static @NotNull ${className} noneOf(final @Ref @NotNull ${className} @NonNull ... ranges) {
        return value -> {
            for (val range : ranges) if (range.testAs${capitalizedPrimitiveType}(value)) return false;

            return true;
        };
    }

    /**
     * Creates a range <i>{x: &forall; range &isin; ranges, x &notin; range}</i>.
     *
     * @param ranges not matched conjunctive ranges
     * @return created range
     *
     * @apiNote this takes ownership over {@code ranges}
     */
    static @NotNull ${className} noneOf(final @Ref @NonNull Iterable${'<@NotNull ${className}>'} ranges) {
        return value -> {
            for (val range : ranges) if (range.testAs${capitalizedPrimitiveType}(value)) return false;

            return true;
        };
    }

    /**
     * Creates a range <i>{x: &forall; range &isin; ranges, x &notin; range}</i>.
     *
     * @param ranges not matched conjunctive ranges
     * @return created range
     *
     * @apiNote this copies {@code ranges} preserving order
     */
    static @NotNull ${className} noneOfCopy(final @Ref @NotNull ${className} @NonNull ... ranges) {
        return noneOf(Arrays.copyOf(ranges, ranges.length));
    }

    /**
     * Creates a range <i>{x: &forall; range &isin; ranges, x &notin; range}</i>.
     *
     * @param ranges not matched conjunctive ranges
     * @return created range
     *
     * @apiNote this copies {@code ranges} not preserving order
     */
    static @NotNull ${className} noneOfCopy(final @Ref @NonNull Iterable${'<@NotNull ${className}>'} ranges) {
        return noneOf(Iterables.toSet(ranges));
    }

    /**
     * Creates a range <i>{x: &forall; range &isin; ranges, x &notin; range}</i>.
     *
     * @param ranges not matched conjunctive ranges
     * @return created range
     *
     * @apiNote this copies {@code ranges} preserving order
     */
    static @NotNull ${className} noneOfCopyOrdered(final @Ref @NonNull Iterable${'<@NotNull ${className}>'} ranges) {
        return noneOf(Iterables.toList(ranges));
    }
}
