package ru.progrm_jarvis.padla.annotation.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Range;

/**
 * Magical constants of the Java.
 */
@UtilityClass
public class MagicJavaConstants {

    /**
     * Maximal number of pairs allowed by the family of {@link java.util.Map}{@code .of(..)} family of methods
     */
    public @Range(from = 0, to = Integer.MAX_VALUE) int MAP_OF_MAX_PAIRS = 10;
}
