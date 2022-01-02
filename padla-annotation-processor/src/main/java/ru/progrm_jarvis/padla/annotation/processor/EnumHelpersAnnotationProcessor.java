package ru.progrm_jarvis.padla.annotation.processor;

import com.google.auto.service.AutoService;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.padla.annotation.EnumHelper;
import ru.progrm_jarvis.padla.annotation.importing.Imports;
import ru.progrm_jarvis.padla.annotation.importing.SimpleImports;
import ru.progrm_jarvis.padla.annotation.util.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static ru.progrm_jarvis.padla.annotation.util.JavaSourceParts.*;

/**
 * {@link Processor Annotation processing} implementing behaviour of {@link EnumHelper @EnumHelper}.
 */
// TODO: correct support for nested classes
@AutoService(Processor.class)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class EnumHelpersAnnotationProcessor extends AbstractProcessor {

    /**
     * {@link Class#getCanonicalName()} of {@link EnumHelpersAnnotationProcessor}
     */
    private static @NotNull String THIS_CANONICAL_NAME = EnumHelpersAnnotationProcessor.class.getCanonicalName();

    /**
     * Canonical name of {@link EnumHelper.Annotations.None}
     */
    private static @NotNull String ENUM_HELPER__ANNOTATIONS__NONE__CANONICAL_NAME
            = EnumHelper.Annotations.None.class.getCanonicalName();

    /**
     * The value of {@link SourceVersion#latestSupported()}
     */
    private static final @NotNull SourceVersion LATEST_SUPPORTED_SOURCE_VERSION = SourceVersion.latestSupported();

    /**
     * Set containing only {@link EnumHelper}{@code .class}.
     */
    private static final @NotNull Set<@NotNull String> SUPPORTED_ANNOTATION_TYPES = Collections.singleton(
            EnumHelper.class.getCanonicalName()
    );

    /**
     * Preferred source version of the generated class
     */
    @NotNull SourceVersion preferredSourceVersion;

    /**
     * Supplier which provides a time value on request to be used as generation time
     */
    @NotNull Supplier<? extends TemporalAccessor> generationTimeSupplier;

    /**
     * Creates a new {@link EnumHelper @EnumHelper} annotation processor with default configuration.
     */
    @SuppressWarnings({"unused", "PublicConstructor"}) // Java Services API
    public EnumHelpersAnnotationProcessor() {
        this(
                defaultPreferredSourceVersion(),
                defaultGenerationTimeSupplier()
        );
    }

    /**
     * Creates a new {@link EnumHelper @EnumHelper} annotation processor with default configuration.
     *
     * @return created annotation processor
     */
    public static @NotNull Processor create() {
        return new EnumHelpersAnnotationProcessor(
                defaultPreferredSourceVersion(),
                defaultGenerationTimeSupplier()
        );
    }

    /**
     * Creates a new {@link EnumHelper @EnumHelper} annotation processor with the provided configuration.
     *
     * @param generationTimeSupplier supplier which provides a time value on request to be used as generation time
     * @param sourceVersion preferred source version of the generated class.
     *
     * @return created annotation processor
     */
    public static @NotNull Processor create(
            final @NonNull SourceVersion sourceVersion,
            final @NonNull Supplier<? extends TemporalAccessor> generationTimeSupplier
    ) {
        return new EnumHelpersAnnotationProcessor(sourceVersion, generationTimeSupplier);
    }

    /**
     * Creates a default {@link #generationTimeSupplier}.
     *
     * @return default {@link #generationTimeSupplier}
     */
    public static @NotNull Supplier<? extends TemporalAccessor> defaultGenerationTimeSupplier() {
        return Instant::now;
    }

    /**
     * Creates a default {@link #preferredSourceVersion}.
     *
     * @return default {@link #preferredSourceVersion}
     */
    public static @NotNull SourceVersion defaultPreferredSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public @NotNull SourceVersion getSupportedSourceVersion() {
        return LATEST_SUPPORTED_SOURCE_VERSION;
    }

    @Override
    public @NotNull Set<@NotNull String> getSupportedAnnotationTypes() {
        return SUPPORTED_ANNOTATION_TYPES;
    }

    @Override
    public boolean process(final @NotNull Set<? extends @NotNull TypeElement> annotations,
                           final @NotNull RoundEnvironment roundEnvironment) {
        if (roundEnvironment.processingOver()) return false;

        final SourceVersion sourceVersion;
        final Filer filer;
        final Elements elements;
        final Supplier<? extends TemporalAccessor> generationTimeSupplier;
        final Messager messager;
        {
            final ProcessingEnvironment processingEnvironment;
            sourceVersion = SourceVersions.min(
                    preferredSourceVersion,
                    (processingEnvironment = processingEnv).getSourceVersion()
            );
            filer = processingEnvironment.getFiler();
            elements = processingEnvironment.getElementUtils();
            generationTimeSupplier = this.generationTimeSupplier;
            messager = processingEnvironment.getMessager();
        }

        // @formatter:off
        for (val annotationElement : annotations) for (val annotatedElement
                : roundEnvironment.getElementsAnnotatedWith(annotationElement)) {
            // @formatter:on
            if (annotatedElement.getKind() != ElementKind.ENUM) {
                messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Only enums can be annotated with `@EnumHelpers` but "
                                + annotatedElement + " is not the one"
                );

                continue;
            }

            val generationStartTime = generationTimeSupplier.get();

            AnnotationMirror annotationMirror = null;
            // @formatter:off
            for (val annotation : annotatedElement.getAnnotationMirrors()) if (annotation.getAnnotationType()
                    .asElement().equals(annotationElement)) {
                // @formatter:on
                annotationMirror = annotation;
                break;
            }
            assert annotationMirror != null : "element is known to be annotated with this annotation";

            val annotationValue = annotatedElement.getAnnotation(EnumHelper.class);
            assert annotationValue != null : "`element` is known to be annotated with `@EnumHelpers`";

            val annotatedTypeElement = (TypeElement) annotatedElement;
            val enumHelperMirror = EnumHelperMirror.from(annotationMirror, elements);
            try {
                generateEnumHelperClass(
                        sourceVersion,
                        filer,
                        generationStartTime,
                        annotatedTypeElement,
                        enumHelperMirror
                );
            } catch (final IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "Failed to write generated helper class: " + e.getMessage(), annotatedTypeElement
                );
            }
        }

        return true;
    }

    @SuppressWarnings({
            "LocalVariableNamingConvention", // usage of `$[simple class name]` notation for imported names
            "VariableNotUsedInsideIf" // checks against nullability which will matter "later"
    })
    private static void generateEnumHelperClass(
            final @NotNull SourceVersion sourceVersion,
            final @NotNull Filer filer,
            final @NotNull TemporalAccessor generationStartTime,
            final @NotNull TypeElement enumElement,
            @SuppressWarnings("UseOfConcreteClass" /* local class */) final @NotNull EnumHelperMirror enumHelper
    ) throws IOException {
        final String fullEnumName, fullEnumHelperName, enumHelperPackage, enumHelperName;
        {
            final Map<String, String> placeholders;
            {
                final PackageAndName enumPackageAndName;
                (placeholders = new HashMap<>()).put(
                        "package",
                        (enumPackageAndName = ClassNames.parsePackageAndName(
                                fullEnumName = enumElement.getQualifiedName().toString()
                        )).packageName()
                );
                placeholders.put("name", enumPackageAndName.simpleName());
            }
            final PackageAndName enumHelperPackageAndName;
            enumHelperPackage = (enumHelperPackageAndName = ClassNames.parsePackageAndName(
                    fullEnumHelperName = TrivialPlaceholders.replace(enumHelper.name(), placeholders)
            )).packageName();
            enumHelperName = enumHelperPackageAndName.simpleName();
        }

        // do as much as possible before opening a writer

        val java9OrLater = sourceVersion.compareTo(SourceVersion.RELEASE_8) > 0;

        val constantNames = enumElement.getEnclosedElements().stream()
                .filter(element -> element.getKind() == ElementKind.ENUM_CONSTANT)
                .map(element -> element.getSimpleName().toString())
                .collect(Collectors.toSet());

        final boolean anyCollectionMethod;
        final Imports imports;
        final String
                enumName = (imports = SimpleImports.create(fullEnumHelperName)).importClass(fullEnumName),
                $AssertionError = imports.importClass(PackageNames.JAVA_LANG, Names.ASSERTION_ERROR),
                setMethod, listMethod, mapMethod, streamMethod,
                // imported annotation
                $NotNull = importAnnotationIf(imports, enumHelper.annotationNotNull(), anyCollectionMethod =
                        (setMethod = enumHelper.setMethod()) != null
                                | (listMethod = enumHelper.listMethod()) != null
                                | (streamMethod = enumHelper.streamMethod()) != null
                                | (mapMethod = enumHelper.mapMethod()) != null
                ),
                //$Nullable = importAnnotationIf(imports, enumName, ),
                $Unmodifiable = importAnnotationIf(imports, enumHelper.annotationUnmodifiable(), anyCollectionMethod),
                //$UnmodifiableView = importAnnotation(imports, enumHelper.annotationUnmodifiableView())
                // imported collections
                $Collections = setMethod != null || listMethod != null && !java9OrLater
                        ? imports.importClass(PackageNames.JAVA_UTIL, Names.COLLECTIONS) : null,
                $List = listMethod != null
                        ? imports.importClass(PackageNames.JAVA_UTIL, Names.LIST) : null,
                $Stream = streamMethod != null
                        ? imports.importClass(PackageNames.JAVA_UTIL_STREAM, Names.STREAM) : null,
                $Set, $EnumSet;

        if (setMethod != null) {
            $Set = imports.importClass(PackageNames.JAVA_UTIL, Names.SET);
            $EnumSet = imports.importClass(PackageNames.JAVA_UTIL, Names.ENUM_SET);
        } else {
            $Set = null;
            $EnumSet = null;
        }

        final String $String, $Map, $HashMap;
        if (mapMethod != null) {
            $String = imports.importClass(PackageNames.JAVA_LANG, Names.STRING);
            $Map = imports.importClass(PackageNames.JAVA_UTIL, Names.MAP);
            $HashMap = imports.importClass(PackageNames.JAVA_UTIL, Names.HASH_MAP);
        } else {
            $String = null;
            $Map = null;
            $HashMap = null;
        }

        final String $Generated, $Arrays;
        if (java9OrLater) {
            $Generated = imports.importClass(PackageNames.JAVAX_ANNOTATION_PROCESSING, Names.GENERATED);
            $Arrays = null;
        } else {
            $Generated = null;
            $Arrays = imports.importClass(PackageNames.JAVA_UTIL, Names.ARRAYS);
        }

        val file = filer.createSourceFile(fullEnumHelperName, enumElement);
        try (val out = file.openWriter()) {
            // `package ${helperName};`
            out
                    .append("package ").append(enumHelperPackage).append(END_OF_STATEMENT)
                    .write(LINE_SEPARATOR);

            JavaCodegen.writeImports(out, imports.imports());

            if ($Generated != null) JavaCodegen
                    .appendGenerated(out, $Generated, THIS_CANONICAL_NAME, generationStartTime);

            // `public final class `${helperName}` {`
            out.append("public final class ").append(enumHelperName).append(' ').write(START_OF_EXPRESSION);

            if (anyCollectionMethod) out
                    .append(LINE_SEPARATOR)
                    // `private static final @NotNull ${helperName} @NotNull [] AS_ARRAY`
                    .append("    private static final ")
                    .append($NotNull).append(enumName).append(' ').append($NotNull)
                    .append("[] AS_ARRAY").append(LINE_SEPARATOR)
                    // `= <...>.values();`
                    .append("            = ").append(enumName).append(".values()")
                    .write(END_OF_STATEMENT);

            if (setMethod != null) out
                    .append(LINE_SEPARATOR)
                    // `private static final @NotNull @Unmodifiable Set<${enumName}> AS_SET`
                    .append("    private static final ")
                    .append($NotNull).append($Unmodifiable).append($Set)
                    .append('<').append($NotNull).append(enumName).append("> AS_SET").append(LINE_SEPARATOR)
                    // `= Collections.unmodifiableSet(EnumSet.allOf(${enumName}));`
                    .append("            = ").append($Collections)
                    .append(".unmodifiableSet(")
                    .append($EnumSet).append(".allOf(").append(enumName)
                    .append(".class))")
                    .append(END_OF_STATEMENT);

            if (listMethod != null) {
                out
                        .append(LINE_SEPARATOR)
                        .append("    private static final ")
                        .append($NotNull).append($Unmodifiable).append($List).append('<')
                        .append($NotNull).append(enumName).append("> AS_LIST").append(LINE_SEPARATOR)
                        .write("            = ");
                (
                        $Arrays == null
                                ? out.append($List).append(".of(AS_ARRAY)")
                                : out.append($Collections)
                                        .append(".unmodifiableList(").append($Arrays).append(".asList(AS_ARRAY))")
                ).write(END_OF_STATEMENT);
            }

            if (mapMethod != null) out
                    .append(LINE_SEPARATOR)
                    // `private static final @NotNull @Unmodifiable Map<@NotNull String, @NotNull ${enumName} AS_MAP;`
                    .append("    private static final ")
                    .append($NotNull).append($Unmodifiable).append($Map).append('<')
                    .append($NotNull).append($String).append(", ").append($NotNull).append(enumName)
                    .append("> AS_MAP").write(END_OF_STATEMENT);

            // static initializer
            if (mapMethod != null) {
                // TODO: finders
                out.append(LINE_SEPARATOR)
                        // `static {
                        .append("    static ").write(START_OF_EXPRESSION);
                if (mapMethod != null) {
                    // final Map<String, ${enumName}> asMap = new HashMap<>(${constantNames.size()});
                    out.append("        final ").append($Map).append('<')
                            .append($String).append(", ").append(enumName)
                            .append("> asMap = new ").append($HashMap).append("<>(")
                            .append(Integer.toString(constantNames.size()))
                            .append(')').write(END_OF_STATEMENT);

                    // `asMap.put("${constantName}", ${enumName}.${constantName});`
                    for (val constantName : constantNames) out
                            .append("        asMap.put(\"").append(constantName).append("\", ")
                            .append(enumName).append('.').append(constantName).append(')').write(END_OF_STATEMENT);

                    // `AS_MAP = Collections.unmodifiableMap(asMap);`
                    out.append("        AS_MAP = ")
                            .append($Collections).append(".unmodifiableMap(asMap)").write(END_OF_STATEMENT);
                }
                // `}`
                out.append("    ").write(END_OF_EXPRESSION);
            }

            out
                    .append(LINE_SEPARATOR)
                    // `private ${helperName}() {`
                    .append("    private ").append(enumHelperName).append("() ").append(START_OF_EXPRESSION)
                    // `throw new AssertionError(`
                    .append("        throw new ").append($AssertionError).append('(').append(LINE_SEPARATOR)
                    // `"${helperName} is an utility class and thus cannot be instantiated"`
                    .append("                \"").append(enumHelperName).append(
                            " is an utility class and thus cannot be instantiated"
                    ).append('"').append(LINE_SEPARATOR)
                    // `)`
                    .append("        )").append(END_OF_STATEMENT)
                    // `}`
                    .append("    ").append(END_OF_EXPRESSION);

            // set-method
            if (setMethod != null) out
                    .append(LINE_SEPARATOR)
                    // `public static @NotNull @Unmodifiable Set<@NotNull ${enumName}> ${setMethod}() {`
                    .append("    public static ").append($NotNull).append($Unmodifiable)
                    .append($Set).append('<').append($NotNull).append(enumName).append("> ")
                    .append(setMethod).append("() ")
                    .append(START_OF_EXPRESSION)
                    // `return AS_SET;`
                    .append("        return AS_SET").append(END_OF_STATEMENT)
                    // `}`
                    .append("    ").append(END_OF_EXPRESSION);

            // list-method
            if (listMethod != null) out
                    .append(LINE_SEPARATOR)
                    // `public static @NotNull @Unmodifiable List<@NotNull ${enumName}> ${listMethod}() {`
                    .append("    public static ").append($NotNull).append($Unmodifiable)
                    .append($List).append('<').append($NotNull).append(enumName).append("> ")
                    .append(listMethod).append("() ")
                    .append(START_OF_EXPRESSION)
                    // `return AS_LIST;`
                    .append("        return AS_LIST").append(END_OF_STATEMENT)
                    // `}`
                    .append("    ").append(END_OF_EXPRESSION);

            // map-method
            if (mapMethod != null) out
                    .append(LINE_SEPARATOR)
                    // `public static @NotNull @Unmodifiable Map<@NotNull String, @NotNull ${enumName}> ${listMethod}() {`
                    .append("    public static ").append($NotNull).append($Unmodifiable)
                    .append($Map).append('<')
                    .append($NotNull).append($String).append(", ").append($NotNull).append(enumName)
                    .append("> ")
                    .append(mapMethod).append("() ")
                    .append(START_OF_EXPRESSION)
                    // `return AS_MAP;`
                    .append("        return AS_MAP").append(END_OF_STATEMENT)
                    // `}`
                    .append("    ").append(END_OF_EXPRESSION);

            // stream-method
            if (streamMethod != null) out
                    .append(LINE_SEPARATOR)
                    // `public static @NotNull @Unmodifiable Stream<@NotNull ${enumName}> ${streamMethod}() {`
                    .append("    public static ").append($NotNull)
                    .append($Stream).append('<').append($NotNull).append(enumName).append("> ")
                    .append(streamMethod).append("() ")
                    .append(START_OF_EXPRESSION)
                    // `return Stream.of(AS_ARRAY);`
                    .append("        return Stream.of(AS_ARRAY)").append(END_OF_STATEMENT)
                    // `}`
                    .append("    ").append(END_OF_EXPRESSION);

            // `}`
            out.append(END_OF_EXPRESSION);
        }
    }

    @Contract(value = "_, _, true -> !null; _, _, false -> null", mutates = "param1")
    private static @Nullable String importAnnotationIf(final @NonNull Imports imports,
                                                       final @Nullable String fullName,
                                                       final boolean condition) {
        return condition ? fullName == null ? "" : '@' + imports.importClass(fullName) + ' ' : null;
    }

    /**
     * <p>Mirror of {@link EnumHelper} providing codegen-friendly values.</p>
     * <p>The values are flattened (i.e. there is no nesting) and represented as {@link String}s.</p>
     */
    @Value
    @Builder
    @Accessors(fluent = true)
    private static class EnumHelperMirror {

        /**
         * Value of {@link EnumHelper#value()}
         */
        @NotNull String name;

        // @Generate

        /**
         * Value of {@link EnumHelper.Generate#set()}
         */
        @Nullable String setMethod;

        /**
         * Value of {@link EnumHelper.Generate#list()}
         */
        @Nullable String listMethod;

        /**
         * Value of {@link EnumHelper.Generate#stream()}
         */
        @Nullable String streamMethod;

        /**
         * Value of {@link EnumHelper.Generate#map()}
         */
        @Nullable String mapMethod;

        /**
         * Value of {@link EnumHelper.Generate#match()}
         */
        @Nullable String matchMethod;

        // @Annotations

        /**
         * Value of {@link EnumHelper.Annotations#notNull()}
         */
        @Nullable String annotationNotNull;

        /**
         * Value of {@link EnumHelper.Annotations#nullable()}
         */
        @Nullable String annotationNullable;

        /**
         * Value of {@link EnumHelper.Annotations#unmodifiable()}
         */
        @Nullable String annotationUnmodifiable;

        /**
         * Value of {@link EnumHelper.Annotations#unmodifiableView()}
         */
        @Nullable String annotationUnmodifiableView;

        @SuppressWarnings("UseOfConcreteClass") // this is a simple inner value class
        private static @NotNull EnumHelperMirror from(final @NotNull AnnotationMirror annotation,
                                                      final @NotNull Elements elements) {
            val builder = builder();
            elements.getElementValuesWithDefaults(annotation).forEach((key, genericValue) -> {
                final Name keyName;
                if ((keyName = key.getSimpleName()).contentEquals("value")) builder
                        .name(JavaCodegen.annotationValueString(genericValue));
                else if (keyName.contentEquals("generate")) elements
                        .getElementValuesWithDefaults(JavaCodegen.annotationValueAnnotation(genericValue))
                        .forEach((methodName, genericSubValue) -> {
                            // all values are known to be of the same type
                            final String method;
                            {
                                final String exactValue;
                                method = (exactValue = JavaCodegen.annotationValueString(genericSubValue))
                                        .equals(EnumHelper.Generate.NONE) ? null : exactValue;
                            }

                            final Name methodType;
                            if ((methodType = methodName.getSimpleName()).contentEquals("set")) builder
                                    .setMethod(method);
                            else if (methodType.contentEquals("list")) builder.listMethod(method);
                            else if (methodType.contentEquals("stream")) builder.streamMethod(method);
                            else if (methodType.contentEquals("match")) builder.matchMethod(method);
                            else if (methodType.contentEquals("map")) builder.mapMethod(method);
                            else assert false : "Unknown annotation value found at `@EnumHelper.generate`: \""
                                        + methodType + '"';
                        });
                else if (keyName.contentEquals("annotations")) elements
                        .getElementValuesWithDefaults(JavaCodegen.annotationValueAnnotation(genericValue))
                        .forEach((subKey, genericSubValue) -> {
                            // all values are known to be of the same type
                            final String annotationClass;
                            {
                                // TypeElement
                                final Name exactValue;
                                annotationClass = (
                                        exactValue
                                                = JavaCodegen.annotationValueAnnotationClass(genericSubValue)
                                                .getQualifiedName()
                                ).contentEquals(ENUM_HELPER__ANNOTATIONS__NONE__CANONICAL_NAME)
                                        ? null : exactValue.toString();

                                // TODO: check annotation availability
                            }

                            final Name annotationType;
                            if ((annotationType = subKey.getSimpleName()).contentEquals("notNull")) builder
                                    .annotationNotNull(annotationClass);
                            else if (annotationType.contentEquals("nullable")) builder
                                    .annotationNullable(annotationClass);
                            else if (annotationType.contentEquals("unmodifiable")) builder
                                    .annotationUnmodifiable(annotationClass);
                            else if (annotationType.contentEquals("unmodifiableView")) builder
                                    .annotationUnmodifiableView(annotationClass);
                            else assert false : "Unknown annotation value found at `@EnumHelper.annotations`: \""
                                        + annotationType + '"';
                        });
                else assert false : "Unknown annotation attribute found at `@EnumHelper`: \"" + keyName + "\"";
            });

            return builder.build();
        }
    }
}
