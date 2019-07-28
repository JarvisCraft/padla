package ru.progrm_jarvis.reflector.wrapper.invoke;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class InvokeConstructorWrapperTest {

    @Test
    void testInvokePrivateNoArgs() throws NoSuchMethodException {
        assertThat(
                InvokeConstructorWrapper.from(Petya.class.getDeclaredConstructor())
                        .invoke().args, equalTo(0)
        );
    }

    @Test
    void testInvokePrivate1Arg() throws NoSuchMethodException {
        assertThat(
                InvokeConstructorWrapper.from(Petya.class.getDeclaredConstructor(String.class))
                        .invoke("Hello world").args, equalTo(1)
        );
    }

    @Test
    void testInvokePrivate2Args() throws NoSuchMethodException {
        assertThat(
                InvokeConstructorWrapper.from(Petya.class.getDeclaredConstructor(int.class, int.class))
                        .invoke(23, 42).args, equalTo(2)
        );
    }

    @Test
    void testInvokePrivate3Args() throws NoSuchMethodException {
        assertThat(
                InvokeConstructorWrapper.from(Petya.class.getDeclaredConstructor(int.class, int.class, int.class))
                        .invoke(743, 523, 24).args, equalTo(3)
        );
    }

    @SuppressWarnings("unused")
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Petya {

        public final int args;

        private Petya() {
            this(0);
        }

        private Petya(final String a) {
            this(1);
        }

        private Petya(final int a, final int b) {
            this(2);
        }

        private Petya(final int a, final int b, final int c) {
            this(3);
        }
    }
}