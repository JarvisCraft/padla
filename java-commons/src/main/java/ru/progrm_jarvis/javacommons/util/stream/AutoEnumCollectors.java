package ru.progrm_jarvis.javacommons.util.stream;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.javacommons.util.TypeHints;

import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * {@link Collector Collectors} for use with enums with automatic type inference.
 * This delegates its logic to the corresponding methods of {@link EnumCollectors}
 * but infers enum types via {@link TypeHints#resolve(Object[]) type hints}
 * this leads to insignificant overhead due to empty-array allocation cost
 * thus it is recommended to provide types explicitly in critical sections.
 */
@UtilityClass
public class AutoEnumCollectors {

    /**
     * Returns a {@link Collector} that accumulates the input elements into a new enum-{@link Map}.
     *
     * @param keyMapper mapping function used to convert the elements into enum-keys
     * @param valueMapper mapping function used to convert the elements into values
     * @param merger function used to handle duplicate values
     * @param typeHint array used for enum-type discovery
     * @param <T> type of the input elements
     * @param <E> type of the enum
     * @param <V> type of map values
     * @return a collector collecting al its elements into an enum-map
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public <T, E extends Enum<E>, V> @NotNull Collector<T, ?, @NotNull Map<E, V>> toEnumMap(
            final @NonNull Function<T, E> keyMapper,
            final @NonNull Function<T, V> valueMapper,
            final @NonNull BinaryOperator<V> merger,
            final @Nullable E @NonNull ... typeHint
    ) {
        return EnumCollectors.toEnumMap(TypeHints.resolve(typeHint), keyMapper, valueMapper, merger);
    }

    /**
     * Returns a {@link Collector} that accumulates the input elements into a new enum-{@link Map}.
     *
     * @param valueMapper mapping function used to convert the elements into values
     * @param merger function used to handle duplicate values
     * @param typeHint array used for enum-type discovery
     * @param <E> type of the enum
     * @param <V> type of map values
     * @return a collector collecting al its elements into an enum-map
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public <E extends Enum<E>, V> @NotNull Collector<E, ?, @NotNull Map<E, V>> toEnumMap(
            final @NonNull Function<E, V> valueMapper,
            final @NonNull BinaryOperator<V> merger,
            final @Nullable E @NonNull ... typeHint
    ) {
        return EnumCollectors.toEnumMap(TypeHints.resolve(typeHint), valueMapper, merger);
    }

    /**
     * Returns a {@link Collector} that accumulates the input elements into a new enum-{@link Set}.
     *
     * @param typeHint array used for enum-type discovery
     * @param <E> type of the enum
     * @return a collector collecting al its elements into an enum-set
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public <E extends Enum<E>> Collector<E, ?, Set<E>> toEnumSet(final @Nullable E @NonNull ... typeHint) {
        return EnumCollectors.toEnumSet(TypeHints.resolve(typeHint));
    }
}
