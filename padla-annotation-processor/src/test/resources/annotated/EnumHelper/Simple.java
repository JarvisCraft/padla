package foo.bar.baz;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.padla.annotation.EnumHelper;

@EnumHelper(
        /*annotations = @EnumHelper.Annotations(
                nullable = Nullable.class,
                notNull = NotNull.class,
                unmodifiable = EnumHelper.Annotations.None.class
        )*/
)
public enum Simple {
    SOME_A, OTHER_B, AT_LAST_ITS_C
}
