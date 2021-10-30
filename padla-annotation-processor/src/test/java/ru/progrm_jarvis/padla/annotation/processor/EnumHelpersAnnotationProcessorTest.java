package ru.progrm_jarvis.padla.annotation.processor;

import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static javax.tools.StandardLocation.SOURCE_OUTPUT;

class EnumHelpersAnnotationProcessorTest {

    @NotNull Compiler compiler;

    @BeforeEach
    void setUp() {
        compiler = Compiler.javac()
                .withProcessors(new EnumHelpersAnnotationProcessor());
    }

    @Test
    void test_default_EnumHelpers() {
        assertThat(compiler.compile(JavaFileObjects.forResource("foo/bar/baz/Simple.java")))
                .generatedFile(SOURCE_OUTPUT, "foo.bar.baz", "Simples.java")
                .hasSourceEquivalentTo(JavaFileObjects.forResource("foo/bar/baz/Simples.java"));
    }
}