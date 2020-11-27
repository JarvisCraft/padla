package ru.progrm_jarvis.reflector.wrapper.invoke;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvokeConstructorWrapperTest {

    @Test
    void testInvokePrivateNoArgs() throws NoSuchMethodException {
        assertEquals(InvokeConstructorWrapper.from(Petya.class.getDeclaredConstructor()).invoke().args, 0);
    }

    @Test
    void testInvokePrivate1Arg() throws NoSuchMethodException {
        assertEquals(1, InvokeConstructorWrapper
                .from(Petya.class.getDeclaredConstructor(String.class))
                .invoke("Hello world")
                .args
        );
    }

    @Test
    void testInvokePrivate2Args() throws NoSuchMethodException {
        assertEquals(2, InvokeConstructorWrapper
                .from(Petya.class.getDeclaredConstructor(int.class, int.class))
                .invoke(23, 42)
                .args
        );
    }

    @Test
    void testInvokePrivate3Args() throws NoSuchMethodException {
        assertEquals(3, InvokeConstructorWrapper
                .from(Petya.class.getDeclaredConstructor(int.class, int.class, int.class))
                .invoke(743, 523, 24)
                .args
        );
    }

    @SuppressWarnings("unused")
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Petya {

        public final int args;

        private Petya() {
            this(0);
        }

        private Petya(final String first) {
            this(1);
        }

        private Petya(final int first, final int second) {
            this(2);
        }

        private Petya(final int first, final int second, final int third) {
            this(3);
        }
    }
}