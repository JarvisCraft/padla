package ru.progrm_jarvis.ultimatemessenger.format.model;

import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.ultimatemessenger.format.model.AsmTextModelFactory.StringConcatFactoryAlgorithm;

import java.util.stream.Stream;

class AsmTextModelFactoryTest extends AbstractTextModelFactoryTest {

    @Override
    protected @NotNull Stream<@NotNull TextModelFactory<@NotNull User>> provideTestSubjects() {
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