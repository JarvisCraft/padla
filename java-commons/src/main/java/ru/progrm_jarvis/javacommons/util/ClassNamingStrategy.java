package ru.progrm_jarvis.javacommons.util;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import ru.progrm_jarvis.javacommons.classloading.ClassLoadingUtil;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Object responsible for creating class names at runtime.
 */
public interface ClassNamingStrategy extends Supplier<String> {

    /**
     * Creates a name for a new class to be generated considering
     * that it should not overlap any names of loaded classes.
     *
     * @return name for a new class
     */
    @Override
    String get();

    /**
     * Creates new instance of paginated class naming strategy with the given base name.
     * This strategy will append numeric IDs to the given base name.
     *
     * @param baseName base name of the generated class names to which the ID should be appended
     *
     * @return created paginated class naming strategy
     */
    static PaginatedClassNamingStrategy createPaginated(@NonNull final String baseName) {
        return new PaginatedClassNamingStrategy(baseName);
    }

    /***
     * Class naming strategy appending numeric IDs to the base name.
     */
    @EqualsAndHashCode
    @FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
    @NonFinal class PaginatedClassNamingStrategy implements ClassNamingStrategy {

        /**
         * Base name of the generated class names to which the ID should be appended
         */
        @NonNull String baseName;

        /**
         * Counter incremented for each attempt to create a class name
         */
        @NonNull AtomicReference<BigInteger> counter;

        /**
         * Creates new instance of paginated class naming strategy with the given base name.
         *
         * @param baseName base name of the generated class names to which the ID should be appended
         */
        public PaginatedClassNamingStrategy(@NonNull final String baseName) {
            this.baseName = baseName;

            counter = new AtomicReference<>(BigInteger.ZERO);
        }

        /**
         * Gets the next ID to be used for naming the class.
         *
         * @return next ID to be used for class naming
         *
         * @apiNote always returns new value
         */
        protected BigInteger nextClassNameId() {
            return counter.updateAndGet(id -> id.add(BigInteger.ONE));
        }

        @Override
        public String get() {
            String name;
            do {
                name = baseName + nextClassNameId().toString();
            } while (ClassLoadingUtil.isClassAvailable(name, false));

            return name;
        }
    }
}
