package ru.progrm_jarvis.javacommons.delegate;

import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import ru.progrm_jarvis.javacommons.annotation.Internal;
import ru.progrm_jarvis.javacommons.bytecode.CommonBytecodeLibrary;
import ru.progrm_jarvis.javacommons.bytecode.annotation.UsesBytecodeModification;
import ru.progrm_jarvis.javacommons.bytecode.asm.AsmUtil;
import ru.progrm_jarvis.javacommons.cache.Cache;
import ru.progrm_jarvis.javacommons.cache.Caches;
import ru.progrm_jarvis.javacommons.classloading.ClassNamingStrategy;
import ru.progrm_jarvis.javacommons.classloading.GcClassDefiners;
import ru.progrm_jarvis.javacommons.util.UncheckedCasts;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

@UsesBytecodeModification(CommonBytecodeLibrary.ASM)
public final class AsmDelegateFactory extends CachingGeneratingDelegateFactory {

    /**
     * Lookup of this class
     */
    private static final @NotNull MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * Class naming strategy used to allocate names for generated classes
     */
    private static final @NotNull ClassNamingStrategy CLASS_NAMING_STRATEGY = ClassNamingStrategy.createPaginated(
            AsmDelegateFactory.class.getName() + "$$Generated$$SupplierWrapper$$"
    );

    /**
     * ASM type of {@link Supplier}
     */
    private static final @NotNull Type SUPPLIER_TYPE = getType(Supplier.class);

    /**
     * Name of the generated field in which the lazy will be stored
     */
    private static final @NotNull String GENERATED_INNER_LAZY_FIELD_NAME = "$" ,
    /**
     * Name of {@link Supplier#get()} method
     */
    GET_METHOD_NAME = "get" ,
    /**
     * Internal name of {@link Supplier}
     */
    SUPPLIER_INTERNAL_NAME = SUPPLIER_TYPE.getInternalName(),
    /**
     * Descriptor of {@link Supplier}
     */
    SUPPLIER_DESCRIPTOR = SUPPLIER_TYPE.getDescriptor(),
    /**
     * Descriptor of {@code void(}{@link Supplier}{@code )} method
     */
    VOID_SUPPLIER_METHOD_DESCRIPTOR = getMethodDescriptor(VOID_TYPE, SUPPLIER_TYPE);

    private AsmDelegateFactory(final @NotNull Cache<Class<?>, DelegateWrapperFactory<?>> factories) {
        super(factories);
    }

    /**
     * Creates an {@link DelegateFactory ASM-based supplier wrapper}.
     *
     * @return ASM-based supplier wrapper
     *
     * @apiNote singleton may be used here
     */
    public static DelegateFactory create() {
        return Singleton.INSTANCE;
    }

    @Override
    protected <T> @NotNull DelegateWrapperFactory<T> createFactory(final @NotNull Class<T> targetType) {
        final Constructor<? extends T> constructor;
        {
            final Class<? extends T> generatedClass = generateWrapperClass(targetType);

            try {
                constructor = generatedClass.getDeclaredConstructor(Supplier.class);
            } catch (final NoSuchMethodException e) {
                throw new Error("Could not get an empty constructor of the generated class" , e);
            }
            constructor.setAccessible(true);
        }

        return supplier -> {
            try {
                return constructor.newInstance(supplier);
            } catch (final IllegalAccessException | InstantiationException | InvocationTargetException e) {
                throw new Error("Cannot invoke constructor of the generated class" , e);
            }
        };
    }

    /**
     * Creates a class implementing a delegate-wrapper for the given type.
     *
     * @param targetClass class for which to create a delegate-wrapper
     * @param <T> type of implemented delegate-wrapper
     * @return created delegate-wrapper for the given type
     */
    private static <T> @NotNull Class<? extends T> generateWrapperClass(final @NotNull Class<T> targetClass) {
        final String className;
        val clazz = new ClassWriter(0);

        //<editor-fold desc="Class generation" defaultstate="collapsed">
        {
            val internalName = AsmUtil.classNameToInternalName(className = CLASS_NAMING_STRATEGY.get());

            {
                final Type targetType;
                final String
                        targetInternalName = (targetType = getType(targetClass)).getInternalName(),
                        superClassInternalName;
                if (targetClass.isInterface()) clazz.visit( // implement interface
                        V1_8, AsmUtil.OPCODES_ACC_PUBLIC_STATIC_FINAL, internalName,
                        null /* no generics */, superClassInternalName = AsmUtil.OBJECT_INTERNAL_NAME,
                        new String[]{targetInternalName}
                );
                else clazz.visit( // extend class
                        V1_8, AsmUtil.OPCODES_ACC_PUBLIC_STATIC_FINAL, internalName,
                        null /* no generics */, superClassInternalName = targetInternalName, null
                );

                //<editor-fold desc="Field generation" defaultstate="collapsed">
                final String supplierSignature;
                clazz.visitField(
                        AsmUtil.OPCODES_ACC_PUBLIC_FINAL /* safe to use public here */, GENERATED_INNER_LAZY_FIELD_NAME,
                        SUPPLIER_DESCRIPTOR,
                        supplierSignature = 'L' + SUPPLIER_INTERNAL_NAME + '<' + targetType.getDescriptor() + ">;" ,
                        null
                ).visitEnd();
                //</editor-fold>

                //<editor-fold desc="Constructor generation" defaultstate="collapsed">
                {
                    final MethodVisitor constructor;
                    (constructor = clazz.visitMethod(
                            ACC_PUBLIC, AsmUtil.CONSTRUCTOR_METHOD_NAME, VOID_SUPPLIER_METHOD_DESCRIPTOR,
                            '(' + supplierSignature + ")V" , null /* no exceptions */
                    )).visitCode();

                    // push `this` onto the stack
                    constructor.visitVarInsn(ALOAD, 0);
                    // duplicate `this` for later use
                    constructor.visitInsn(DUP);
                    // invoke constructor of the super-class
                    constructor.visitMethodInsn(
                            INVOKESPECIAL, superClassInternalName,
                            AsmUtil.CONSTRUCTOR_METHOD_NAME, AsmUtil.VOID_METHOD_DESCRIPTOR, false
                    );

                    // push `Supplier<T>` parameter onto the stack
                    constructor.visitVarInsn(ALOAD, 1);
                    // put parameter into the field
                    constructor.visitFieldInsn(
                            PUTFIELD, internalName, GENERATED_INNER_LAZY_FIELD_NAME, SUPPLIER_DESCRIPTOR
                    );

                    constructor.visitInsn(RETURN);

                    constructor.visitMaxs(2 /* this + field value */, 2 /* this + single parameter */);
                    constructor.visitEnd();
                }
                //</editor-fold>

                for (val originalMethod : targetClass.getMethods()) {
                    if (Modifier.isStatic(originalMethod.getModifiers())) continue;

                    //<editor-fold desc="Method generation" defaultstate="collapsed">
                    final MethodVisitor method;
                    final String methodDescriptor, methodName;
                    {
                        final String[] exceptions;
                        {
                            final Class<?>[] exceptionTypes;
                            final int parametersLength;
                            exceptions = new String[parametersLength = (
                                    exceptionTypes = originalMethod.getExceptionTypes()
                            ).length];
                            for (var i = 0; i < parametersLength; i++) exceptions[i] = getDescriptor(exceptionTypes[i]);
                        }

                        (method = clazz.visitMethod(
                                ACC_PUBLIC, methodName = originalMethod.getName(),
                                methodDescriptor = getMethodDescriptor(originalMethod), null, exceptions
                        )).visitVarInsn(ALOAD, 0); // push `this` onto the stack
                    }
                    // get field storing the Supplier
                    method.visitFieldInsn(GETFIELD, internalName, GENERATED_INNER_LAZY_FIELD_NAME, SUPPLIER_DESCRIPTOR);
                    // invoke `Supplier#get()` on the field
                    method.visitMethodInsn(
                            INVOKEINTERFACE, SUPPLIER_INTERNAL_NAME, GET_METHOD_NAME,
                            AsmUtil.OBJECT_METHOD_DESCRIPTOR, true
                    );

                    {
                        final int parameterCount;

                        {
                            final Class<?>[] parameterTypes;
                            parameterCount = (parameterTypes = originalMethod.getParameterTypes()).length;
                            for (var i = 0; i < parameterCount; i++)
                                method
                                        .visitVarInsn(AsmUtil.loadOpcode(parameterTypes[i]), i + 1);
                        }

                        // Invoke method on the delegate
                        {
                            final Class<?> methodOwner;
                            val ownerInternalName = getInternalName(methodOwner = originalMethod.getDeclaringClass());
                            if (methodOwner.isInterface()) method.visitMethodInsn(
                                    INVOKEINTERFACE, ownerInternalName, methodName, methodDescriptor, true
                            );
                            else method.visitMethodInsn(
                                    INVOKEVIRTUAL, ownerInternalName, methodName, methodDescriptor, false
                            );

                        }

                        // return from the method
                        method.visitInsn(AsmUtil.returnOpcode(originalMethod.getReturnType()));

                        {
                            final int max; // this + parameters == delegate + parameters
                            method.visitMaxs(max = 1 + parameterCount, max);
                        }
                    }

                    method.visitEnd();
                    //</editor-fold>
                }
            }

            clazz.visitEnd();
        }
        //</editor-fold>

        return UncheckedCasts.uncheckedClassCast(GcClassDefiners.getDefault().defineClass(LOOKUP, className, clazz.toByteArray()));
    }

    @UtilityClass
    @Internal("Safe singleton implementation using class-loading rules for achieving efficient thread-safe laziness")
    private static class Singleton {

        /**
         * Instance of {@link AsmDelegateFactory ASM-based delegate factory}
         */
        private final @NotNull DelegateFactory INSTANCE = new AsmDelegateFactory(
                Caches.weakKeysCache() // classes are GC-friendly loaded so they may be effectively weakly-referenced
        );
    }
}
