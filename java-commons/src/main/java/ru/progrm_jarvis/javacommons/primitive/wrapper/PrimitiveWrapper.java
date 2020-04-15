package ru.progrm_jarvis.javacommons.primitive.wrapper;

/**
 * Mutable wrapper of a primitive type, this is mostly useful for passing these to lambdas.
 */
public interface PrimitiveWrapper {

    /**
     * Returns the primitive class wrapped by this one.
     *
     * @return wrapped primitive class
     */
    Class<?> getPrimitiveClass();

    /**
     * Returns the {@link java.lang} primitive wrapper class corresponding to the one wrapped.
     *
     * @return related {@link java.lang} primitive wrapper
     */
    Class<?> getWrapperClass();
}
