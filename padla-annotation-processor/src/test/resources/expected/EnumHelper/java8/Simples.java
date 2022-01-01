package foo.bar.baz;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class Simples {

    private static final @NotNull Simple @NotNull [] AS_ARRAY
            = Simple.values();

    private static final @NotNull @Unmodifiable Set<@NotNull Simple> AS_SET
            = Collections.unmodifiableSet(EnumSet.allOf(Simple.class));

    private static final @NotNull @Unmodifiable List<@NotNull Simple> AS_LIST
            = Collections.unmodifiableList(Arrays.asList(AS_ARRAY));

    private Simples() {
        throw new AssertionError(
                "Simples is an utility class and thus cannot be instantiated"
        );
    }

    public static @NotNull @Unmodifiable Set<@NotNull Simple> set() {
        return AS_SET;
    }

    public static @NotNull @Unmodifiable List<@NotNull Simple> list() {
        return AS_LIST;
    }

    public static @NotNull Stream<@NotNull Simple> stream() {
        return Stream.of(AS_ARRAY);
    }
}
