/*
 * Copyright 2019 Feather Core
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.progrm_jarvis.reflector.invoke;

import lombok.EqualsAndHashCode;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class InvokeUtilTest {

    @Test
    void testToStaticRunnable() {
        assertDoesNotThrow(
                () -> InvokeUtil.toStaticRunnable(TestClass.class.getDeclaredMethod("staticVoidMethod")).run()
        );
        assertDoesNotThrow(
                () -> InvokeUtil.toStaticRunnable(TestClass.class.getDeclaredMethod("staticStringMethod")).run()
        );
    }

    @Test
    void testToBoundRunnable() {
        assertDoesNotThrow(() -> InvokeUtil.toBoundRunnable(
                TestClass.class.getDeclaredMethod("nonStaticStringMethod"), new TestClass()
                ).run()
        );
        assertDoesNotThrow(() -> InvokeUtil.toBoundRunnable(
                TestClass.class.getDeclaredMethod("nonStaticVoidMethod"), new TestClass()
                ).run()
        );
    }

    @Test
    void testToStaticSupplier() throws NoSuchMethodException {
        assertThat(
                InvokeUtil.toStaticSupplier(TestClass.class.getDeclaredMethod("staticStringMethod")).get(),
                equalTo("hi")
        );
    }

    @Test
    void testToBoundSupplier() throws NoSuchMethodException {
        assertThat(
                InvokeUtil.toBoundSupplier(
                        TestClass.class.getDeclaredMethod("nonStaticStringMethod"), new TestClass()
                ).get(),
                equalTo("bro")
        );
    }

    @Test
    void testToSupplier() throws NoSuchMethodException {
        assertThat(
                InvokeUtil.toSupplier(TestClass.class.getConstructor()).get(),
                equalTo(new TestClass())
        );
    }

    @Test
    void testToStaticGetterSupplier() throws NoSuchFieldException {
        assertThat(
                InvokeUtil.toStaticGetterSupplier(TestClass.class.getDeclaredField("privateStaticFinalIntField")).get(),
                equalTo(777)
        );
    }

    @Test
    void testToBoundGetterSupplier() throws NoSuchFieldException {
        val instance = new TestClass();
        val value = instance.privateIntField = ThreadLocalRandom.current().nextInt();
        assertThat(
                InvokeUtil.toBoundGetterSupplier(TestClass.class.getDeclaredField("privateIntField"), instance).get(),
                equalTo(value)
        );
    }

    @Test
    void testToGetterFunction() throws NoSuchFieldException {
        val instance = new TestClass();
        val value = instance.privateIntField = ThreadLocalRandom.current().nextInt();
        assertThat(
                InvokeUtil.toGetterFunction(TestClass.class.getDeclaredField("privateIntField")).apply(instance),
                equalTo(value)
        );
    }

    @Test
    void testToStaticSetterConsumer() throws NoSuchFieldException {
        val value = ThreadLocalRandom.current().nextInt();
        InvokeUtil.toStaticSetterConsumer(TestClass.class.getDeclaredField("privateStaticIntField")).accept(value);
        assertThat(TestClass.privateStaticIntField, equalTo(value));
    }

    @Test
    void testToBoundSetterConsumer() throws NoSuchFieldException {
        val instance = new TestClass();
        val value = ThreadLocalRandom.current().nextInt();
        InvokeUtil.toBoundSetterConsumer(TestClass.class.getDeclaredField("privateIntField"), instance).accept(value);
        assertThat(instance.privateIntField, equalTo(value));
    }

    @Test
    void testToSetterBiConsumer() throws NoSuchFieldException {
        val instance = new TestClass();
        val value = ThreadLocalRandom.current().nextInt();
        InvokeUtil.toSetterBiConsumer(TestClass.class.getDeclaredField("privateIntField")).accept(instance, value);
        assertThat(instance.privateIntField, equalTo(value));
    }

    @EqualsAndHashCode // to test constructors
    public static class TestClass {

        private static final int privateStaticFinalIntField = 777;

        private static int privateStaticIntField = 345;

        private int privateIntField = 1337;

        private static void staticVoidMethod() {}

        private static String staticStringMethod() {
            return "hi";
        }

        private void nonStaticVoidMethod() {}

        private String nonStaticStringMethod() {
            return "bro";
        }
    }
}