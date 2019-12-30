package ru.progrm_jarvis.ultimatemessenger.format.model;

import ru.progrm_jarvis.ultimatemessenger.format.model.AsmTextModelFactory.Configuration.StringConcatFactoryAlgorithm;

import java.util.stream.Stream;

class AsmTextModelFactoryTest extends AbstractTextModelFactoryTest {

    static Stream<TextModelFactory<User>> provideTestSubjects() {
        return Stream.of(
                /*
                AsmTextModelFactory.create(
                        AsmTextModelFactory.configuration()
                                .enableStringConcatFactory(true)
                                .stringConcatFactoryAlgorithm(StringConcatFactoryAlgorithm.TREE)
                                .build()
                ),
                */
                AsmTextModelFactory.create(
                        AsmTextModelFactory.configuration()
                                .enableStringConcatFactory(true)
                                .stringConcatFactoryAlgorithm(StringConcatFactoryAlgorithm.VECTOR)
                                .build()
                ),
                AsmTextModelFactory.create(
                        AsmTextModelFactory.configuration()
                                .enableStringConcatFactory(false)
                                .build()
                )
        );
    }
}