package ru.progrm_jarvis.javacommons.range;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.annotation.ownership.Own;
import ru.progrm_jarvis.javacommons.annotation.ownership.Ref;

import java.util.*;
import java.util.function.Predicate;

/**
 * Range limiting some value.
 *
 * @param <T> type of value limited by this range.
 */
@FunctionalInterface
public interface Range<T> extends Predicate<T> {

    /**
     * Checks if the given value is in range.
     *
     * @param value value checked for inclusion in this range
     * @return {@code true} if the range includes the given value and {@code false} otherwise
     */
    default boolean includes(final T value) {
        return test(value);
    }

    /* ************************************************* Modifiers ************************************************* */

    @Override
    default @NotNull Range<T> negate() {
        return value -> !includes(value);
    }

    /* ************************************************ Combinators ************************************************ */

    @Override
    default @NotNull Range<T> and(final @NonNull Predicate<? super T> other) {
        return value -> includes(value) && other.test(value);
    }

    @Override
    default @NotNull Range<T> or(final @NonNull Predicate<? super T> other) {
        return value -> includes(value) || other.test(value);
    }

    /* ************************************************* Factories ************************************************* */

    /**
     * Creates a range <i>[-&infin; ; +&infin;]</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     */
    static <T> @NotNull Range<T> any() {
        return value -> true;
    }

    /**
     * Creates a range <i>&empty;</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     */
    static <T> @NotNull Range<T> none() {
        return value -> false;
    }

    /**
     * Creates a range <i>{{@code null}}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     */
    static <T> @NotNull Range<T> onlyNull() {
        return Objects::isNull;
    }

    /**
     * Creates a range <i>{{@code value}}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     */
    static <T> @NotNull Range<T> only(final @Ref T value) {
        return value == null ? onlyNull() : value::equals;
    }

    /**
     * Creates a range <i>{x: x &isin; {@code values}}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this takes ownership over {@code values}
     */
    @SafeVarargs
    static <T> @NotNull Range<T> only(final @Own T @Own @NonNull ... values) {
        Arrays.sort(values);

        return value -> Arrays.binarySearch(values, value) >= 0;
    }

    /**
     * Creates a range <i>{x: x &isin; {@code values}}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this takes ownership over {@code values}
     */
    static <T> @NotNull Range<T> only(final @Ref @NonNull Collection<T> values) {
        return values::contains;
    }

    /**
     * Creates a range <i>{x: x &isin; {@code values}}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this copies {@code values} not preserving order
     */
    @SafeVarargs
    static <T> @NotNull Range<T> onlyCopy(final @Own T @Ref @NonNull ... values) {
        return only(Arrays.copyOf(values, values.length));
    }

    /**
     * Creates a range <i>{x: x &isin; {@code values}}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this copies {@code values} not preserving order
     */
    static <T> @NotNull Range<T> onlyCopy(final @Ref @NonNull Collection<T> values) {
        return only(new HashSet<>(values));
    }

    /**
     * Creates a range <i>{x: x &isin; {@code values}}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this copies {@code values} preserving order
     */
    static <T> @NotNull Range<T> onlyCopyOrdered(final @Ref @NonNull Collection<T> values) {
        return only(new ArrayList<>(values));
    }

    /**
     * Creates a range <i>&not;{{@code null}}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     */
    static <T> @NotNull Range<T> exceptNull() {
        return Objects::nonNull;
    }

    /**
     * Creates a range <i>&not;{{@code value}}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     */
    static <T> @NotNull Range<T> except(final @Ref T value) {
        return value == null ? exceptNull() : tested -> !value.equals(tested);
    }

    /**
     * Creates a range <i>{x: x &notin; {@code values}}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this takes ownership over {@code values}
     */
    @SafeVarargs
    static <T> @NotNull Range<T> except(final @Own T @Own @NonNull ... values) {
        Arrays.sort(values);

        return value -> Arrays.binarySearch(values, value) < 0;
    }

    /**
     * Creates a range <i>{x: x &notin; {@code values}}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this takes ownership over {@code values}
     */
    static <T> @NotNull Range<T> except(final @Ref @NonNull Collection<T> values) {
        return value -> !values.contains(value);
    }

    /**
     * Creates a range <i>{x: x &notin; {@code values}}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this copies {@code values} not preserving order
     */
    @SafeVarargs
    static <T> @NotNull Range<T> exceptCopy(final @Own T @Own @NonNull ... values) {
        return except(Arrays.copyOf(values, values.length));
    }

    /**
     * Creates a range <i>{x: x &notin; {@code values}}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this copies {@code values} not preserving order
     */
    static <T> @NotNull Range<T> exceptCopy(final @Ref @NonNull Collection<T> values) {
        return except(new HashSet<>(values));
    }

    /**
     * Creates a range <i>{x: x &notin; {@code values}}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this copies {@code values} preserving order
     */
    static <T> @NotNull Range<T> exceptCopyOrdered(final @Ref @NonNull Collection<T> values) {
        return except(new ArrayList<>(values));
    }

    /* ************************************************* Intervals ************************************************* */

    /**
     * Creates a range <i>({@code lowerBound}; +&infin;)</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     */
    static <T extends Comparable<T>> @NotNull Range<T> greater(final @Ref @NonNull T lowerBound) {
        return value -> lowerBound.compareTo(value) < 0;
    }

    /**
     * Creates a range <i>[{@code lowerBound}; +&infin;)</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     */
    static <T extends Comparable<T>> @NotNull Range<T> greaterOrEqual(final @Ref @NonNull T lowerBound) {
        return value -> lowerBound.compareTo(value) <= 0;
    }

    /**
     * Creates a range <i>(&infin;; {@code upperBound})</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     */
    static <T extends Comparable<T>> @NotNull Range<T> less(final @Ref @NonNull T upperBound) {
        return value -> upperBound.compareTo(value) > 0;
    }

    /**
     * Creates a range <i>(&infin;; {@code upperBound}]</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     */
    static <T extends Comparable<T>> @NotNull Range<T> lessOrEqual(final @Ref @NonNull T upperBound) {
        return value -> upperBound.compareTo(value) >= 0;
    }

    /**
     * Creates a range <i>({@code lowerBound}; {@code upperBound})</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     */
    static <T extends Comparable<T>> @NotNull Range<T> between(final @Ref @NonNull T lowerBound,
                                                               final @Ref @NonNull T upperBound) {
        return value -> lowerBound.compareTo(value) < 0 && upperBound.compareTo(value) > 0;
    }

    /**
     * Creates a range <i>[{@code lowerBound}; {@code upperBound}]</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     */
    static <T extends Comparable<T>> @NotNull Range<T> betweenOrEqual(final @Ref @NonNull T lowerBound,
                                                                      final @Ref @NonNull T upperBound) {
        return value -> lowerBound.compareTo(value) <= 0 && upperBound.compareTo(value) >= 0;
    }

    /**
     * Creates a range <i>({@code lowerBound}; {@code upperBound}]</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     */
    static <T extends Comparable<T>> @NotNull Range<T> fromExclusiveTo(final @Ref @NonNull T lowerBound,
                                                                       final @Ref @NonNull T upperBound) {
        return value -> lowerBound.compareTo(value) < 0 && upperBound.compareTo(value) >= 0;
    }

    /**
     * Creates a range <i>[{@code lowerBound}; {@code upperBound})</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     */
    static <T extends Comparable<T>> @NotNull Range<T> fromToExclusive(final @Ref @NonNull T lowerBound,
                                                                       final @Ref @NonNull T upperBound) {
        return value -> lowerBound.compareTo(value) <= 0 && upperBound.compareTo(value) > 0;
    }

    /* ************************************************** Joiners ************************************************** */

    /**
     * Creates a range <i>{x: &exist; range &isin; ranges: x &isin; range}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this takes ownership over {@code ranges}
     */
    @SafeVarargs
    static <T> @NotNull Range<T> anyOf(final @Ref @NotNull Range<T> @NonNull ... ranges) {
        return value -> {
            for (val range : ranges) if (range.includes(value)) return true;

            return false;
        };
    }

    /**
     * Creates a range <i>{x: &exist; range &isin; ranges: x &isin; range}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this takes ownership over {@code ranges}
     */
    static <T> @NotNull Range<T> anyOf(final @Ref @NonNull Iterable<@NotNull Range<T>> ranges) {
        return value -> {
            for (val range : ranges) if (range.includes(value)) return true;

            return false;
        };
    }

    /**
     * Creates a range <i>{x: &exist; range &isin; ranges: x &isin; range}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this copies {@code ranges} preserving order
     */
    @SafeVarargs
    static <T> @NotNull Range<T> anyOfCopy(final @Ref @NotNull Range<T> @NonNull ... ranges) {
        return anyOf(Arrays.copyOf(ranges, ranges.length));
    }

    /**
     * Creates a range <i>{x: &exist; range &isin; ranges: x &isin; range}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this copies {@code ranges} not preserving order
     */
    static <T> @NotNull Range<T> anyOfCopy(final @Ref @NonNull Iterable<@NotNull Range<T>> ranges) {
        return anyOf(Sets.newHashSet(ranges));
    }

    /**
     * Creates a range <i>{x: &exist; range &isin; ranges: x &isin; range}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this copies {@code ranges} preserving order
     */
    static <T> @NotNull Range<T> anyOfCopyOrdered(final @Ref @NonNull Iterable<@NotNull Range<T>> ranges) {
        return anyOf(Lists.newArrayList(ranges));
    }

    /**
     * Creates a range <i>{x: &forall; range &isin; ranges, x &isin; range}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this takes ownership over {@code ranges}
     */
    @SafeVarargs
    static <T> @NotNull Range<T> allOf(final @Ref @NotNull Range<T> @NonNull ... ranges) {
        return value -> {
            for (val range : ranges) if (!range.includes(value)) return false;

            return true;
        };
    }

    /**
     * Creates a range <i>{x: &forall; range &isin; ranges, x &isin; range}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this takes ownership over {@code ranges}
     */
    static <T> @NotNull Range<T> allOf(final @Ref @NonNull Iterable<@NotNull Range<T>> ranges) {
        return value -> {
            for (val range : ranges) if (!range.includes(value)) return false;

            return true;
        };
    }

    /**
     * Creates a range <i>{x: &forall; range &isin; ranges, x &isin; range}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this copies {@code ranges} preserving order
     */
    @SafeVarargs
    static <T> @NotNull Range<T> allOfCopy(final @Ref @NotNull Range<T> @NonNull ... ranges) {
        return allOf(Arrays.copyOf(ranges, ranges.length));
    }

    /**
     * Creates a range <i>{x: &forall; range &isin; ranges, x &isin; range}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this copies {@code ranges} not preserving order
     */
    static <T> @NotNull Range<T> allOfCopy(final @Ref @NonNull Iterable<@NotNull Range<T>> ranges) {
        return allOf(Sets.newHashSet(ranges));
    }

    /**
     * Creates a range <i>{x: &forall; range &isin; ranges, x &isin; range}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this copies {@code ranges} preserving order
     */
    static <T> @NotNull Range<T> allOfCopyOrdered(final @Ref @NonNull Iterable<@NotNull Range<T>> ranges) {
        return allOf(Lists.newArrayList(ranges));
    }

    /**
     * Creates a range <i>{x: &forall; range &isin; ranges, x &notin; range}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this takes ownership over {@code ranges}
     */
    @SafeVarargs
    static <T> @NotNull Range<T> noneOf(final @Ref @NotNull Range<T> @NonNull ... ranges) {
        return value -> {
            for (val range : ranges) if (range.includes(value)) return false;

            return true;
        };
    }

    /**
     * Creates a range <i>{x: &forall; range &isin; ranges, x &notin; range}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this takes ownership over {@code ranges}
     */
    static <T> @NotNull Range<T> noneOf(final @Ref @NonNull Iterable<@NotNull Range<T>> ranges) {
        return value -> {
            for (val range : ranges) if (range.includes(value)) return false;

            return true;
        };
    }

    /**
     * Creates a range <i>{x: &forall; range &isin; ranges, x &notin; range}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this copies {@code ranges} preserving order
     */
    @SafeVarargs
    static <T> @NotNull Range<T> noneOfCopy(final @Ref @NotNull Range<T> @NonNull ... ranges) {
        return noneOf(Arrays.copyOf(ranges, ranges.length));
    }

    /**
     * Creates a range <i>{x: &forall; range &isin; ranges, x &notin; range}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this copies {@code ranges} not preserving order
     */
    static <T> @NotNull Range<T> noneOfCopy(final @Ref @NonNull Iterable<@NotNull Range<T>> ranges) {
        return noneOf(Sets.newHashSet(ranges));
    }

    /**
     * Creates a range <i>{x: &forall; range &isin; ranges, x &notin; range}</i>.
     *
     * @param <T> type of range's elements
     * @return created range
     *
     * @apiNote this copies {@code ranges} preserving order
     */
    static <T> @NotNull Range<T> noneOfCopyOrdered(final @Ref @NonNull Iterable<@NotNull Range<T>> ranges) {
        return noneOf(Lists.newArrayList(ranges));
    }
}
