package ru.progrm_jarvis.padla.annotation.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * Commonly used parts of Java source files.
 */
@UtilityClass
public class JavaSourceParts {

    /**
     * Value of {@link System#lineSeparator()}
     */
    public final @NotNull String LINE_SEPARATOR = System.lineSeparator();

    /**
     * Semicolon followed by {@link #LINE_SEPARATOR}
     */
    public final @NotNull String END_OF_STATEMENT = ';' + LINE_SEPARATOR;

    /**
     * Left brace followed by {@link #LINE_SEPARATOR}
     */
    public final @NotNull String START_OF_EXPRESSION = '{' + LINE_SEPARATOR;

    /**
     * Right brace followed by {@link #LINE_SEPARATOR}
     */
    public final @NotNull String END_OF_EXPRESSION = '}' + LINE_SEPARATOR;

    /**
     * Commonly used class package names.
     */
    @UtilityClass
    public class PackageNames {

        /**
         * Name of {@link java.lang}
         */
        public final @NotNull String JAVA_LANG = "java.lang";

        /**
         * Name of {@link java.util}
         */
        public final @NotNull String JAVA_UTIL = "java.util";

        /**
         * Name of {@link java.util.stream}
         */
        public final @NotNull String JAVA_UTIL_STREAM = "java.util.stream";

        /**
         * Name of {@link javax.annotation.processing}
         */
        public final @NotNull String JAVAX_ANNOTATION_PROCESSING  = "javax.annotation.processing";
    }

    /**
     * Commonly used simple class names.
     */
    @UtilityClass
    public class Names {

        /**
         * Name of {@link AssertionError}
         */
        public final @NotNull String ASSERTION_ERROR = "AssertionError";

        /**
         * Name of {@link AssertionError}
         */
        public final @NotNull String LIST = "List";

        /**
         * Name of {@link AssertionError}
         */
        public final @NotNull String SET = "Set";

        /**
         * Name of {@link java.util.Arrays}
         */
        public final @NotNull String ARRAYS = "Arrays";

        /**
         * Name of {@link java.util.Collections}
         */
        public final @NotNull String COLLECTIONS = "Collections";

        /**
         * Name of {@link java.util.EnumSet}
         */
        public final @NotNull String ENUM_SET = "EnumSet";

        /**
         * Name of {@link java.util.stream.Stream}
         */
        public final @NotNull String STREAM = "Stream";

        /**
         * Name of {@link javax.annotation.processing.Generated}
         */
        public final @NotNull String GENERATED = "Generated";
    }
}
