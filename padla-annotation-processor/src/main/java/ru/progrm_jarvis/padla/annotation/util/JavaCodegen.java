package ru.progrm_jarvis.padla.annotation.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.io.Writer;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Common methods for Java code generation.
 */
@UtilityClass
public class JavaCodegen {

    public @NotNull String annotationValueString(final @NotNull AnnotationValue value) {
        val exactValue = value.getValue();
        assert exactValue instanceof String
                : "annotation value is expected to be of type `String`";

        return (String) exactValue;
    }

    public @NotNull TypeMirror annotationValueClass(final @NotNull AnnotationValue value) {
        val exactValue = value.getValue();
        assert exactValue instanceof TypeMirror
                : "annotation value is expected to be of type `TypeMirror`";

        return (TypeMirror) exactValue;
    }

    public @NotNull DeclaredType annotationValueNonPrimitiveClass(final @NotNull AnnotationValue value) {
        val exactValue = annotationValueClass(value);
        assert exactValue.getKind() == TypeKind.DECLARED
                : "annotation value is expected to be of kind `DECLARED`";

        return (DeclaredType) exactValue;
    }

    public @NotNull TypeElement annotationValueAnnotationClass(final @NotNull AnnotationValue value) {
        val exactValue = annotationValueNonPrimitiveClass(value).asElement();
        assert exactValue.getKind() == ElementKind.ANNOTATION_TYPE
                : "annotation value is expected to be of kind `ANNOTATION_TYPE`";

        return (TypeElement) exactValue;
    }

    public @NotNull AnnotationMirror annotationValueAnnotation(final @NotNull AnnotationValue value) {
        val exactValue = value.getValue();
        assert exactValue instanceof AnnotationMirror : "annotation value is expected to be of type `AnnotationMirror`";

        return (AnnotationMirror) exactValue;
    }

    public void writeImport(final @NonNull Writer writer,
                            final @NonNull String importedClass) throws IOException {
        writer.append("import ").append(importedClass).write(JavaSourceParts.END_OF_STATEMENT);
    }

    public void writeImports(final @NonNull Writer writer,
                             final @NonNull Set<@NotNull String> importedClasses) throws IOException {
        final List<String>
                javaxImports = new ArrayList<>(),
                javaImports = new ArrayList<>();

        boolean addEmptyLine = false;
        for (val importedClass : importedClasses) {
            // minimal form is `java.X`
            if (importedClass.length() >= 6 && importedClass.startsWith("java")) {
                final char nextChar;
                if ((nextChar = importedClass.charAt(4)) == '.') {
                    javaImports.add(importedClass);
                    continue;
                }
                if (nextChar == 'x' && importedClass.charAt(5) == '.') {
                    javaxImports.add(importedClass);
                    continue;
                }
            }

            writeImport(writer, importedClass);
            addEmptyLine = true;
        }
        if (addEmptyLine) writer.write(JavaSourceParts.LINE_SEPARATOR);

        // note: there is no empty lien between `java` and `javax` imports
        if (!javaxImports.isEmpty()) {
            for (val importedClass : javaxImports) writeImport(writer, importedClass);
            addEmptyLine = true;
        }
        if (!javaImports.isEmpty()) {
            for (val importedClass : javaImports) writeImport(writer, importedClass);
            addEmptyLine = true;
        }
        if (addEmptyLine) writer.write(JavaSourceParts.LINE_SEPARATOR);
    }

    public void appendGenerated(final @NotNull Writer writer,
                                final @NotNull CharSequence $Generated,
                                final @NotNull String processorName,
                                final @NotNull TemporalAccessor date) throws IOException {
        writer
                // `@Generated(`
                .append('@').append($Generated).append('(').append(JavaSourceParts.LINE_SEPARATOR)
                // `value = "..."`
                .append("        value = \"").append(processorName).append("\",").append(JavaSourceParts.LINE_SEPARATOR)
                // `date = "..."`
                .append("        date = \"")
                .append(Iso8601.DATE_TIME_FORMAT.format(date))
                .append('"').append(JavaSourceParts.LINE_SEPARATOR)
                // `)`
                .append(')').write(JavaSourceParts.LINE_SEPARATOR);
    }

    private static final class Iso8601 {

        private static final @NotNull DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mmZ")
                .withZone(ZoneOffset.UTC);
    }
}
