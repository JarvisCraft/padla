package ru.progrm_jarvis.ultimatemessenger.format.model;

import java.util.stream.Stream;

class JavassistTextModelFactoryTest extends AbstractTextModelFactoryTest {

    static Stream<TextModelFactory<User>> provideTestSubjects() {
        return Stream.of(new JavassistTextModelFactory<>());
    }
}