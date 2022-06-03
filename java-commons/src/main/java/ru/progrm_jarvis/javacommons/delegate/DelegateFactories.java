package ru.progrm_jarvis.javacommons.delegate;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.bytecode.CommonBytecodeLibrary;

/**
 * Utility making use of {@link DelegateFactory delegate factories} easier.
 */
@UtilityClass
public class DelegateFactories {

    /**
     * Gets the best available {@link DelegateFactory delegate factory}.
     *
     * @return the best available {@link DelegateFactory delegate factory}
     */
    public @NotNull DelegateFactory createAvailable() {
        if (CommonBytecodeLibrary.ASM.isAvailable()) try {
            return AsmDelegateFactory.create();
        } catch (final Throwable ignored) {}

        return ProxyDelegateFactory.create();
    }
}
