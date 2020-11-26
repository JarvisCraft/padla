package ru.progrm_jarvis.padla.tools.unsafemethodsaccessgenerator;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * CLI tool used for generating accessor class for {@code Unsafe}.
 */
@UtilityClass
public final class UnsafeMethodsAccessGenerator {

    private final Options OPTIONS = new Options()
            .addRequiredOption("c", "class-name", true, "Class name")
            .addOption("p", "package-name", true, "Package name");

    public void main(final @NotNull String... args) throws ParseException, IOException {
        final @NotNull String className;
        final @Nullable String packageName;
        {
            val commandLine = new DefaultParser().parse(OPTIONS, args);

            className = commandLine.getOptionValue("class-name");
            packageName = commandLine.getOptionValue('p');
        }

        val engine = new VelocityEngine();
        engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        engine.init();

        val template = engine.getTemplate("/templates/UnsafeMethodsAccess.java.vm");
        val writer = new OutputStreamWriter(System.out);
        writeUnsafeMethodsAccessClass(
                className, packageName, template, writer
        );
        writer.flush(); // close shouldn't be called on System.out
    }

    private void addImport(final @NotNull Collection<String> importedClasses,
                           @NotNull Class<?> possiblyImportedClass /* may be replaced with content */) {
        while (possiblyImportedClass.isArray()) possiblyImportedClass = possiblyImportedClass.getComponentType();
        if (possiblyImportedClass.isPrimitive()) return;

        val className = possiblyImportedClass.getName();
        if (className.startsWith("java.lang.") && className.indexOf('.', 10) == -1) return;

        importedClasses.add(className);
    }

    private void writeUnsafeMethodsAccessClass(final @NonNull String className,
                                                      final @Nullable String packageName,
                                                      final @NonNull Template template,
                                                      final @NonNull Writer output) {
        final Class<?> unsafeClass;
        {
            Class<?> sunMiscUnsafeClass;
            try {
                sunMiscUnsafeClass = Class.forName("sun.misc.Unsafe");
            } catch (final ClassNotFoundException e) {
                sunMiscUnsafeClass = null;
            }

            if (sunMiscUnsafeClass == null) try {
                unsafeClass = Class.forName("jdk.internal.misc.Unsafe");
            } catch (final ClassNotFoundException classNotFoundException) {
                throw new RuntimeException("Could not find Unsafe class");
            } else unsafeClass = sunMiscUnsafeClass;
        }

        val unsafeMethods = unsafeClass.getMethods(); // we only need public Unsafe methods
        val importedClasses = new HashSet<String>();
        for (val unsafeMethod : unsafeMethods) {
            addImport(importedClasses, unsafeMethod.getReturnType());
            for (val parameter : unsafeMethod.getParameterTypes()) addImport(importedClasses, parameter);
        }

        val context = new VelocityContext();
        context.put("className", className);
        context.put("packageName", packageName);
        context.put("importedClasses", importedClasses);
        context.put(
                "unsafeMethods",
                Arrays.stream(unsafeMethods)
                        .filter(method -> !Modifier.isStatic(method.getModifiers()))
                        .map(UnsafeMethodData::from)
                        .toArray(UnsafeMethodData[]::new)
        );

        template.merge(context, output);
    }
}
