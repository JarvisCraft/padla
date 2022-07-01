package ru.progrm_jarvis.javacommons.classloading;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.javacommons.object.Pair;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

/**
 * Object used for runtime class definition.
 */
public interface ClassDefiner {

    /**
     * Uns
     * Defines a class which may be garbage-collected.
     *
     * @param owner lookup whose access rights will be used for class definition
     * @param name name of the defined class
     * @param bytecode bytecode of the class
     *
     * @return defined class
     */
    @NotNull Class<?> defineClass(
            MethodHandles.@NonNull Lookup owner,
            @Nullable String name, byte @NonNull [] bytecode
    );

    /**
     * Defines multiple classes which may be garbage-collected.
     *
     * @param owner lookup whose access rights will be used for class definition
     * @param bytecodes pairs whose first values are canonical class names
     * and the second values are those classes' bytecodes
     *
     * @return defined class in the order their data was passed
     */
    @SuppressWarnings("unchecked")
    @NotNull Class<?> @NotNull [] defineClasses(
            MethodHandles.@NonNull Lookup owner,
            @NonNull Pair<@Nullable String, byte @NotNull []>... bytecodes
    );

    /**
     * Defines multiple classes which may be garbage-collected.
     *
     * @param owner lookup whose access rights will be used for class definition
     * @param bytecodes bytecodes of the classes
     *
     * @return defined class in the order their data was passed
     */
    @NotNull Class<?> @NotNull [] defineClasses(
            MethodHandles.@NonNull Lookup owner,
            byte @NotNull [] @NonNull ... bytecodes
    );

    /**
     * Defines multiple classes which may be garbage-collected.
     *
     * @param owner lookup whose access rights will be used for class definition
     * @param bytecodes bytecodes of the classes
     *
     * @return defined class in the order their data was passed
     */
    @NotNull List<@NotNull Class<?>> defineClasses(
            MethodHandles.@NonNull Lookup owner,
            @NonNull List<byte @NotNull []> bytecodes
    );

    /**
     * Defines multiple classes which may be garbage-collected.
     *
     * @param owner lookup whose access rights will be used for class definition
     * @param namedBytecode map containing bytecodes by their classes' canonical names
     *
     * @return defined classes by their names
     */
    @NotNull Map<@NotNull String, @NotNull Class<?>> defineClasses(
            MethodHandles.@NonNull Lookup owner,
            @NonNull Map<@Nullable String, byte @NotNull []> namedBytecode
    );
}
