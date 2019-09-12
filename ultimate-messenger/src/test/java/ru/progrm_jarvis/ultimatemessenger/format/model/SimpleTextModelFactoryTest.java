package ru.progrm_jarvis.ultimatemessenger.format.model;

import java.util.Collections;

class SimpleTextModelFactoryTest extends AbstractTextModelFactoryTest {

    static Iterable<TextModelFactory<User>> provideTestSubjects() {
        return Collections.singletonList(new SimpleTextModelFactory<>());
    }
}