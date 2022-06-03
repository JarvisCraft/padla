package ru.progrm_jarvis.javacommons.bytecode;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import ru.progrm_jarvis.javacommons.classloading.ClassLoadingUtil;
import ru.progrm_jarvis.javacommons.lazy.Lazy;
import ru.progrm_jarvis.padla.annotation.EnumHelper;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Common bytecode libraries.
 *
 * @implNote this could have been implemented
 */
@EnumHelper
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum CommonBytecodeLibrary implements BytecodeLibrary {

    /**
     * ObjectWeb ASM
     */
    ASM("org.objectweb.asm.Opcodes"),
    /**
     * Javassist
     */
    JAVASSIST("javassist.ClassPool"),
    /**
     * Commons BCEL
     */
    BCEL("org.apache.bcel.classfile.JavaClass"),
    /**
     * GNU Bytecode
     */
    GNU("gnu.bytecode.ClassType");

    /**
     * Name of the class according to whose existence at runtime the availability of this bytecode library gets computed
     */
    @NonNull String checkClassName;

    /**
     * Lazy marker indicating whether this bytecode library seems to be available (according to last check) or not
     */
    Lazy<AtomicBoolean> available = Lazy.createThreadSafe(() -> new AtomicBoolean(forceCheckAvailability()));

    /**
     * Checks if this bytecode library seems to be available at runtime.
     *
     * @return {@code true} if this bytecode library seems to be available at runtime and {@code false} otherwise
     *
     * @implNote this checks if class by name {@link #checkClassName} is accessible via {@link Class#forName(String)}
     */
    private boolean forceCheckAvailability() {
        return ClassLoadingUtil.isClassAvailable(checkClassName, false, Thread.currentThread().getContextClassLoader());
    }

    @Override
    public boolean isAvailable() {
        return available.get().get();
    }

    @Override
    public boolean checkAvailability() {
        if (available.isInitialized()) {
            val available = forceCheckAvailability();
            this.available.get().set(available);

            return available;
        } else return available.get().get(); // simply perform lazy initialization
    }
}
