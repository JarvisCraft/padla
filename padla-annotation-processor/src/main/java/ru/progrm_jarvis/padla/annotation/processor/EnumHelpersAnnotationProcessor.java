package ru.progrm_jarvis.padla.annotation.processor;

import com.google.auto.service.AutoService;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.padla.annotation.EnumHelper;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static ru.progrm_jarvis.padla.annotation.util.JavaSourceParts.*;

@AutoService(Processor.class)
public final class EnumHelpersAnnotationProcessor extends AbstractProcessor {

    private static final @NotNull SourceVersion SUPPORTED_SOURCE_VERSION = SourceVersion.latestSupported();

    private static final @NotNull Set<@NotNull String> SUPPORTED_ANNOTATION_TYPES = Collections.singleton(
            EnumHelper.class.getCanonicalName()
    );

    @Override
    public @NotNull SourceVersion getSupportedSourceVersion() {
        return SUPPORTED_SOURCE_VERSION;
    }

    @Override
    public @NotNull Set<@NotNull String> getSupportedAnnotationTypes() {
        return SUPPORTED_ANNOTATION_TYPES;
    }

    @Override
    public boolean process(final @NotNull Set<? extends @NotNull TypeElement> annotations,
                           final @NotNull RoundEnvironment roundEnvironment) {
        val processingEnvironment = processingEnv;
        for (val annotation : annotations)
            for (val element : roundEnvironment.getElementsAnnotatedWith(annotation)) {
                if (element.getKind() != ElementKind.ENUM) {
                    processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "Only enums can be annotated with `@EnumHelpers` but " + element + " is not the one"
                    );

                    continue;
                }

                processAnnotatedEnumType(roundEnvironment, processingEnvironment, (TypeElement) element);
            }

        return false;
    }

    private static void processAnnotatedEnumType(final @NotNull RoundEnvironment roundEnvironment,
                                                 final @NotNull ProcessingEnvironment processingEnvironment,
                                                 final @NotNull TypeElement enumElement) {
        for (val enclosedElement : enumElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.ENUM_CONSTANT) {

            }
        }

        // FIXME
        val enumName = enumElement.getQualifiedName().toString();
        val enumNameDotIndex = enumName.lastIndexOf('.');
        val helperName = enumName + 's';
        val helperNameDotIndex = helperName.lastIndexOf('.');

        try {
            generateEnumHelperClass(processingEnvironment,
                    enumName.substring(0, enumNameDotIndex),
                    enumName.substring(enumNameDotIndex + 1),
                    helperName.substring(0, helperNameDotIndex),
                    helperName.substring(helperNameDotIndex + 1),
                    // FIXME:
                    "list",
                    "set"
            );
        } catch (final IOException e) {
            processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Failed to write generated helper class", enumElement
            );
        }
    }

    // note: RegExp patterns are simplified not to consider non-ascii identifiers
    private static void generateEnumHelperClass(
            final @NotNull ProcessingEnvironment processingEnvironment,
            //@Pattern("(?:[\\w$]+\\.)*")
            final @NotNull String enumPackage,
            //@Pattern("[\\w$]+")
            final @NotNull String enumName,
            //@Pattern("(?:[\\w$]+\\.)*")
            final @NotNull String helperPackage,
            //@Pattern("[\\w$]+")
            final @NotNull String helperName,
            // Parameters
            final @Nullable String listMethod,
            final @Nullable String setMethod
    ) throws IOException {
        System.out.println(
                "processingEnvironment = " + processingEnvironment + ", enumPackage = " + enumPackage + ", enumName = "
                        + enumName + ", helperPackage = " + helperPackage + ", helperName = " + helperName
                        + ", listMethod = " + listMethod + ", setMethod = " + setMethod);
        val file = processingEnvironment.getFiler().createSourceFile(helperName);
        try (val out = file.openWriter()) {
            out
                    .append("package ").append(helperPackage).append(END_OF_STATEMENT)
                    .append(LINE_SEPARATOR);

            // enum import
            if (!helperPackage.equals(enumPackage)) out.append("import ").append(enumName).append(END_OF_STATEMENT);

            // FIXME
            final String
                    nullableAnnotation
                    = importAnnotationIfPossible(out, helperName, "org.jetbrains.annotations", "Nullable"),
                    notNullAnnotation
                            = importAnnotationIfPossible(out, helperName, "org.jetbrains.annotations", "NotNull");

            // `java.*` imports
            out.append(LINE_SEPARATOR);
            final String
                    listClass = importIfPossible(out, helperName, PackageName.JAVA_UTIL, Name.LIST),
                    setClass = importIfPossible(out, helperName, PackageName.JAVA_UTIL, Name.SET),
                    enumSetClass = importIfPossible(out, helperName, PackageName.JAVA_UTIL, Name.ENUM_SET),
                    arraysClass = importIfPossible(out, helperName, PackageName.JAVA_UTIL, Name.ARRAYS),
                    collectionsClass = importIfPossible(out, helperName, PackageName.JAVA_UTIL, Name.COLLECTIONS),
                    streamClass = importIfPossible(out, helperName, PackageName.JAVA_UTIL_STREAM, Name.STREAM);

            // class definition
            out.append(LINE_SEPARATOR)
                    .append("public final class ").append(helperName).append(' ').append(START_OF_EXPRESSION);

            out
                    .append(LINE_SEPARATOR)
                    .append("    private static final ")
                    .append(notNullAnnotation).append(enumName).append(' ').append(notNullAnnotation)
                    .append("[] AS_ARRAY").append(LINE_SEPARATOR)
                    .append("            = ").append(enumName).append(".values()")
                    .append(END_OF_STATEMENT);

            if (listMethod != null) out
                    .append(LINE_SEPARATOR)
                    .append("    private static final ")
                    .append(notNullAnnotation).append(listClass).append('<')
                    .append(notNullAnnotation).append(enumName)
                    .append("> AS_LIST").append(LINE_SEPARATOR)
                    .append("            = ").append(collectionsClass)
                    // FIXME: use List.of is possible
                    .append(".unmodifiableList(").append(arraysClass).append(".asList(AS_ARRAY))")
                    .append(END_OF_STATEMENT);

            if (setMethod != null) out
                    .append(LINE_SEPARATOR)
                    .append("    private static final ")
                    .append(notNullAnnotation).append(setClass).append('<')
                    .append(notNullAnnotation).append(enumName)
                    .append("> AS_SET").append(LINE_SEPARATOR)
                    .append("            = ").append(collectionsClass)
                    // FIXME: use Set.of if possible
                    .append(".unmodifiableSet(")
                    .append(enumSetClass).append(".allOf(").append(enumName)
                    .append(".class))")
                    .append(END_OF_STATEMENT);

            // private failing constructor
            out
                    .append(LINE_SEPARATOR)
                    .append("    private ").append(helperName).append("() ").append(START_OF_EXPRESSION)
                    .append("        throw new ");
            if (helperName.equals(Name.ASSERTION_ERROR)) out.append(PackageName.JAVA_LANG).append('.');
            out.append(Name.ASSERTION_ERROR).append('(').append(LINE_SEPARATOR)
                    .append("                \"").append(helperName).append(
                            " is an utility class and thus cannot be instantiated"
                    ).append('"').append(LINE_SEPARATOR)
                    .append("        )").append(END_OF_STATEMENT)
                    .append("    ").append(END_OF_EXPRESSION);

            if (setMethod != null) out
                    .append(LINE_SEPARATOR)
                    .append("    public static ").append(notNullAnnotation)
                    .append(setClass).append('<').append(notNullAnnotation).append(enumName).append("> ")
                    .append(setMethod).append("() ")
                    .append(START_OF_EXPRESSION)
                    .append("        return AS_SET").append(END_OF_STATEMENT)
                    .append("    ").append(END_OF_EXPRESSION);

            if (listMethod != null) out
                    .append(LINE_SEPARATOR)
                    .append("    public static ").append(notNullAnnotation)
                    .append(listClass).append('<').append(notNullAnnotation).append(enumName).append("> ")
                    .append(listMethod).append("() ")
                    .append(START_OF_EXPRESSION)
                    .append("        return AS_LIST").append(END_OF_STATEMENT)
                    .append("    ").append(END_OF_EXPRESSION);

            out.append(END_OF_EXPRESSION);
        }
    }

    private static final @NotNull String processStringWithPlaceholders(final @NotNull String raw,
                                                                       final @NotNull String enumName,
                                                                       final @NotNull String enumPackage) {
        return raw
                .replace(EnumHelper.Placeholders.NAME, enumName)
                .replace(EnumHelper.Placeholders.PACKAGE, enumPackage);
    }
}
