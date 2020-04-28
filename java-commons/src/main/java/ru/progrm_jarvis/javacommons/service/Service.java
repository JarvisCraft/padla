package ru.progrm_jarvis.javacommons.service;

/**
 * A simple service mach may be {@link #enable() enabled} and {@link #disable() disabled}.
 * Normally, a newly instantiated service is considered disabled.
 */
public interface Service extends AutoCloseable {

    /**
     * Enables this service, doing nothing if this service is already enabled.
     */
    void enable();

    /**
     * Disables this service, doing nothing if this service is already disabled.
     */
    void disable();

    @Override
    default void close() {
        disable();
    }
}
