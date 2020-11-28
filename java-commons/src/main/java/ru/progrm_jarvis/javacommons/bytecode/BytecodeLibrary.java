package ru.progrm_jarvis.javacommons.bytecode;

/**
 * Library used for bytecode manipulations.
 */
public interface BytecodeLibrary {

    /**
     * Checks if this bytecode library seems to be available at runtime.
     *
     * @return {@code true} if this bytecode library seems to be available at runtime and {@code false} otherwise
     */
    boolean isAvailable();

    /**
     * Checks if this bytecode library seems to be currently available at runtime updating its availability state.
     *
     * @return {@code true} if this bytecode library seems to be available at runtime and {@code false} otherwise
     */
    boolean checkAvailability();
}
