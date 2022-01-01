package ru.progrm_jarvis.padla.annotation.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * Commonly used parts of Java source files.
 */
@UtilityClass
public class JavaSourceParts {

    public final @NotNull String LINE_SEPARATOR = System.lineSeparator();
    public final @NotNull String END_OF_STATEMENT = ';' + LINE_SEPARATOR;
    public final @NotNull String START_OF_EXPRESSION = '{' + LINE_SEPARATOR;
    public final @NotNull String END_OF_EXPRESSION = '}' + LINE_SEPARATOR;

    @UtilityClass
    public class PackageNames {
        public final @NotNull String JAVA_LANG = "java.lang";
        public final @NotNull String JAVA_UTIL = "java.util";
        public final @NotNull String JAVA_UTIL_STREAM = "java.util.stream";
        public final @NotNull String JAVAX_ANNOTATION_PROCESSING  = "javax.annotation.processing";
    }

    @UtilityClass
    public class Names {
        public final @NotNull String ASSERTION_ERROR = "AssertionError";

        public final @NotNull String LIST = "List";
        public final @NotNull String SET = "Set";
        public final @NotNull String ARRAYS = "Arrays";
        public final @NotNull String COLLECTIONS = "Collections";
        public final @NotNull String ENUM_SET = "EnumSet";
        public final @NotNull String STREAM = "Stream";
        public final @NotNull String GENERATED = "Generated";
    }
}
