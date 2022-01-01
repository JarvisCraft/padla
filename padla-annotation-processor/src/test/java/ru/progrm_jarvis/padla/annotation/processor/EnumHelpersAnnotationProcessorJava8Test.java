package ru.progrm_jarvis.padla.annotation.processor;

import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.lang.model.SourceVersion;
import java.io.IOException;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.TimeZone;

import static com.google.testing.compile.CompilationSubject.assertThat;

class EnumHelpersAnnotationProcessorJava8Test {

    private @NotNull Compiler compiler;

    @BeforeEach
    void setUp() {
        final TemporalAccessor generationTime;
        {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"));
            calendar.set(2020, Calendar.JULY, 8, 2, 42);

            generationTime = calendar.toInstant();
        }

        compiler = Compiler.javac()
                .withProcessors(EnumHelpersAnnotationProcessor.create(SourceVersion.RELEASE_8, () -> generationTime));
    }

    @Test
    void defaultAnnotationConfiguration() throws IOException {
        assertThat(compiler.compile(JavaFileObjects.forResource("annotated/EnumHelper/Simple.java")))
                .generatedSourceFile("foo.bar.baz.Simples")
                .contentsAsUtf8String()
                .isEqualTo(JavaFileObjects.forResource("expected/EnumHelper/java8/Simples.java").getCharContent(false));
    }
}