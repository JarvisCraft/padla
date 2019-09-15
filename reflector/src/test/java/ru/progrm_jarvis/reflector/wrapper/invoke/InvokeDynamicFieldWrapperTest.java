package ru.progrm_jarvis.reflector.wrapper.invoke;

import lombok.AllArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
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
        assertThat(field.get(instance), allOf(equalTo(icq), equalTo(instance.icq)));
        // set
        val newIcq = icq + random.nextInt(1, Integer.MAX_VALUE);
        //noinspection RedundantCast Java 11 issue
        assertDoesNotThrow((Executable) () -> field.set(instance, newIcq));
        assertThat(field.get(instance), allOf(equalTo(newIcq), equalTo(instance.icq)));
    }

    @Test
    void testPrivateObjectFieldAccess() throws NoSuchFieldException {
        val random = ThreadLocalRandom.current();

        val nut = "Oreshka" + random.nextInt();
        val instance = new Areg(1424, nut);
        val field = InvokeDynamicFieldWrapper.<Areg, String>from(Areg.class.getDeclaredField("nut"));

        // get
        assertThat(field.get(instance), allOf(equalTo(nut), equalTo(instance.nut)));
        // set
        val newNut = nut + random.nextInt();
        //noinspection RedundantCast Java 11 issue
        assertDoesNotThrow((Executable) () -> field.set(instance, newNut));
        assertThat(field.get(instance), allOf(equalTo(newNut), equalTo(instance.nut)));
    }

    @Test
    void testPrivateFinalPrimitiveFieldAccess() throws NoSuchFieldException {
        val random = ThreadLocalRandom.current();

        val instance = new Areg(53876, "Oreshishe");
        val field = InvokeDynamicFieldWrapper.<Areg, Integer>from(Areg.class.getDeclaredField("id"));

        // get
        assertThat(field.get(instance), allOf(equalTo(127), equalTo(instance.id)));
        // set
        val newId = random.nextInt(128, Integer.MAX_VALUE);
        //noinspection RedundantCast Java 11 issue
        assertDoesNotThrow((Executable) () -> field.set(instance, newId));
        // value gets cached (?)
        assertThat(field.get(instance), /*allOf(*/equalTo(newId)/*, equalTo(instance.id))*/);
    }

    @Test
    void testPrivateFinalObjectFieldAccess() throws NoSuchFieldException {
        val random = ThreadLocalRandom.current();

        val instance = new Areg(52780, "Super nut");
        val field = InvokeDynamicFieldWrapper.<Areg, String>from(Areg.class.getDeclaredField("name"));

        // get
        assertThat(field.get(instance), allOf(equalTo("Mr Areshek"), equalTo(instance.name)));
        // set
        val newName = "Mr. " + random.nextInt();
        //noinspection RedundantCast Java 11 issue
        assertDoesNotThrow((Executable) () -> field.set(instance, newName));
        // value gets cached (?)
        assertThat(field.get(instance), /*allOf(*/equalTo(newName)/*, equalTo(instance.name))*/);
    }

    @AllArgsConstructor
    private static final class Areg {
        private int icq;
        private String nut;
        private final int id = 127;
        private final String name = "Mr Areshek";
    }
}