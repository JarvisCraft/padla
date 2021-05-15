package ru.progrm_jarvis.javacommons.util.stream;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.javacommons.util.TypeHints;
import ru.progrm_jarvis.javacommons.util.TypeHints.TypeHint;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * <p>{@link Collector Collectors} for use with enums.</p>
 *
 * <p>There also are some overloads with automatic type inference.
 * These methods delegate their logic to the corresponding methods with explicit type parameter
 * by inferring enum types via {@link TypeHint type hints}.
 * This approach leads to insignificant overhead due to empty-array allocation cost
 * thus it is recommended to provide types explicitly in critical sections.</p>
 */
@UtilityClass
public class EnumCollectors {

    /**
     * Returns a {@link Collector} that accumulates the input elements into a new enum-{@link Map}.
     *
     * @param <E> type of the enum
     * @param type type object of the enum
     * @param keyMapper mapping function used to convert the elements into enum-keys
     * @param valueMapper mapping function used to convert the elements into values
     * @param merger function used to handle duplicate values
     * @return a collector collecting al its elements into an enum-map
     */
    public <T, E extends Enum<E>, V> @NotNull Collector<T, ?, @NotNull Map<E, V>> toEnumMap(
            final @NonNull Class<E> type,
            final @NonNull Function<T, E> keyMapper,
            final @NonNull Function<T, V> valueMapper,
            final @NonNull BinaryOperator<V> merger
    ) {
        return Collectors.toMap(keyMapper, valueMapper, merger, () -> new EnumMap<>(type));
    }

    /**
     * Returns a {@link Collector} that accumulates the input elements into a new enum-{@link Map}.
     *
     * @param keyMapper mapping function used to convert the elements into enum-keys
     * @param valueMapper mapping function used to convert the elements into values
     * @param merger function used to handle duplicate values
     * @param typeHint array used for enum-type discovery
     * @param <E> type of the enum
     * @return a collector collecting al its elements into an enum-map
     */
    @SafeVarargs
    public <T, E extends Enum<E>, V> @NotNull Collector<T, ?, @NotNull Map<E, V>> toEnumMap(
            final @NonNull Function<T, E> keyMapper,
            final @NonNull Function<T, V> valueMapper,
            final @NonNull BinaryOperator<V> merger,
            @TypeHint final @Nullable E @NonNull ... typeHint
    ) {
        return toEnumMap(TypeHints.resolve(typeHint), keyMapper, valueMapper, merger);
    }

    /**
     * Returns a {@link Collector} that accumulates the input elements into a new enum-{@link Map}.
     *
     * @param <E> type of the enum
     * @param type type object of the enum
     * @param keyMapper mapping function used to convert the elements into enum-keys
     * @param valueMapper mapping function used to convert the elements into values
     * @return a collector collecting al its elements into an enum-map
     */
    public <T, E extends Enum<E>, V> @NotNull Collector<T, ?, @NotNull Map<E, V>> toEnumMap(
            final @NonNull Class<E> type,
            final @NonNull Function<T, E> keyMapper,
            final @NonNull Function<T, V> valueMapper
    ) {
        return toEnumMap(type, keyMapper, valueMapper, throwingMerger());
    }

    /**
     * Returns a {@link Collector} that accumulates the input elements into a new enum-{@link Map}.
     *
     * @param <E> type of the enum
     * @param type type object of the enum
     * @param valueMapper mapping function used to convert the elements into values
     * @return a collector collecting al its elements into an enum-map
     */
    public <E extends Enum<E>, V> @NotNull Collector<E, ?, @NotNull Map<E, V>> toEnumMap(
            final @NonNull Class<E> type,
            final @NonNull Function<E, V> valueMapper
    ) {
        return toEnumMap(type, Function.identity(), valueMapper, throwingMerger());
    }

    /**
     * Returns a {@link Collector} that accumulates the input elements into a new enum-{@link Map}.
     *
     * @param <E> type of the enum
     * @param type type object of the enum
     * @param valueMapper mapping function used to convert the elements into values
     * @param merger function used to handle duplicate values
     * @return a collector collecting al its elements into an enum-map
     */
    public <E extends Enum<E>, V> @NotNull Collector<E, ?, @NotNull Map<E, V>> toEnumMap(
            final @NonNull Class<E> type,
            final @NonNull Function<E, V> valueMapper,
            final @NonNull BinaryOperator<V> merger
    ) {
        return toEnumMap(type, Function.identity(), valueMapper, merger);
    }

    /**
     * Returns a {@link Collector} that accumulates the input elements into a new enum-{@link Map}.
     *
     * @param valueMapper mapping function used to convert the elements into values
     * @param merger function used to handle duplicate values
     * @param typeHint array used for enum-type discovery
     * @param <E> type of the enum
     * @return a collector collecting al its elements into an enum-map
     */
    @SafeVarargs
    public <E extends Enum<E>, V> @NotNull Collector<E, ?, @NotNull Map<E, V>> toEnumMap(
            final @NonNull Function<E, V> valueMapper,
            final @NonNull BinaryOperator<V> merger,
            @TypeHint final @Nullable E @NonNull ... typeHint
    ) {
        return toEnumMap(TypeHints.resolve(typeHint), valueMapper, merger);
    }

    /**
     * Returns a {@link Collector} that accumulates the input elements into a new enum-{@link Set}.
     *
     * @param type type object of the enum
     * @param <E> type of the enum
     * @return a collector collecting al its elements into an enum-set
     */
    public <E extends Enum<E>> Collector<E, ?, Set<E>> toEnumSet(
            final @NonNull Class<E> type
    ) {
        return Collectors.toCollection(() -> EnumSet.noneOf(type));
    }

    /**
     * Returns a {@link Collector} that accumulates the input elements into a new enum-{@link Set}.
     *
     * @param typeHint array used for enum-type discovery
     * @param <E> type of the enum
     * @return a collector collecting al its elements into an enum-set
     */
    @SafeVarargs
    public <E extends Enum<E>> Collector<E, ?, Set<E>> toEnumSet(
            @TypeHint final @Nullable E @NonNull ... typeHint
    ) {
        return toEnumSet(TypeHints.resolve(typeHint));
    }

    /**
     * Returns a default merger for use with <i>to-map</i> collectors
     * which throws {@link IllegalStateException} on duplicate values.
     *
     * @param <V> type of the mapped value
     * @return default throwing merger
     */
    private static <V> @NotNull BinaryOperator<V> throwingMerger() {
        return (left, right) -> {
            throw new IllegalStateException("Duplicate elements " + left + " and " + right);
        };
    }
}
