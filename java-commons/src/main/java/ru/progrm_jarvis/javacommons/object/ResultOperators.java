package ru.progrm_jarvis.javacommons.object;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.JavaCommons;

/**
 * Operator extensions for Result.
 */
@UtilityClass
public class ResultOperators {

    /**
     * <p>Continues the control-flow by {@link Result#unwrap() unwrapping}
     * the {@link Result#isSuccess() successful value},
     * {@link Result#isError() otherwise} returning the result object itself.</p>
     * <p>The call looking like</p>
     * <pre>{@code
     * T value = foo()._try();
     * }</pre>
     * <p>Where {@code foo()} returns {@code Result<T, E>}</p>
     * <p>Will get transformed into something semantically identical to</p>
     * <pre>{@code
     * T value;
     * {
     *     final Result<T, E> $result = foo();
     *     if ($result.isError()) return Result.error($result.unwrapError());
     *     value = $result.unwrap();
     * }
     * }</pre>
     *
     * @return the successful result
     */
    public @NotNull <T, E> T _try(@SuppressWarnings("unused") final @NotNull Result<? extends T, ? extends E> result) {
        return JavaCommons.requireBytecodePatching();
    }
}
