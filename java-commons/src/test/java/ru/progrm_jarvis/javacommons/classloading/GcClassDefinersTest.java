package ru.progrm_jarvis.javacommons.classloading;

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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GcClassDefinersTest {

    @Test
    @SuppressWarnings("CallToSystemGC")
    @EnabledIfSystemProperty(named = "test.gc.always-respects-System.gc()", matches = "true|yes|\\+|1|enabled")
    void testDefineGCClass() throws IOException, CannotCompileException, IllegalAccessException,
            InstantiationException, NoSuchMethodException, InvocationTargetException {
        val clazz = ClassPool.getDefault().makeClass(GcClassDefinersTest.class.getName() + "$GeneratedClass");
        clazz.addMethod(CtNewMethod.make("public int foo() {return 123;}", clazz));
        clazz.addConstructor(CtNewConstructor.make(new CtClass[0], new CtClass[0], clazz));

        val className = clazz.getName();

        final WeakReference<Class<?>> classRef;
        {
            var newClass = GcClassDefiners.getDefault()
                    .defineClass(MethodHandles.lookup(), className, clazz.toBytecode());
            classRef = new WeakReference<>(newClass);
            final WeakReference<?> instanceReference;
            {
                var instance = newClass.getDeclaredConstructor().newInstance();
                instanceReference = new WeakReference<>(instance);
                assertThat(instance.getClass().getDeclaredMethod("foo").invoke(instance), equalTo(123));
                //noinspection UnusedAssignment GC magic :)
                instance = null;
            }
            {
                var gcAttempts = 8;
                while (instanceReference.get() != null && gcAttempts-- != 0) System.gc();
            }
            assertNull(instanceReference.get());
            //noinspection UnusedAssignment GC magic :)
            newClass = null;
        }

        {
            var gcAttempts = 8;
            while (classRef.get() != null && gcAttempts-- != 0) System.gc();
        }
        assertNull(classRef.get());

        assertThrows(ClassNotFoundException.class, () -> Class.forName(className));
    }
}