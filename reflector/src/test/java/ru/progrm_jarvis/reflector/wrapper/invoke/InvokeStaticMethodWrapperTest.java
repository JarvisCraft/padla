package ru.progrm_jarvis.reflector.wrapper.invoke;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

class InvokeStaticMethodWrapperTest {

    @Test
    void testBoundVoidNoArgs() {
        final class Petya {
            private boolean called;

            private void call() {
                called = true;
            }
        }
        val instance = new Petya();

        assertDoesNotThrow(() -> InvokeStaticMethodWrapper
                .from(Petya.class.getDeclaredMethod("call"), instance)
                .invoke()
        );
        assertTrue(instance.called);
    }

    @Test
    void testBoundVoid1Arg() {
        val val1 = ThreadLocalRandom.current().nextInt();

        final class Petya {
            private boolean called;

            private void call(final int arg1) {
                assertEquals(val1, arg1);

                called = true;
            }
        }
        val instance = new Petya();

        assertDoesNotThrow(() -> InvokeStaticMethodWrapper
                .from(Petya.class.getDeclaredMethod("call", int.class), instance)
                .invoke(val1)
        );
        assertTrue(instance.called);
    }

    @Test
    void testBoundVoid2Args() {
        final int val1, val2;
        {
            val random = ThreadLocalRandom.current();
            val1 = random.nextInt();
            val2 = random.nextInt();
        }

        final class Petya {
            private boolean called;

            private void call(final int arg1, final int arg2) {
                assertEquals(val1, arg1);
                assertEquals(val2, arg2);

                called = true;
            }
        }
        val instance = new Petya();

        assertDoesNotThrow(() -> InvokeStaticMethodWrapper
                .from(Petya.class.getDeclaredMethod("call", int.class, int.class), instance)
                .invoke(val1, val2)
        );
        assertTrue(instance.called);
    }

    @Test
    void testBoundVoid3Args() {
        final int val1, val2, val3;
        {
            val random = ThreadLocalRandom.current();
            val1 = random.nextInt();
            val2 = random.nextInt();
            val3 = random.nextInt();
        }

        final class Petya {
            private boolean called;

            private void call(final int arg1, final int arg2, final int arg3) {
                assertEquals(val1, arg1);
                assertEquals(val2, arg2);
                assertEquals(val3, arg3);

                called = true;
            }
        }
        val instance = new Petya();

        assertDoesNotThrow(() -> InvokeStaticMethodWrapper
                .from(Petya.class.getDeclaredMethod("call", int.class, int.class, int.class), instance)
                .invoke(val1, val2, val3)
        );
        assertTrue(instance.called);
    }

    @Test
    void testBoundObjectNoArgs() throws NoSuchMethodException {
        val result = "Res0_" + ThreadLocalRandom.current().nextInt();
        final class Petya {
            private boolean called;

            private String call() {
                return result;
            }
        }
        val instance = new Petya();

        assertEquals(result, InvokeStaticMethodWrapper
                .from(Petya.class.getDeclaredMethod("call"), instance)
                .invoke()
        );
    }

    @Test
    void testBoundObject1Arg() throws NoSuchMethodException {
        final int val1;
        final String result;
        {
            val random = ThreadLocalRandom.current();
            val1 = random.nextInt();
            result = "Res1_" + random.nextInt();
        }

        final class Petya {
            private boolean called;

            private String call(final int arg1) {
                assertEquals(val1, arg1);

                return result;
            }
        }
        val instance = new Petya();

        assertEquals(result, InvokeStaticMethodWrapper
                .from(Petya.class.getDeclaredMethod("call", int.class), instance)
                .invoke(val1)
        );
    }

    @Test
    void testBoundObject2Args() throws NoSuchMethodException {
        final int val1, val2;
        final String result;
        {
            val random = ThreadLocalRandom.current();
            val1 = random.nextInt();
            val2 = random.nextInt();
            result = "Res2_" + random.nextInt();
        }

        class Petya {
            private String call(final int arg1, final int arg2) {
                assertEquals(val1, arg1);
                assertEquals(val2, arg2);

                return result;
            }
        }
        val instance = new Petya();

        assertEquals(result, InvokeStaticMethodWrapper
                .from(Petya.class.getDeclaredMethod("call", int.class, int.class), instance)
                .invoke(val1, val2)
        );
    }

    @Test
    void testBoundObject3Args() throws NoSuchMethodException {
        final int val1, val2, val3;
        final String result;
        {
            val random = ThreadLocalRandom.current();
            val1 = random.nextInt();
            val2 = random.nextInt();
            val3 = random.nextInt();
            result = "Res2_" + random.nextInt();
        }

        class Petya {
            private String call(final int arg1, final int arg2, final int arg3) {
                assertEquals(val1, arg1);
                assertEquals(val2, arg2);
                assertEquals(val3, arg3);

                return result;
            }
        }
        val instance = new Petya();

        assertEquals(result, InvokeStaticMethodWrapper
                .from(Petya.class.getDeclaredMethod("call", int.class, int.class, int.class), instance)
                .invoke(val1, val2, val3)
        );
    }

    @Test
    void testStaticVoidNoArgs() {
        assertDoesNotThrow(() -> InvokeStaticMethodWrapper
                .from(Void0StaticPetya.class.getDeclaredMethod("call"))
                .invoke()
        );
        assertTrue(Void0StaticPetya.called);
    }

    @Test
    void testStaticVoid1Arg() {
        val val1 = Void1StaticPetya.val1 = ThreadLocalRandom.current().nextInt();

        assertDoesNotThrow(() -> InvokeStaticMethodWrapper
                .from(Void1StaticPetya.class.getDeclaredMethod("call", int.class))
                .invoke(val1)
        );
        assertTrue(Void1StaticPetya.called);
    }

    @Test
    void testStaticVoid2Args() {
        final int val1, val2;
        {
            val random = ThreadLocalRandom.current();
            val1 = Void2StaticPetya.val1 = random.nextInt();
            val2 = Void2StaticPetya.val2 = random.nextInt();
        }

        assertDoesNotThrow(() -> InvokeStaticMethodWrapper
                .from(Void2StaticPetya.class.getDeclaredMethod("call", int.class, int.class))
                .invoke(val1, val2)
        );
        assertTrue(Void2StaticPetya.called);
    }

    @Test
    void testStaticVoid3Args() {
        final int val1, val2, val3;
        {
            val random = ThreadLocalRandom.current();
            val1 = Void3StaticPetya.val1 = random.nextInt();
            val2 = Void3StaticPetya.val2 = random.nextInt();
            val3 = Void3StaticPetya.val3 = random.nextInt();
        }

        assertDoesNotThrow(() -> InvokeStaticMethodWrapper
                .from(Void3StaticPetya.class.getDeclaredMethod("call", int.class, int.class, int.class))
                .invoke(val1, val2, val3)
        );
        assertTrue(Void3StaticPetya.called);
    }

    @Test
    void testStaticObjectNoArgs() throws NoSuchMethodException {
        final String result = Object0StaticPetya.result = "Res0_" + ThreadLocalRandom.current().nextInt();

        assertEquals(result, InvokeStaticMethodWrapper
                .from(Object0StaticPetya.class.getDeclaredMethod("call"))
                .invoke(), result
        );
    }

    @Test
    void testStaticObject1Arg() throws NoSuchMethodException {
        final int val1;
        final String result;
        {
            val random = ThreadLocalRandom.current();
            val1 = Object1StaticPetya.val1 = random.nextInt();
            result = Object1StaticPetya.result = "Res1_" + random.nextInt();
        }

        assertEquals(result, InvokeStaticMethodWrapper
                .from(Object1StaticPetya.class.getDeclaredMethod("call", int.class))
                .invoke(val1)
        );
    }

    @Test
    void testStaticObject2Args() throws NoSuchMethodException {
        final int val1, val2;
        final String result;
        {
            val random = ThreadLocalRandom.current();
            val1 = Object2StaticPetya.val1 = random.nextInt();
            val2 = Object2StaticPetya.val2 = random.nextInt();
            result = Object2StaticPetya.result = "Res2_" + random.nextInt();
        }

        assertEquals(result, InvokeStaticMethodWrapper
                .from(Object2StaticPetya.class.getDeclaredMethod("call", int.class, int.class))
                .invoke(val1, val2)
        );
    }

    @Test
    void testStaticObject3Args() throws NoSuchMethodException {
        final int val1, val2, val3;
        final String result;
        {
            val random = ThreadLocalRandom.current();
            val1 = Object3StaticPetya.val1 = random.nextInt();
            val2 = Object3StaticPetya.val2 = random.nextInt();
            val3 = Object3StaticPetya.val3 = random.nextInt();
            result = Object3StaticPetya.result = "Res3_" + random.nextInt();
        }

        assertEquals(result, InvokeStaticMethodWrapper
                .from(Object3StaticPetya.class.getDeclaredMethod("call", int.class, int.class, int.class))
                .invoke(val1, val2, val3)
        );
    }

    @UtilityClass // static (everything) + final (class) + no constructor
    private class Void0StaticPetya {

        private boolean called;

        private void call() {
            called = true;
        }
    }

    @UtilityClass // static (everything) + final (class) + no constructor
    private class Void1StaticPetya {

        private int val1;
        private boolean called;

        private void call(final int arg1) {
            assertEquals(val1, arg1);

            called = true;
        }
    }

    @UtilityClass // static (everything) + final (class) + no constructor
    private class Void2StaticPetya {

        private int val1, val2;
        private boolean called;

        private void call(final int arg1, final int arg2) {
            assertEquals(val1, arg1);
            assertEquals(val2, arg2);

            called = true;
        }
    }

    @UtilityClass // static (everything) + final (class) + no constructor
    private class Void3StaticPetya {

        private boolean called;
        private int val1, val2, val3;

        private void call(final int arg1, final int arg2, final int arg3) {
            assertEquals(val1, arg1);
            assertEquals(val2, arg2);
            assertEquals(val3, arg3);

            called = true;
        }
    }

    @UtilityClass // static (everything) + final (class) + no constructor
    private class Object0StaticPetya {

        private String result;

        private String call() {
            return result;
        }
    }

    @UtilityClass // static (everything) + final (class) + no constructor
    private class Object1StaticPetya {

        private int val1;
        private String result;

        private String call(final int arg1) {
            assertEquals(val1, arg1);

            return result;
        }
    }

    @UtilityClass // static (everything) + final (class) + no constructor
    private class Object2StaticPetya {

        private int val1, val2;
        private String result;

        private String call(final int arg1, final int arg2) {
            assertEquals(val1, arg1);
            assertEquals(val2, arg2);

            return result;
        }
    }

    @UtilityClass // static (everything) + final (class) + no constructor
    private class Object3StaticPetya {

        private int val1, val2, val3;
        private String result;

        private String call(final int arg1, final int arg2, final int arg3) {
            assertEquals(val1, arg1);
            assertEquals(val2, arg2);
            assertEquals(val3, arg3);

            return result;
        }
    }
}