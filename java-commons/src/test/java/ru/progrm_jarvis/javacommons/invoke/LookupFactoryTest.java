package ru.progrm_jarvis.javacommons.invoke;

import org.junit.jupiter.api.Test;
import ru.progrm_jarvis.javacommons.invoke.LookupFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

class LookupFactoryTest {

    @Test
    void testInstantiatingFactory() {
        assertThat(LookupFactory.INSTANTIATING_LOOKUP_FACTORY.get(), notNullValue());
    }

    @Test
    void testTrustedSingletonFactory() {
        assertThat(LookupFactory.TRUSTED_LOOKUP_FACTORY.get(), notNullValue());
    }
}