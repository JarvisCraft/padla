package ru.progrm_jarvis.reflector.wrapper;

/**
 * Basic interface for all wrappers of Reflector.
 *
 * @param <T> type of class to whom the method belongs
 * @param <W> type of wrapped class element
 */
public interface ReflectorWrapper<T, W> {

    /**
     * Gets the class containing the wrapped element.
     *
     * @return class containing the wrapped element
     */
    Class<? extends T> getContainingClass();

    /**
     * Gets the wrapped object.
     *
     * @return wrapped class element
     */
    W getWrapped();
}
