package ru.progrm_jarvis.javacommons.bytecode;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import ru.progrm_jarvis.javacommons.lazy.Lazy;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Common bytecode libraries.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum BytecodeLibrary {

    /**
     * ObjectWeb ASM
     */
    ASM("org.objectweb.asm.Opcodes"),
    /**
     * Javassist
     */
    JAVASSIST("javassist.ClassPool");

    /**
     * Name of the class according to whose existence at runtime the availability of this bytecode library gets computed
     */
    @NonNull String checkClassName;

    /**
     * Lazy marker indicating whether this bytecode library seems to be available (according to last check) or not
     */
    Lazy<AtomicBoolean> AVAILABLE = Lazy.createThreadSafe(() -> new AtomicBoolean(forceCheckAvailability()));

    /**
     * Checks if this bytecode library seems to be available at runtime.
     *
     * @return {@code true} if this bytecode library seems to be available at runtime and {@code false} otherwise
     *
     * @implNote this checks if class by name {@link #checkClassName} is accessible via {@link Class#forName(String)}
     */
    protected boolean forceCheckAvailability() {
        try {
            return Class.forName(checkClassName, false, Thread.currentThread().getContextClassLoader()) != null;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Checks if this bytecode library seems to be available at runtime (according to the last performed check).
     *
     * @return {@code true} if this bytecode library seems to be available at runtime
     * (according to the last performed check) and {@code false} otherwise
     */
    public boolean isAvailable() {
        return AVAILABLE.get().get();
    }

    /**
     * Checks if this bytecode library seems to be currently available at runtime updating its availability state.
     *
     * @return {@code true} if this bytecode library seems to be available at runtime and {@code false} otherwise
     */
    protected boolean checkAvailability() {
        if (AVAILABLE.isInitialized()) {
            val available = forceCheckAvailability();
            AVAILABLE.get().set(available);

            return available;
        } else return AVAILABLE.get().get(); // simply perform lazy initialization
    }
}
