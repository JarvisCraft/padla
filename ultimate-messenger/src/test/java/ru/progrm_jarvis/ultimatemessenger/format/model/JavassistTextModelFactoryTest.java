package ru.progrm_jarvis.ultimatemessenger.format.model;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import java.util.stream.Stream;

@EnabledForJreRange(min = JRE.JAVA_9) // Surefire uses IsolatedClassLoader which causes issues with ClassPool
class JavassistTextModelFactoryTest extends AbstractTextModelFactoryTest {

    @Override
    protected @NotNull Stream<@NotNull TextModelFactory<@NotNull User>> provideTestSubjects() {
        return Stream.of(new JavassistTextModelFactory<>());
    }
}