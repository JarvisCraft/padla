package ru.progrm_jarvis.ultimatemessenger.format.model;

import java.util.stream.Stream;

class SimpleTextModelFactoryTest extends AbstractTextModelFactoryTest {

    static Stream<TextModelFactory<User>> provideTestSubjects() {
        return Stream.of(new SimpleTextModelFactory<>());
    }
}