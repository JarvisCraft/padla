package ru.progrm_jarvis.reflector.wrapper.invoke;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class InvokeDynamicMethodWrapperTest {

    @Test
    void testVoidNoArgs() {
        final class Petya {
            private boolean called;
            private void call() {
                called = true;
            }
        }
        val instance = new Petya();

        assertDoesNotThrow(() -> InvokeDynamicMethodWrapper
                .from(Petya.class.getDeclaredMethod("call"))
                .invoke(instance)
        );
        assertThat(instance.called, is(true));
    }

    @Test
    void testVoid1Arg() {
        val val1 = ThreadLocalRandom.current().nextInt();

        final class Petya {
            private boolean called;
            private void call(int arg1) {
                assertThat(arg1, is(val1));

                called = true;
            }
        }
        val instance = new Petya();

        assertDoesNotThrow(() -> InvokeDynamicMethodWrapper
                .from(Petya.class.getDeclaredMethod("call", int.class))
                .invoke(instance, val1)
        );
        assertThat(instance.called, is(true));
    }

    @Test
    void testVoid2Args() {
        final int val1, val2;
        {
            val random = ThreadLocalRandom.current();
            val1 = random.nextInt();
            val2 = random.nextInt();
        }

        final class Petya {
            private boolean called;
            private void call(int arg1, int arg2) {
                assertThat(arg1, is(val1));
                assertThat(arg2, is(val2));

                called = true;
            }
        }
        val instance = new Petya();

        assertDoesNotThrow(() -> InvokeDynamicMethodWrapper
                .from(Petya.class.getDeclaredMethod("call", int.class, int.class))
                .invoke(instance, val1, val2)
        );
        assertThat(instance.called, is(true));
    }

    @Test
    void testObjectNoArgs() throws NoSuchMethodException {
        val result = "Res0_" + ThreadLocalRandom.current().nextInt();
        final class Petya {
            private boolean called;
            private String call() {
                return result;
            }
        }
        val instance = new Petya();

        assertThat(InvokeDynamicMethodWrapper
                .from(Petya.class.getDeclaredMethod("call"))
                .invoke(instance), equalTo(result)
        );
    }

    @Test
    void testObject1Arg() throws NoSuchMethodException {
        final int val1;
        final String result;
        {
            val random = ThreadLocalRandom.current();
            val1 = random.nextInt();
            result = "Res1_" + random.nextInt();
        }

        final class Petya {
            private boolean called;
            private String call(int arg1) {
                assertThat(arg1, is(val1));

                return result;
            }
        }
        val instance = new Petya();

        assertThat(InvokeDynamicMethodWrapper
                .from(Petya.class.getDeclaredMethod("call", int.class))
                .invoke(instance, val1), equalTo(result)
        );
    }

    @Test
    void testObject2Args() throws NoSuchMethodException {
        final int val1, val2;
        final String result;
        {
            val random = ThreadLocalRandom.current();
            val1 = random.nextInt();
            val2 = random.nextInt();
            result = "Res2_" + random.nextInt();
        }

        class Petya {
            private String call(int arg1, int arg2) {
                assertThat(arg1, is(val1));
                assertThat(arg2, is(val2));

                return result;
            }
        }
        val instance = new Petya();

        assertThat(InvokeDynamicMethodWrapper
                .from(Petya.class.getDeclaredMethod("call", int.class, int.class))
                .invoke(instance, val1, val2), equalTo(result)
        );
    }
}