package ru.progrm_jarvis.reflector.wrapper.invoke;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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
        assertThat(instance.called, is(true));
    }

    @Test
    void testBoundVoid1Arg() {
        val val1 = ThreadLocalRandom.current().nextInt();

        final class Petya {
            private boolean called;
            private void call(final int arg1) {
                assertThat(arg1, is(val1));

                called = true;
            }
        }
        val instance = new Petya();

        assertDoesNotThrow(() -> InvokeStaticMethodWrapper
                .from(Petya.class.getDeclaredMethod("call", int.class), instance)
                .invoke(val1)
        );
        assertThat(instance.called, is(true));
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
                assertThat(arg1, is(val1));
                assertThat(arg2, is(val2));

                called = true;
            }
        }
        val instance = new Petya();

        assertDoesNotThrow(() -> InvokeStaticMethodWrapper
                .from(Petya.class.getDeclaredMethod("call", int.class, int.class), instance)
                .invoke(val1, val2)
        );
        assertThat(instance.called, is(true));
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
                assertThat(arg1, is(val1));
                assertThat(arg2, is(val2));
                assertThat(arg3, is(val3));

                called = true;
            }
        }
        val instance = new Petya();

        assertDoesNotThrow(() -> InvokeStaticMethodWrapper
                .from(Petya.class.getDeclaredMethod("call", int.class, int.class, int.class), instance)
                .invoke(val1, val2, val3)
        );
        assertThat(instance.called, is(true));
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

        assertThat(InvokeStaticMethodWrapper
                .from(Petya.class.getDeclaredMethod("call"), instance)
                .invoke(), equalTo(result)
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
                assertThat(arg1, is(val1));

                return result;
            }
        }
        val instance = new Petya();

        assertThat(InvokeStaticMethodWrapper
                .from(Petya.class.getDeclaredMethod("call", int.class), instance)
                .invoke(val1), equalTo(result)
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
                assertThat(arg1, is(val1));
                assertThat(arg2, is(val2));

                return result;
            }
        }
        val instance = new Petya();

        assertThat(InvokeStaticMethodWrapper
                .from(Petya.class.getDeclaredMethod("call", int.class, int.class), instance)
                .invoke(val1, val2), equalTo(result)
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
                assertThat(arg1, is(val1));
                assertThat(arg2, is(val2));
                assertThat(arg3, is(val3));

                return result;
            }
        }
        val instance = new Petya();

        assertThat(InvokeStaticMethodWrapper
                .from(Petya.class.getDeclaredMethod("call", int.class, int.class, int.class), instance)
                .invoke(val1, val2, val3), equalTo(result)
        );
    }

    @Test
    void testStaticVoidNoArgs() {
        assertDoesNotThrow(() -> InvokeStaticMethodWrapper
                .from(Void0StaticPetya.class.getDeclaredMethod("call"))
                .invoke()
        );
        assertThat(Void0StaticPetya.called, is(true));
    }

    @Test
    void testStaticVoid1Arg() {
        val val1 = Void1StaticPetya.val1 = ThreadLocalRandom.current().nextInt();

        assertDoesNotThrow(() -> InvokeStaticMethodWrapper
                .from(Void1StaticPetya.class.getDeclaredMethod("call", int.class))
                .invoke(val1)
        );
        assertThat(Void1StaticPetya.called, is(true));
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
        assertThat(Void2StaticPetya.called, is(true));
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
        assertThat(Void3StaticPetya.called, is(true));
    }

    @Test
    void testStaticObjectNoArgs() throws NoSuchMethodException {
        final String result = Object0StaticPetya.result = "Res0_" + ThreadLocalRandom.current().nextInt();

        assertThat(InvokeStaticMethodWrapper
                .from(Object0StaticPetya.class.getDeclaredMethod("call"))
                .invoke(), equalTo(result)
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

        assertThat(InvokeStaticMethodWrapper
                .from(Object1StaticPetya.class.getDeclaredMethod("call", int.class))
                .invoke(val1), equalTo(result)
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

        assertThat(InvokeStaticMethodWrapper
                .from(Object2StaticPetya.class.getDeclaredMethod("call", int.class, int.class))
                .invoke(val1, val2), equalTo(result)
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

        assertThat(InvokeStaticMethodWrapper
                .from(Object3StaticPetya.class.getDeclaredMethod("call", int.class, int.class, int.class))
                .invoke(val1, val2, val3), equalTo(result)
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
            assertThat(arg1, is(val1));

            called = true;
        }
    }

    @UtilityClass // static (everything) + final (class) + no constructor
    private class Void2StaticPetya {

        private int val1, val2;
        private boolean called;

        private void call(final int arg1, final int arg2) {
            assertThat(arg1, is(val1));
            assertThat(arg2, is(val2));

            called = true;
        }
    }

    @UtilityClass // static (everything) + final (class) + no constructor
    private class Void3StaticPetya {

        private boolean called;
        private int val1, val2, val3;

        private void call(final int arg1, final int arg2, final int arg3) {
            assertThat(arg1, is(val1));
            assertThat(arg2, is(val2));
            assertThat(arg3, is(val3));

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
            assertThat(arg1, is(val1));

            return result;
        }
    }

    @UtilityClass // static (everything) + final (class) + no constructor
    private class Object2StaticPetya {

        private int val1, val2;
        private String result;

        private String call(final int arg1, final int arg2) {
            assertThat(arg1, is(val1));
            assertThat(arg2, is(val2));

            return result;
        }
    }

    @UtilityClass // static (everything) + final (class) + no constructor
    private class Object3StaticPetya {

        private int val1, val2, val3;
        private String result;

        private String call(final int arg1, final int arg2, final int arg3) {
            assertThat(arg1, is(val1));
            assertThat(arg2, is(val2));
            assertThat(arg3, is(val3));

            return result;
        }
    }
}