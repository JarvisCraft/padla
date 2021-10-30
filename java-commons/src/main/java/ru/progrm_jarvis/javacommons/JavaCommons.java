package ru.progrm_jarvis.javacommons;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import ru.progrm_jarvis.javacommons.annotation.Any;

/**
 * Internal API methods of <b>java-commons</b>.
 */
@UtilityClass
@ApiStatus.Internal
public class JavaCommons {

    /**
     * Throws an {@link AssertionError} indicating that the caller should have been patched by <b>PADLA patcher</b>.
     *
     * @param <T> the required type of the expression
     * @return <i>never</i>
     */
    @Contract("-> fail")
    public <@Any T> T requireBytecodePatching() {
        throw new AssertionError("Caller should have had it class patched using PADLA patcher");
    }
}
