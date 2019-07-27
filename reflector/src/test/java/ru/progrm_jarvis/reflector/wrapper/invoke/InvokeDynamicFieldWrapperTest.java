package ru.progrm_jarvis.reflector.wrapper.invoke;

import lombok.AllArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class InvokeDynamicFieldWrapperTest {

    @Test
    void testPrivatePrimitiveFieldAccess() throws NoSuchFieldException {
        val random = ThreadLocalRandom.current();

        val icq = random.nextInt();
        val instance = new Areg(icq, "Oreshek");
        val field = InvokeDynamicFieldWrapper.<Areg, Integer>from(Areg.class.getDeclaredField("icq"));

        // get
        assertThat(field.get(instance), equalTo(icq));
        // set
        val newIcq = icq + random.nextInt(1, Integer.MAX_VALUE);
        assertDoesNotThrow(() -> field.set(instance, newIcq));
        assertThat(field.get(instance), equalTo(newIcq));
    }

    @Test
    void testPrivateObjectFieldAccess() throws NoSuchFieldException {
        val random = ThreadLocalRandom.current();

        val nut = "Oreshka" + random.nextInt();
        val instance = new Areg(1424, nut);
        val field = InvokeDynamicFieldWrapper.<Areg, String>from(Areg.class.getDeclaredField("nut"));

        // get
        assertThat(field.get(instance), equalTo(nut));
        // set
        val newNut = nut + random.nextInt();
        assertDoesNotThrow(() -> field.set(instance, newNut));
        assertThat(field.get(instance), equalTo(newNut));
    }

    @Test
    void testPrivateFinalPrimitiveFieldAccess() throws NoSuchFieldException {
        val random = ThreadLocalRandom.current();

        val instance = new Areg(53876, "Oreshishe");
        val field = InvokeDynamicFieldWrapper.<Areg, Integer>from(Areg.class.getDeclaredField("aregId"));

        // get
        assertThat(field.get(instance), equalTo(127));
        // set
        val newId = random.nextInt(128, Integer.MAX_VALUE);
        assertDoesNotThrow(() -> field.set(instance, newId));
        assertThat(field.get(instance), equalTo(newId));
    }

    @Test
    void testPrivateFinalObjectFieldAccess() throws NoSuchFieldException {
        val random = ThreadLocalRandom.current();

        val instance = new Areg(52780, "Super nut");
        val field = InvokeDynamicFieldWrapper.<Areg, String>from(Areg.class.getDeclaredField("name"));

        // get
        assertThat(field.get(instance), equalTo("Mr Areshek"));
        // set
        val newName = "Mr. " + random.nextInt();
        assertDoesNotThrow(() -> field.set(instance, newName));
        assertThat(field.get(instance), equalTo(newName));
    }

    @AllArgsConstructor
    private static final class Areg {
        private int icq;
        private String nut;
        private final int aregId = 127;
        private final String name = "Mr Areshek";
    }
}