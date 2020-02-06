package ru.progrm_jarvis.javacommons.invoke;

import lombok.NonNull;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class SimpleInvokeFactoryTest {

    @Test
    void testStatic() throws Throwable {
        assertThat(
                SimpleInvokeFactory
                        .<Function<Integer, String>, SociophobicClass>newInstance()
                        .using(SociophobicClass.LOOKUP_FACTORY)
                        .implementing(Function.class)
                        .via(SociophobicClass.class.getDeclaredMethod("bar", int.class))
                        .create().apply(123),
                equalTo("bar-123")
        );
    }

    @Test
    void testNotBound() throws Throwable {

        assertThat(
                SimpleInvokeFactory
                        .<BiFunction<SociophobicClass, Integer, String>, SociophobicClass>newInstance()
                        .using(SociophobicClass.LOOKUP_FACTORY)
                        .implementing(BiFunction.class)
                        .via(SociophobicClass.class.getDeclaredMethod("foo", int.class))
                        .create().apply(new SociophobicClass(), 123),
                equalTo("foo-123")
        );
    }

    @Test
    void testBound() throws Throwable {
        assertThat(
                SimpleInvokeFactory
                        .<Function<Integer, String>, SociophobicClass>newInstance()
                        .using(SociophobicClass.LOOKUP_FACTORY)
                        .implementing(Function.class)
                        .via(SociophobicClass.class.getDeclaredMethod("foo", int.class))
                        .boundTo(new SociophobicClass())
                        .create().apply(456),
                equalTo("foo-456")
        );
    }

    private static final class SociophobicClass {

        @NonNull private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
        @NonNull private static final LookupFactory LOOKUP_FACTORY = clazz -> LOOKUP;

        public String foo(int number) {
            return "foo-" + number;
        }

        public static String bar(int number) {
            return "bar-" + number;
        }
    }
}