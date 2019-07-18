package ru.progrm_jarvis.javacommons.collection;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionFactoryTest {

    @Test
    void testEmptyImmutableEnumSet() {
        val set = CollectionFactory.<TestEnum>createImmutableEnumSet();

        assertThat(set, empty());
        assertThat(set, hasSize(0));
        assertThat(set, emptyIterable());
        assertThat(set, iterableWithSize(0));
        // test immutability
        assertThrows(UnsupportedOperationException.class, () -> set.add(TestEnum.FOO));
        assertThat(set.remove(TestEnum.FOO), is(false));
    }

    @Test
    void testNonEmptyImmutableEnumSet() {
        val set = CollectionFactory.createImmutableEnumSet(TestEnum.BAR, TestEnum.BAZ, TestEnum.BAR);

        assertThat(set, not(empty()));
        assertThat(set, hasSize(2));
        assertThat(set, not(emptyIterable()));
        assertThat(set, iterableWithSize(2));
        // test immutability
        assertThrows(UnsupportedOperationException.class, () -> set.add(TestEnum.FOO));
        assertThrows(UnsupportedOperationException.class, () -> set.remove(TestEnum.FOO));
        assertThrows(UnsupportedOperationException.class, () -> set.add(TestEnum.BAR));
        assertThrows(UnsupportedOperationException.class, () -> set.remove(TestEnum.BAR));
        assertThrows(UnsupportedOperationException.class, () -> set.add(TestEnum.BAZ));
        assertThrows(UnsupportedOperationException.class, () -> set.remove(TestEnum.BAZ));
        // test containment
        assertThat(set, containsInAnyOrder(TestEnum.BAR, TestEnum.BAZ));
        assertTrue(set.contains(TestEnum.BAR));
        assertTrue(set.contains(TestEnum.BAZ));
        assertThat(set, not(contains(TestEnum.FOO)));
        // test equality (both sides)
        assertThat(new HashSet<>(Arrays.asList(TestEnum.BAR, TestEnum.BAZ)), equalTo(set));
        assertThat(set, equalTo(new HashSet<>(Arrays.asList(TestEnum.BAR, TestEnum.BAZ))));
        assertThat(set, equalTo(new ArrayList<>(Arrays.asList(TestEnum.BAR, TestEnum.BAZ))));
        // test to-array conversion
        assertThat(set.toArray(), anyOf( // although implementation uses natural order, check any array orders
                equalTo(new Object[]{TestEnum.BAR, TestEnum.BAZ}),
                equalTo(new Object[]{TestEnum.BAZ, TestEnum.BAR}))
        );
        //noinspection SuspiciousToArrayCall empty wrong-typed array
        assertThrows(ArrayStoreException.class, () -> set.toArray(new String[0]));
        //noinspection SuspiciousToArrayCall smaller wrong-typed array
        assertThrows(ArrayStoreException.class, () -> set.toArray(new String[1]));
        //noinspection SuspiciousToArrayCall same-sized wrong-typed array
        assertThrows(ArrayStoreException.class, () -> set.toArray(new String[2]));
        //noinspection SuspiciousToArrayCall bigger wrong-typed array
        assertThrows(ArrayStoreException.class, () -> set.toArray(new String[3]));
        { // empty array
            val array = new TestEnum[0];
            val newArray = set.toArray(array);

            assertThat(newArray, not(sameInstance(array)));
            assertThat(set.toArray(), anyOf( // although implementation uses natural order, check any array orders
                    equalTo(new Object[]{TestEnum.BAR, TestEnum.BAZ}),
                    equalTo(new Object[]{TestEnum.BAZ, TestEnum.BAR}))
            );
        }
        { // smaller array
            val array = new TestEnum[1];
            val newArray = set.toArray(array);

            assertThat(newArray, not(sameInstance(array)));
            assertThat(newArray, anyOf( // although implementation uses natural order, check any array orders
                    equalTo(new Object[]{TestEnum.BAR, TestEnum.BAZ}),
                    equalTo(new Object[]{TestEnum.BAZ, TestEnum.BAR}))
            );
        }
        { // same-sized array
            val array = new TestEnum[2];
            val newArray = set.toArray(array);

            assertThat(newArray, sameInstance(array));
            assertThat(newArray, anyOf( // although implementation uses natural order, check any array orders
                    equalTo(new Object[]{TestEnum.BAR, TestEnum.BAZ}),
                    equalTo(new Object[]{TestEnum.BAZ, TestEnum.BAR}))
            );
        }
        { // bigger array
            val array = new TestEnum[3];
            val newArray = set.toArray(array);

            assertThat(newArray, sameInstance(array));
            assertThat(newArray, anyOf( // although implementation uses natural order, check any array orders
                    equalTo(new Object[]{TestEnum.BAR, TestEnum.BAZ, null}),
                    equalTo(new Object[]{TestEnum.BAZ, TestEnum.BAR, null}))
            );
            assertThat(newArray[2], nullValue());
        }
    }

    @Test
    void testClassUnloading() {
        val set = new WeakReference<>(CollectionFactory
                .createImmutableEnumSet(TestEnum.BAR, TestEnum.BAZ, TestEnum.BAR)
        );
        System.out.println(set.get());
        while (set.get() != null) System.gc();
        CollectionFactory
                .createImmutableEnumSet(TestEnum.BAR, TestEnum.FOO);
    }

    public enum TestEnum {
        FOO, BAR,
        BAZ {
            @Override
            public int foo() {
                return ThreadLocalRandom.current().nextInt();
            }
        };

        @SuppressWarnings("unused") // added to fill enum with logic
        public int foo() {
            return 1;
        }
    }
}