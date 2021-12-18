package ru.progrm_jarvis.javacommons.bytecode.asm;

import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import ru.progrm_jarvis.javacommons.classloading.GcClassDefiners;
import ru.progrm_jarvis.javacommons.classloading.ClassNamingStrategy;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

class AsmUtilTest {

    private static ClassNamingStrategy classNamingStrategy;

    @BeforeAll
    static void setUp() {
        classNamingStrategy = ClassNamingStrategy.createPaginated(AsmUtilTest.class.getName() + "$$generated$$");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAddEmptyConstructor()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ClassWriter clazz;
        String name, internalName, superClass;
        {
            clazz = new ClassWriter(0);

            name = classNamingStrategy.get();
            internalName = name.replace('.', '/');
            superClass = Type.getInternalName(TestSubject.class);
            clazz.visit(Opcodes.V1_8, AsmUtil.OPCODES_ACC_PUBLIC_FINAL_SUPER, internalName, null, superClass, null);
            AsmUtil.addEmptyConstructor(clazz, superClass);
            clazz.visitEnd();

            val constructor = ((Class<? extends TestSubject>) GcClassDefiners.getDefault()
                    .defineClass(MethodHandles.lookup(), name, clazz.toByteArray())).getDeclaredConstructor();

            assertThat(constructor.getParameterCount(), is(0));

            constructor.setAccessible(true);

            assertThat(constructor.newInstance(), instanceOf(TestSubject.class));
        }
        {
            clazz = new ClassWriter(0);

            name = classNamingStrategy.get();
            internalName = name.replace('.', '/');
            superClass = Type.getInternalName(StatusSubject.class);
            clazz.visit(Opcodes.V1_8, AsmUtil.OPCODES_ACC_PUBLIC_FINAL_SUPER, internalName, null, superClass, null);
            AsmUtil.addEmptyConstructor(clazz, superClass);
            clazz.visitEnd();

            val constructor = ((Class<? extends StatusSubject>) GcClassDefiners.getDefault()
                    .defineClass(MethodHandles.lookup(), name, clazz.toByteArray())).getDeclaredConstructor();

            assertThat(constructor.getParameterCount(), is(0));

            constructor.setAccessible(true);
            try {
                constructor.newInstance();
                // a SuccessStatus should be thrown
                fail();
            } catch (final InvocationTargetException e) {
                assertThat(e.getCause(), instanceOf(ExpectedException.class));
            }
        }
    }

    @Test
    void testAddEmptyConstructorDefault()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        val clazz = new ClassWriter(0);

        val name = classNamingStrategy.get();
        val internalName = name.replace('.', '/');
        clazz.visit(
                Opcodes.V1_8, AsmUtil.OPCODES_ACC_PUBLIC_FINAL_SUPER, internalName,
                null, AsmUtil.OBJECT_INTERNAL_NAME, null
        );
        AsmUtil.addEmptyConstructor(clazz);
        clazz.visitEnd();

        val constructor = GcClassDefiners.getDefault()
                .defineClass(MethodHandles.lookup(), name, clazz.toByteArray()).getDeclaredConstructor();

        assertThat(constructor.getParameterCount(), is(0));

        constructor.setAccessible(true);

        val instance = constructor.newInstance();
        assertThat(instance, instanceOf(Object.class));

        assertDoesNotThrow(() -> constructor.newInstance().getClass());
    }

    @SuppressWarnings("RedundantNoArgConstructor")
    public static class TestSubject {

        @SuppressWarnings("PublicConstructor")
        public TestSubject() {}
    }

    public static class StatusSubject {

        @SuppressWarnings("PublicConstructor")
        public StatusSubject() throws ExpectedException {
            throw new ExpectedException();
        }
    }

    @SuppressWarnings("PublicConstructor")
    private static class ExpectedException extends Exception {

        private static final long serialVersionUID = 0L;

        public ExpectedException() {
            super(null, null, true, false);
        }
    }
}