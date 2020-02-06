package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.bytecode.CommonBytecodeLibrary;

/**
 * Utility making use of {@link TextModelFactory text model factories} easier.
 */
@UtilityClass
public class TextModelFactories {

    /**
     * Gets the best available {@link TextModelFactory text model factory}.
     *
     * @param <T> generic type of {@link TextModelFactory}
     * @return the best available {@link TextModelFactory text model factory}
     */
    @NotNull public <T> TextModelFactory<T> getAvailable() {
        if (CommonBytecodeLibrary.ASM.isAvailable()) try {
            return AsmTextModelFactory.get();
        } catch (final Throwable ignored) {}

        if (CommonBytecodeLibrary.JAVASSIST.isAvailable()) try {
            return JavassistTextModelFactory.get();
        } catch (final Throwable ignored) {}

        return SimpleTextModelFactory.get();
    }
}
