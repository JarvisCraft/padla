package ru.progrm_jarvis.padla.annotation.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

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
    public class PackageName {
        public final @NotNull String JAVA_LANG = "java.lang";
        public final @NotNull String JAVA_UTIL = "java.util";
        public final @NotNull String JAVA_UTIL_STREAM = "java.util.stream";
    }

    @UtilityClass
    public class Name {
        public final @NotNull String ASSERTION_ERROR = "AssertionError";

        public final @NotNull String LIST = "List";
        public final @NotNull String SET = "Set";
        public final @NotNull String ARRAYS = "Arrays";
        public final @NotNull String COLLECTIONS = "Collections";
        public final @NotNull String ENUM_SET = "EnumSet";
        public final @NotNull String STREAM = "Stream";
    }

    public @NotNull String importIfPossible(final @NotNull Appendable writer,
                                            final @NotNull String className,
                                            final @NotNull CharSequence importedPackageName,
                                            final @NotNull String importedClassName) throws IOException {
        if (importedClassName.equals(className)) return importedPackageName + "." + importedClassName;

        writer.append("import ")
                .append(importedPackageName).append('.').append(importedClassName)
                .append(END_OF_STATEMENT);

        return importedClassName;
    }

    public @NotNull String importAnnotationIfPossible(final @NotNull Appendable writer,
                                                      final @NotNull String className,
                                                      final @NotNull CharSequence importedPackageName,
                                                      final @NotNull String importedClassName) throws IOException {
        if (className.isEmpty()) return "";

        if (importedClassName.equals(className)) return "@" + importedPackageName + '.' + importedClassName + ' ';

        writer.append("import ")
                .append(importedPackageName).append('.').append(importedClassName)
                .append(END_OF_STATEMENT);

        return '@' + importedClassName + ' ';
    }
}
