package ru.progrm_jarvis.ultimatemessenger.format.model;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

class JavassistTextModelFactoryTest extends AbstractTextModelFactoryTest {

    @Override
    protected @NotNull Stream<@NotNull TextModelFactory<@NotNull User>> provideTestSubjects() {
        return Stream.of(JavassistTextModelFactory.create());
    }
}