package foo.bar.baz;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.processing.Generated;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Generated(
        value = "ru.progrm_jarvis.padla.annotation.processor.EnumHelpersAnnotationProcessor",
        date = "2020-07-07T23:42+0000"
)
public final class Simples {

    private static final @NotNull Simple @NotNull [] AS_ARRAY
            = Simple.values();

    private static final @NotNull @Unmodifiable Set<@NotNull Simple> AS_SET
            = Collections.unmodifiableSet(EnumSet.allOf(Simple.class));

    private static final @NotNull @Unmodifiable List<@NotNull Simple> AS_LIST
            = List.of(AS_ARRAY);

    private static final @NotNull @Unmodifiable Map<@NotNull String, @NotNull Simple> AS_MAP
            = Map.of(
            "SOME_A", Simple.SOME_A,
            "OTHER_B", Simple.OTHER_B,
            "AT_LAST_ITS_C", Simple.AT_LAST_ITS_C
    );

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

    public static @NotNull @Unmodifiable Map<@NotNull String, @NotNull Simple> map() {
        return AS_MAP;
    }

    public static @NotNull Stream<@NotNull Simple> stream() {
        return Stream.of(AS_ARRAY);
    }
}
