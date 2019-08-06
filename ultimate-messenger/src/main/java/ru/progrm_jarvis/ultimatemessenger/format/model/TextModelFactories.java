package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.experimental.UtilityClass;
import ru.progrm_jarvis.javacommons.bytecode.BytecodeLibrary;

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
    protected <T> TextModelFactory<T> getAvailable() {
        if (BytecodeLibrary.ASM.isAvailable()) try {
            return AsmGeneratingTextModelFactory.get();
        } catch (final Throwable ignored) {}

        if (BytecodeLibrary.JAVASSIST.isAvailable()) try {
            return AsmGeneratingTextModelFactory.get();
        } catch (final Throwable ignored) {}

        return SimpleTextModelFactory.get();
    }
}
