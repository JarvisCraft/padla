package ru.progrm_jarvis.reflector.wrapper.invoke;

import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class InvokeStaticFieldWrapperTest {

    @Test
    void testBoundPrivatePrimitiveFieldAccess() throws NoSuchFieldException {
        val random = ThreadLocalRandom.current();

        val icq = random.nextInt();
        val instance = new Areg(icq, "Oreshek");
        val field = InvokeStaticFieldWrapper.<Areg, Integer>from(Areg.class.getDeclaredField("icq"), instance);

        // get
        assertThat(field.get(), allOf(equalTo(icq), equalTo(instance.icq)));
        // set
        val newIcq = icq + random.nextInt(1, Integer.MAX_VALUE);
        //noinspection RedundantCast Java 11 issue
        assertDoesNotThrow((Executable) () -> field.set(newIcq));
        assertThat(field.get(), allOf(equalTo(newIcq), equalTo(instance.icq)));
    }

    @Test
    void testBoundPrivateObjectFieldAccess() throws NoSuchFieldException {
        val random = ThreadLocalRandom.current();

        val nut = "Oreshka" + random.nextInt();
        val instance = new Areg(1424, nut);
        val field = InvokeStaticFieldWrapper.<Areg, String>from(Areg.class.getDeclaredField("nut"), instance);

        // get
        assertThat(field.get(), allOf(equalTo(nut), equalTo(instance.nut)));
        // set
        val newNut = nut + random.nextInt();
        //noinspection RedundantCast Java 11 issue
        assertDoesNotThrow((Executable) () ->field.set(newNut));
        assertThat(field.get(), allOf(equalTo(newNut), equalTo(instance.nut)));
    }

    @Test
    void testBoundPrivateFinalPrimitiveFieldAccess() throws NoSuchFieldException {
        val random = ThreadLocalRandom.current();

        val instance = new Areg(53876, "Oreshishe");
        val field = InvokeStaticFieldWrapper.<Areg, Integer>from(Areg.class.getDeclaredField("id"), instance);

        // get
        assertThat(field.get(), allOf(equalTo(127), equalTo(instance.id)));
        // set
        val newId = random.nextInt(128, Integer.MAX_VALUE);
        //noinspection RedundantCast Java 11 issue
        assertDoesNotThrow((Executable) () ->field.set(newId));
        // value gets cached (?)
        assertThat(field.get(), /*allOf(*/equalTo(newId)/*, equalTo(instance.id))*/);
    }

    @Test
    void testBoundPrivateFinalObjectFieldAccess() throws NoSuchFieldException {
        val random = ThreadLocalRandom.current();

        val instance = new Areg(52780, "Super nut");
        val field = InvokeStaticFieldWrapper.<Areg, String>from(Areg.class.getDeclaredField("name"), instance);

        // get
        assertThat(field.get(), allOf(equalTo("Mr Areshek"), equalTo(instance.name)));
        // set
        val newName = "Mr. " + random.nextInt();
        //noinspection RedundantCast Java 11 issue
        assertDoesNotThrow((Executable) () ->field.set(newName));
        // value gets cached (?)
        assertThat(field.get(), /*allOf(*/equalTo(newName)/*, equalTo(instance.name))*/);
    }

    @Test
    void testPrivateStaticPrimitiveFieldAccess() throws NoSuchFieldException {
        val random = ThreadLocalRandom.current();

        val icq = random.nextInt();
        StaticAreg.icq = icq;
        val field = InvokeStaticFieldWrapper.<Areg, Integer>from(StaticAreg.class.getDeclaredField("icq"));

        // get
        assertThat(field.get(), allOf(equalTo(icq), equalTo(StaticAreg.icq)));
        // set
        val newIcq = icq + random.nextInt(1, Integer.MAX_VALUE);
        //noinspection RedundantCast Java 11 issue
        assertDoesNotThrow((Executable) () ->field.set(newIcq));
        assertThat(field.get(), allOf(equalTo(newIcq), equalTo(StaticAreg.icq)));
    }

    @Test
    void testPrivateStaticObjectFieldAccess() throws NoSuchFieldException {
        val random = ThreadLocalRandom.current();

        val nut = "Oreshka" + random.nextInt();
        StaticAreg.nut = nut;
        val field = InvokeStaticFieldWrapper.<Areg, String>from(StaticAreg.class.getDeclaredField("nut"));

        // get
        assertThat(field.get(), allOf(equalTo(nut), equalTo(StaticAreg.nut)));
        // set
        val newNut = nut + random.nextInt();
        //noinspection RedundantCast Java 11 issue
        assertDoesNotThrow((Executable) () ->field.set(newNut));
        assertThat(field.get(), allOf(equalTo(newNut), equalTo(StaticAreg.nut)));
    }

    @Test
    void testPrivateStaticFinalPrimitiveFieldAccess() throws NoSuchFieldException {
        val random = ThreadLocalRandom.current();

        val field = InvokeStaticFieldWrapper.<Areg, Integer>from(StaticAreg.class.getDeclaredField("id"));

        // get
        assertThat(field.get(), allOf(equalTo(127), equalTo(StaticAreg.id)));
        // set
        val newId = random.nextInt(128, Integer.MAX_VALUE);
        //noinspection RedundantCast Java 11 issue
        assertDoesNotThrow((Executable) () ->field.set(newId));
        // value gets cached (?)
        assertThat(field.get(), /*allOf(*/equalTo(newId)/*, equalTo(StaticAreg.id))*/);
    }

    @Test
    void testPrivateStaticFinalObjectFieldAccess() throws NoSuchFieldException {
        val random = ThreadLocalRandom.current();

        val field = InvokeStaticFieldWrapper.<Areg, String>from(StaticAreg.class.getDeclaredField("name"));

        // get
        assertThat(field.get(), allOf(equalTo("Mr Areshek"), equalTo(StaticAreg.name)));
        // set
        val newName = "Mr. " + random.nextInt();
        //noinspection RedundantCast Java 11 issue
        assertDoesNotThrow((Executable) () ->field.set(newName));
        // value gets cached (?)
        assertThat(field.get(), /*allOf(*/equalTo(newName)/*, equalTo(StaticAreg.name))*/);
    }

    @AllArgsConstructor
    private static final class Areg {
        private int icq;
        private String nut;
        private final int id = 127;
        private final String name = "Mr Areshek";
    }

    @UtilityClass // static (everything) + final (class) + no constructor
    private class StaticAreg {
        private int icq;
        private String nut;
        private final int id = 127;
        private final String name = "Mr Areshek";
    }
}