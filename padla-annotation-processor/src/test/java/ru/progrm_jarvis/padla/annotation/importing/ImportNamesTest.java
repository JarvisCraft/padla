package ru.progrm_jarvis.padla.annotation.importing;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ImportNamesTest {

    @Test
    void importJava_noJavaImports() {
        val imports = new String[]{
                "javax.naming.Name",
                "com.google.Baz",
                "com.google.Foo",
                "com.google.Bar",
                "java.lang.String",
        };
        Arrays.sort(imports, ImportNames.javaPackagesLast());

        assertArrayEquals(
                new String[]{
                        "com.google.Bar",
                        "com.google.Baz",
                        "com.google.Foo",
                        "java.lang.String",
                        "javax.naming.Name"
                },
                imports
        );
    }

    static @NotNull Stream<@NotNull String> provideJavaClasses() {
        return Stream.of(
                "java.lang.String",
                "java.X",
                "java.util.UUID",
                "java.util.function.Function",
                "javax.naming.Name",
                "javax.Something",
                "java.lang.RuntimeException"
        );
    }

    static @NotNull Stream<@NotNull String> provideNonJavaClasses() {
        return Stream.of(
                "foo.bar.Baz",
                "A",
                "oh.Yes",
                "some.other.class.in.a.longy.Package",
                "Short",
                "Java",
                "JavaRules",
                "javafoo.Nice",
                "javaxing.Lul"
        );
    }

    @ParameterizedTest
    @MethodSource("provideJavaClasses")
    void isJavaPackage_true(final @NotNull String className) {
        assertTrue(ImportNames.isInJavaPackage(className));
    }

    @ParameterizedTest
    @MethodSource("provideNonJavaClasses")
    void isJavaPackage_false(final @NotNull String className) {
        assertFalse(ImportNames.isInJavaPackage(className));
    }
}