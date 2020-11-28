package ru.progrm_jarvis.javacommons.classloading;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Object responsible for creating class names at runtime.
 */
@FunctionalInterface
public interface ClassNamingStrategy extends Supplier<@NotNull String> {

    /**
     * Creates a name for a new class to be generated considering
     * that it should not overlap any names of loaded classes.
     *
     * @return name for a new class
     */
    @NotNull String nextName();

    @Override
    default @NotNull String get() {
        return nextName();
    }

    /**
     * Creates new instance of paginated class naming strategy with the given base name.
     * This strategy will append numeric IDs to the given base name.
     *
     * @param baseName base name of the generated class names to which the ID should be appended
     * @return created paginated class naming strategy
     */
    static @NotNull ClassNamingStrategy createPaginated(final @NonNull String baseName) {
        return new PaginatedClassNamingStrategy(baseName, new AtomicReference<>(BigInteger.ZERO));
    }

    /***
     * Class naming strategy appending numeric IDs to the base name.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    final class PaginatedClassNamingStrategy implements ClassNamingStrategy {

        /**
         * Base name of the generated class names to which the ID should be appended
         */
        @NotNull String baseName;

        /**
         * Counter incremented for each attempt to create a class name
         */
        @NotNull AtomicReference<BigInteger> counter;

        /**
         * Gets the next ID to be used for naming the class.
         *
         * @return next ID to be used for class naming
         *
         * @apiNote always returns new value
         */
        private BigInteger nextClassNameId() {
            return counter.updateAndGet(id -> id.add(BigInteger.ONE));
        }

        @Override
        public @NotNull String nextName() {
            String name;
            do name = baseName + nextClassNameId();
            while (ClassLoadingUtil.isClassAvailable(name, false));

            return name;
        }
    }
}
