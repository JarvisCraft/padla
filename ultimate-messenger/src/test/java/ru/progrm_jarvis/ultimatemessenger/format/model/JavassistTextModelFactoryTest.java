package ru.progrm_jarvis.ultimatemessenger.format.model;

import java.util.Collections;

class JavassistTextModelFactoryTest extends AbstractTextModelFactoryTest {

    static Iterable<TextModelFactory<User>> provideTestSubjects() {
        return Collections.singletonList(new JavassistTextModelFactory<>());
    }
}