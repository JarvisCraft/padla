package ru.progrm_jarvis.javacommons.primitive;

/**
 * Interface analog of {@link Number}.
 */
public interface Numeric {

    /**
     * Returns the value as {@code byte}.
     *
     * @return value as {@code byte}
     */
    byte byteValue();

    /**
     * Returns the value as {@code short}.
     *
     * @return value as {@code short}
     */
    short shortValue();

    /**
     * Returns the value as {@code int}.
     *
     * @return value as {@code int}
     */
    int intValue();

    /**
     * Returns the value as {@code long}.
     *
     * @return value as {@code long}
     */
    long longValue();

    /**
     * Returns the value as {@code float}.
     *
     * @return value as {@code float}
     */
    float floatValue();

    /**
     * Returns the value as {@code double}.
     *
     * @return value as {@code double}
     */
    double doubleValue();
}
