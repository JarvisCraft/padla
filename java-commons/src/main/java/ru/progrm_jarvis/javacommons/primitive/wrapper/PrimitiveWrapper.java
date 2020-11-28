package ru.progrm_jarvis.javacommons.primitive.wrapper;

/**
 * Mutable wrapper of a primitive type, this is mostly useful for passing these to lambdas.
 *
 * @param <W> type of wrapped value
 */
public interface PrimitiveWrapper<W> {

    /**
     * Returns the primitive class wrapped by this one.
     *
     * @return wrapped primitive class
     */
    Class<? extends W> getPrimitiveClass();

    /**
     * Returns the {@link java.lang} primitive wrapper class corresponding to the one wrapped.
     *
     * @return related {@link java.lang} primitive wrapper
     */
    Class<? extends W> getWrapperClass();
}
