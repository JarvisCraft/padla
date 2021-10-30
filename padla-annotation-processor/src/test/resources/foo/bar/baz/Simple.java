package foo.bar.baz;

import org.checkerframework.checker.nullness.qual.NonNull;
import ru.progrm_jarvis.padla.annotation.EnumHelper;

import javax.annotation.Nullable;

@EnumHelper(
        annotations = @EnumHelper.Annotations(
                nullable = Nullable.class,
                notNull = NonNull.class,
                unmodifiable = EnumHelper.Annotations.None.class
        )
)
public enum Simple {
    SOME_A, OTHER_B, AT_LAST_ITS_C;
}
