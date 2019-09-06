package ru.progrm_jarvis.javacommons.classload;

import javassist.*;
import lombok.val;
import lombok.var;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClassFactoryTest {

    @Test
    @EnabledIfSystemProperty(named = "test.gc.always-respects-System.gc()", matches = "true|yes|\\+|1|enabled")
    void testDefineGCClass() throws IOException, CannotCompileException, IllegalAccessException,
            InstantiationException, NoSuchMethodException, InvocationTargetException {
        val clazz = ClassPool.getDefault().makeClass("foo.bar.Baz");
        clazz.addMethod(CtNewMethod.make("public int foo() {return 123;}", clazz));
        clazz.addConstructor(CtNewConstructor.make(new CtClass[0], new CtClass[0], clazz));

        val className = clazz.getName();

        final WeakReference<Class<?>> classRef;
        {
            var newClass = GcClassDefiners.getDefault()
                    .orElseThrow(() -> new IllegalStateException("GC-ClassDefiner is unavailable"))
                    .defineClass(MethodHandles.lookup(), className, clazz.toBytecode());
            classRef = new WeakReference<>(newClass);
            final WeakReference<?> instanceRef;
            {
                var instance = newClass.getDeclaredConstructor().newInstance();
                instanceRef = new WeakReference<>(instance);
                assertThat(instance.getClass().getDeclaredMethod("foo").invoke(instance), equalTo(123));
                //noinspection UnusedAssignment GC magic :)
                instance = null;
            }
            while (instanceRef.get() != null)  System.gc();
            //noinspection UnusedAssignment GC magic :)
            newClass = null;
        }

        int counter = 10;
        while (classRef.get() != null && counter-- != 0) System.gc();

        assertThat(classRef.get(), nullValue());
        assertThrows(ClassNotFoundException.class, () -> Class.forName(className));
    }
}