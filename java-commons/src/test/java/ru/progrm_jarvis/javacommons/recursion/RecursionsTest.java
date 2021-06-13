package ru.progrm_jarvis.javacommons.recursion;

import lombok.val;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.*;

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
    void testRecurse_classHierarchy() {
        assertThat(
                Recursions.recurse(TestSubjects.D.class, classToParentsFunction())
                        .flatMap(classToMethodStreamFunction())
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

    @Test
    void testRecurseFully_classHierarchy() {
        assertThat(
                Recursions.recurseFully(TestSubjects.D.class, classToParentsFunction(), classToMethodStreamFunction())
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

    @Test
    void testRecurse_classHierarchy_LazyEnough() throws NoSuchMethodException {
        var digger = classToParentsFunction();
        digger = mock(Function.class, delegatesTo(digger));

        val searchedMethod = TestSubjects.D.class.getDeclaredMethod("d");
        assertEquals(
                Optional.of(searchedMethod),
                Recursions.recurse(TestSubjects.D.class, digger)
                        .flatMap(classToMethodStreamFunction())
                        .filter(method -> method.equals(searchedMethod))
                        .findAny()
        );

        verify(digger, never()).apply(any());
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