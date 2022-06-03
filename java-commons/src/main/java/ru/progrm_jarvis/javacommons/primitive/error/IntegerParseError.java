package ru.progrm_jarvis.javacommons.primitive.error;

import ru.progrm_jarvis.padla.annotation.EnumHelper;

/**
 * An error occurring while parsing an integer (e.g. an {@code int}).
 */
@EnumHelper
public enum IntegerParseError {
    /**
     * Parsed expression contains no digits at all.
     */
    EMPTY,
    /**
     * Parsed expression contains only a sign (i.e. {@code '+'} or {@code '-'}).
     */
    ONLY_SIGN,
    /**
     * Parsed expression contains an invalid character.
     */
    INVALID_CHARACTER,
    /**
     * Parsed expression represents a number which is out of resulting type's bounds.
     */
    OUT_OF_BOUNDS
}
