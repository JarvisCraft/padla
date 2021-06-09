package ru.progrm_jarvis.javacommons.recursion;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

class RecursionsTest {

    private static @NotNull Function<
            ? super @NotNull Class<?>, @NotNull Stream<? extends @NotNull Class<?>>
            > classToParentsFunction() {
        return clazz -> {
            final Class<?> superClass;
            return Stream.concat(
                    (superClass = clazz.getSuperclass()) == null
                            ? Stream.empty() : Stream.of(superClass),
                    Arrays.stream(clazz.getInterfaces())
            );
        };
    }

    private static @NotNull Function<
            ? super @NotNull Class<?>, @NotNull Stream<? extends @NotNull Method>
            > classToMethodStreamFunction() {
        return clazz -> Arrays.stream(clazz.getDeclaredMethods());
    }

    @Test
    void testRecurseClassHierarchy() {
        assertThat(
                Recursions.recurse(TestSubjects.D.class, classToParentsFunction(), classToMethodStreamFunction())
                        .collect(Collectors.toCollection(HashSet::new)),
                containsInAnyOrder(Stream.of(
                        Object.class.getDeclaredMethods(),
                        TestSubjects.A.class.getDeclaredMethods(),
                        TestSubjects.B.class.getDeclaredMethods(),
                        TestSubjects.C.class.getDeclaredMethods(),
                        TestSubjects.D.class.getDeclaredMethods()
                ).flatMap(Arrays::stream).distinct().toArray())
        );
    }

    private static final class TestSubjects {

        private static class A {
            void foo() {}
        }

        private interface B {
            void fooB();

            void barB();
        }

        @FunctionalInterface // why not
        private interface C {
            void fooC();
        }

        private static class D extends A implements B, C {
            public void d() {}

            @Override
            public void fooB() {}

            @Override
            public void barB() {}

            @Override
            public void fooC() {}
        }
    }
}