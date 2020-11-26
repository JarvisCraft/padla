package ru.progrm_jarvis.javacommons.bytecode.asm;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import ru.progrm_jarvis.javacommons.bytecode.CommonBytecodeLibrary;
import ru.progrm_jarvis.javacommons.bytecode.annotation.UsesBytecodeModification;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * Utility for common ObjectWeb ASM tasks.
 */
@UtilityClass
@UsesBytecodeModification(CommonBytecodeLibrary.ASM)
public class AsmUtil {

    ///////////////////////////////////////////////////////////////////////////
    // Types
    ///////////////////////////////////////////////////////////////////////////
    /**
     * ASM type of {@link Object}
     */
    public final Type OBJECT_TYPE = getType(Object.class),
    /**
     * ASM type of {@link Object} array
     */
    OBJECT_ARRAY_TYPE = getType(Object[].class),
    /**
     * ASM type of {@link String}
     */
    STRING_TYPE = getType(String.class),
    /**
     * ASM type of {@link MethodHandles}
     */
    METHOD_HANDLES_TYPE = getType(MethodHandles.class),
    /**
     * ASM type of {@link Lookup}
     */
    LOOKUP_TYPE = getType(Lookup.class),
    /**
     * ASM type of {@link CallSite}
     */
    CALL_SITE_TYPE = getType(CallSite.class),
    /**
     * ASM type of {@link MethodType}
     */
    METHOD_TYPE_TYPE = getType(MethodType.class);
    ///////////////////////////////////////////////////////////////////////////
    // Strings
    ///////////////////////////////////////////////////////////////////////////
    /* ******************************************** Special Method names ******************************************** */
    /**
     * Name of static initializer method
     */
    public final String STATIC_INITIALIZER_METHOD_NAME = "<clinit>",
    /**
     * Name of constructor method
     */
    CONSTRUCTOR_METHOD_NAME = "<init>",
    /* ************************************ Very special names (read INDY magic) ************************************ */
    /**
     * Name of inner lookup class.
     */
    LOOKUP_INNER_CLASS_NAME = "Lookup",
    /* ******************************************** Common method names ******************************************** */
    /**
     * Name of {@link Object#hashCode()} method
     */
    HASH_CODE_METHOD_NAME = "hashCode",
    /**
     * Name of {@link Object#equals(Object)} method
     */
    EQUALS_METHOD_NAME = "equals",
    /**
     * Name of {@link Object#toString()} method
     */
    TO_STRING_METHOD_NAME = "toString",
    /* ******************************************** Internal class names ******************************************** */
    /**
     * Internal name of {@link Object}
     */
    OBJECT_INTERNAL_NAME = OBJECT_TYPE.getInternalName(),
    /**
     * Internal name of {@link Object} array
     */
    OBJECT_ARRAY_INTERNAL_NAME = OBJECT_ARRAY_TYPE.getInternalName(),
    /**
     * Internal name of {@link String}
     */
    STRING_INTERNAL_NAME = STRING_TYPE.getInternalName(),
    /**
     * Internal name of {@link MethodHandles}
     */
    METHOD_HANDLES_INTERNAL_NAME = METHOD_HANDLES_TYPE.getInternalName(),
    /**
     * Internal name of {@link Lookup}
     */
    LOOKUP_INTERNAL_NAME = LOOKUP_TYPE.getInternalName(),
    /**
     * Internal name of {@link CallSite}
     */
    CALL_SITE_INTERNAL_NAME = CALL_SITE_TYPE.getInternalName(),
    /**
     * Internal name of {@link MethodType}
     */
    METHOD_TYPE_INTERNAL_NAME = METHOD_TYPE_TYPE.getInternalName(),
    /* ********************************************* Class descriptors ********************************************* */
    /**
     * Descriptor of {@link Object}
     */
    OBJECT_DESCRIPTOR = OBJECT_TYPE.getDescriptor(),
    /**
     * Descriptor of {@link Object} array
     */
    OBJECT_ARRAY_DESCRIPTOR = OBJECT_ARRAY_TYPE.getDescriptor(),
    /**
     * Descriptor of {@link String}
     */
    STRING_DESCRIPTOR = STRING_TYPE.getDescriptor(),
    /**
     * Descriptor of {@link MethodHandles}
     */
    METHOD_HANDLES_DESCRIPTOR = METHOD_HANDLES_TYPE.getDescriptor(),
    /**
     * Descriptor of {@link Lookup}
     */
    LOOKUP_DESCRIPTOR = LOOKUP_TYPE.getDescriptor(),
    /**
     * Descriptor of {@link CallSite}
     */
    CALL_SITE_DESCRIPTOR = CALL_SITE_TYPE.getDescriptor(),
    /**
     * Descriptor of {@link MethodType}
     */
    METHOD_TYPE_DESCRIPTOR = METHOD_TYPE_TYPE.getDescriptor(),
    /* ************************************ Method descriptors (aka signatures) ************************************ */
    /**
     * Signature of {@code void()} method
     */
    VOID_METHOD_DESCRIPTOR = getMethodDescriptor(VOID_TYPE),
    /**
     * Signature of {@code boolean()} method
     */
    BOOLEAN_METHOD_DESCRIPTOR = getMethodDescriptor(BOOLEAN_TYPE),
    /**
     * Signature of {@code byte()} method
     */
    BYTE_METHOD_DESCRIPTOR = getMethodDescriptor(BYTE_TYPE),
    /**
     * Signature of {@code short()} method
     */
    SHORT_METHOD_DESCRIPTOR = getMethodDescriptor(SHORT_TYPE),
    /**
     * Signature of {@code int()} method
     */
    INT_METHOD_DESCRIPTOR = getMethodDescriptor(INT_TYPE),
    /**
     * Signature of {@code long()} method
     */
    LONG_METHOD_DESCRIPTOR = getMethodDescriptor(LONG_TYPE),
    /**
     * Signature of {@code float()} method
     */
    FLOAT_METHOD_DESCRIPTOR = getMethodDescriptor(FLOAT_TYPE),
    /**
     * Signature of {@code double()} method
     */
    DOUBLE_METHOD_DESCRIPTOR = getMethodDescriptor(DOUBLE_TYPE),
    /**
     * Signature of {@code char()} method
     */
    CHAR_METHOD_DESCRIPTOR = getMethodDescriptor(CHAR_TYPE),
    /**
     * Signature of {@code Object()} method
     */
    OBJECT_METHOD_DESCRIPTOR = getMethodDescriptor(OBJECT_TYPE),
    /**
     * Signature of {@code String()} method
     */
    STRING_METHOD_DESCRIPTOR = getMethodDescriptor(STRING_TYPE),
    /**
     * Signature of {@code void(boolean)} method
     */
    VOID_BOOLEAN_METHOD_DESCRIPTOR = getMethodDescriptor(VOID_TYPE, BOOLEAN_TYPE),
    /**
     * Signature of {@code void(byte)} method
     */
    VOID_BYTE_METHOD_DESCRIPTOR = getMethodDescriptor(VOID_TYPE, BYTE_TYPE),
    /**
     * Signature of {@code void(short)} method
     */
    VOID_SHORT_METHOD_DESCRIPTOR = getMethodDescriptor(VOID_TYPE, SHORT_TYPE),
    /**
     * Signature of {@code void(int)} method
     */
    VOID_INT_METHOD_DESCRIPTOR = getMethodDescriptor(VOID_TYPE, INT_TYPE),
    /**
     * Signature of {@code void(long)} method
     */
    VOID_LONG_METHOD_DESCRIPTOR = getMethodDescriptor(VOID_TYPE, LONG_TYPE),
    /**
     * Signature of {@code void(float)} method
     */
    VOID_FLOAT_METHOD_DESCRIPTOR = getMethodDescriptor(VOID_TYPE, FLOAT_TYPE),
    /**
     * Signature of {@code void(double)} method
     */
    VOID_DOUBLE_METHOD_DESCRIPTOR = getMethodDescriptor(VOID_TYPE, DOUBLE_TYPE),
    /**
     * Signature of {@code void(char)} method
     */
    VOID_CHAR_METHOD_DESCRIPTOR = getMethodDescriptor(VOID_TYPE, CHAR_TYPE),
    /**
     * Signature of {@code void(String)} method
     */
    VOID_STRING_METHOD_DESCRIPTOR = getMethodDescriptor(VOID_TYPE, STRING_TYPE);

    /* ****************************************** Stored multi-opcodes ****************************************** */
    /**
     * Result of {@link Opcodes#ACC_PUBLIC} and {@link Opcodes#ACC_FINAL} flags disjunction
     */
    public final int OPCODES_ACC_PUBLIC_FINAL = ACC_PUBLIC | ACC_FINAL,
    /**
     * Result of {@link Opcodes#ACC_PUBLIC}, {@link Opcodes#ACC_STATIC}
     * and {@link Opcodes#ACC_FINAL} flags disjunction
     */
    OPCODES_ACC_PUBLIC_STATIC_FINAL = OPCODES_ACC_PUBLIC_FINAL | ACC_STATIC,
    /**
     * Result of {@link Opcodes#ACC_PUBLIC}, {@link Opcodes#ACC_FINAL}
     * and {@link Opcodes#ACC_SUPER} flags disjunction
     */
    OPCODES_ACC_PUBLIC_FINAL_SUPER = OPCODES_ACC_PUBLIC_FINAL | ACC_SUPER;

    /**
     * Gets the internal name (aka VM-name) for the given {@link ClassLoader}-unique name.
     *
     * @param className name of the class
     * @return internal name corresponding to the given class name
     *
     * @see Class#getName() as an appropriate source of this method's parameter
     * @see #internalNameOf(Class) alias of this method accepting {@link Class} object
     */
    public @NotNull String classNameToInternalName(final @NonNull String className) {
        return className.replace('.', '/');
    }

    /**
     * Gets the internal name (aka VM-name) of the given class.
     *
     * @param type class whose internal name should be resolved
     * @return internal name of the given class
     */
    public @NotNull String internalNameOf(final @NonNull Class<?> type) {
        return classNameToInternalName(type.getName());
    }

    /**
     * Visits the class's static initializer method.
     *
     * @param clazz visitor of the class
     * @return static initializer block of the class
     */
    public MethodVisitor visitStaticInitializer(final @NonNull ClassVisitor clazz) {
        return clazz.visitMethod(ACC_STATIC, STATIC_INITIALIZER_METHOD_NAME, VOID_METHOD_DESCRIPTOR, null, null);
    }

    /**
     * Adds {@link Lookup the inner lookup class} to the class needed by the JVM via its visitor.
     *
     * @param clazz visitor of the class
     */
    public void addLookup(final @NonNull ClassVisitor clazz) {
        clazz.visitInnerClass(
                LOOKUP_INTERNAL_NAME, METHOD_HANDLES_INTERNAL_NAME,
                LOOKUP_INNER_CLASS_NAME, OPCODES_ACC_PUBLIC_STATIC_FINAL
        );
    }

    /**
     * Adds an empty constructor to the class via its visitor.
     *
     * @param classVisitor visitor of the class modified
     * @param superClassInternalName internal name of the super-class whose constructor should be invoked
     */
    public void addEmptyConstructor(final @NonNull ClassVisitor classVisitor,
                                    final @NonNull String superClassInternalName) {
        // visit (create) empty constructor method
        val constructor = classVisitor.visitMethod(
                ACC_PUBLIC, CONSTRUCTOR_METHOD_NAME, VOID_METHOD_DESCRIPTOR,
                null /* no generics in signature as there are no parameters */, null /* no exceptions declared */
        );

        constructor.visitCode();
        // push `this` onto the stack
        constructor.visitVarInsn(ALOAD, 0);
        // invoke constructor of the super-class
        constructor.visitMethodInsn(
                INVOKESPECIAL, superClassInternalName, CONSTRUCTOR_METHOD_NAME, VOID_METHOD_DESCRIPTOR, false
        );
        constructor.visitInsn(RETURN);

        // pre-computed MAXs
        constructor.visitMaxs(1, 1);

        constructor.visitEnd();
    }

    /**
     * Adds an empty constructor to the class via its visitor.
     *
     * @param classVisitor visitor of the class modified
     *
     * @see #addEmptyConstructor(ClassVisitor, String) the method for handlin super-classes other than {@link Object}
     */
    public void addEmptyConstructor(final @NonNull ClassVisitor classVisitor) {
        addEmptyConstructor(classVisitor, OBJECT_INTERNAL_NAME);
    }

    /**
     * Gets the return-{@link Opcodes opcode} corresponding to the given non-void type.
     *
     * @param returnType type of the value for which to get the return-opcode
     * @return return-opcode corresponding to the given type
     *
     * @apiNote behaviour for {@code returnType == void.type} is undefined
     */
    @SuppressWarnings("ChainOfInstanceofChecks")
    private int nonVoidReturnOpcodeNoChecks(final Class<?> returnType) {
        if (returnType.isPrimitive()) {
            if (returnType == boolean.class || returnType == byte.class
                    || returnType == short.class || returnType == int.class) return IRETURN;
            if (returnType == long.class) return LRETURN;
            if (returnType == float.class) return FRETURN;
            if (returnType == double.class) return DRETURN;
        }

        return ARETURN;
    }

    /**
     * Gets the return-{@link Opcodes opcode} corresponding to the given type.
     *
     * @param returnType type of the value for which to get the return-opcode
     * @return return-opcode corresponding to the given type
     */
    public int returnOpcode(final Class<?> returnType) {
        return returnType == void.class ? RETURN : nonVoidReturnOpcodeNoChecks(returnType);
    }

    /**
     * Gets the return-{@link Opcodes opcode} corresponding to the given non-void type.
     *
     * @param returnType type of the value for which to get the return-opcode
     * @return return-opcode corresponding to the given type
     *
     * @throws IllegalArgumentException if {@code returnType} is of {@code void} type
     */
    public int nonVoidReturnOpcode(final Class<?> returnType) {
        if (returnType == void.class) throw new IllegalStateException("returnType should not be of void-type");

        return nonVoidReturnOpcodeNoChecks(returnType);
    }

    /**
     * Gets the load-{@link Opcodes opcode} corresponding to the given type.
     *
     * @param loadedType type of the value for which to get the load-opcode
     * @return load-opcode corresponding to the given type
     */
    @SuppressWarnings("ChainOfInstanceofChecks")
    public int loadOpcode(final Class<?> loadedType) {
        if (loadedType.isPrimitive()) {
            if (loadedType == boolean.class || loadedType == byte.class
                    || loadedType == short.class || loadedType == int.class) return ILOAD;
            if (loadedType == long.class) return LLOAD;
            if (loadedType == float.class) return FLOAD;
            if (loadedType == double.class) return DLOAD;
        }

        return ALOAD;
    }

    /**
     * Gets the array-load-{@link Opcodes opcode} corresponding to the array of given component type.
     *
     * @param loadedComponentType component type of the array for which to get the array-load-opcode
     * @return array-load-opcode corresponding to the given array component type
     */
    @SuppressWarnings("ChainOfInstanceofChecks")
    public int arrayLoadOpcode(final Class<?> loadedComponentType) {
        if (loadedComponentType.isPrimitive()) {
            if (loadedComponentType == boolean.class || loadedComponentType == byte.class) return BALOAD;
            if (loadedComponentType == short.class) return SALOAD;
            if (loadedComponentType == int.class) return IALOAD;
            if (loadedComponentType == long.class) return LALOAD;
            if (loadedComponentType == float.class) return FALOAD;
            if (loadedComponentType == double.class) return DALOAD;
        }

        return AALOAD;
    }

    /**
     * Gets the store-{@link Opcodes opcode} corresponding to the given type
     *
     * @param storedType type of the value for which to get the store-opcode
     * @return store-opcode corresponding to the given type
     */
    @SuppressWarnings("ChainOfInstanceofChecks")
    public int storeOpcode(final Class<?> storedType) {
        if (storedType.isPrimitive()) {
            if (storedType == boolean.class || storedType == byte.class
                    || storedType == short.class || storedType == int.class) return ISTORE;
            if (storedType == long.class) return LSTORE;
            if (storedType == float.class) return FSTORE;
            if (storedType == double.class) return DSTORE;
        }

        return ASTORE;
    }

    /**
     * Gets the array-store-{@link Opcodes opcode} corresponding to the array of given component type.
     *
     * @param storedComponentType component type of the array for which to get the array-store-opcode
     * @return array-store-opcode corresponding to the given array component type
     */
    @SuppressWarnings("ChainOfInstanceofChecks")
    public int arrayStoreOpcode(final Class<?> storedComponentType) {
        if (storedComponentType.isPrimitive()) {
            if (storedComponentType == boolean.class || storedComponentType == byte.class) return BASTORE;
            if (storedComponentType == short.class) return SASTORE;
            if (storedComponentType == int.class) return IASTORE;
            if (storedComponentType == long.class) return LASTORE;
            if (storedComponentType == float.class) return FASTORE;
            if (storedComponentType == double.class) return DASTORE;
        }
        return AASTORE;
    }

    /**
     * Adds code to the method which pushes the given {@code int} value onto the stack.
     *
     * @param method method visitor used for appending code to the method
     * @param value value to get pushed onto the stack
     */
    public void pushInt(final @NonNull MethodVisitor method, final int value) {
        switch (value) {
            case -1: {
                method.visitInsn(ICONST_M1);
                break;
            }
            case 0: {
                method.visitInsn(ICONST_0);
                break;
            }
            case 1: {
                method.visitInsn(ICONST_1);
                break;
            }
            case 2: {
                method.visitInsn(ICONST_2);
                break;
            }
            case 3: {
                method.visitInsn(ICONST_3);
                break;
            }
            case 4: {
                method.visitInsn(ICONST_4);
                break;
            }
            case 5: {
                method.visitInsn(ICONST_5);
                break;
            }
            default: {
                if (Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE) method.visitIntInsn(BIPUSH, value);
                else if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) method.visitIntInsn(SIPUSH, value);
                else method.visitLdcInsn(value);
            }
        }
    }

    /**
     * Adds code to the method which pushes the given {@code long} value onto the stack.
     *
     * @param method method visitor used for appending code to the method
     * @param value value to get pushed onto the stack
     */
    public void pushLong(final @NonNull MethodVisitor method, final long value) {
        if (value == 0) method.visitInsn(LCONST_0);
        else if (value == 1) method.visitInsn(LCONST_1);
        else method.visitLdcInsn(value);
    }

    /**
     * Adds code to the method which pushes the given {@code float} value onto the stack.
     *
     * @param method method visitor used for appending code to the method
     * @param value value to get pushed onto the stack
     */
    public void pushFloat(final @NonNull MethodVisitor method, final float value) {
        if (value == 0) method.visitInsn(FCONST_0);
        else if (value == 1) method.visitInsn(FCONST_1);
        else if (value == 2) method.visitInsn(FCONST_2);
        else method.visitLdcInsn(value);
    }

    /**
     * Adds code to the method which pushes the given {@code double} value onto the stack.
     *
     * @param method method visitor used for appending code to the method
     * @param value value to get pushed onto the stack
     */
    public void pushDouble(final @NonNull MethodVisitor method, final double value) {
        if (value == 0) method.visitInsn(DCONST_0);
        else if (value == 1) method.visitInsn(DCONST_1);
        else method.visitLdcInsn(value);
    }

    /**
     * Adds code to the method which <b>unsafely</b> pushes the given {@code char} value onto the stack.
     * The unsafety of this operation is in the fact that for some chars
     * (<i>[(char) 128; (char) 255]</i> and <i>[(char) 32768; (char) 65535</i>) negative {@code int}s will be
     * pushed onto the stack thus resulting incorrect values when casting those to signed numeric types.
     * This yet might be a micro-optimization in some cases when there is a guarantee that the pushed value
     * will only be treated as {@code char} (such as passing it to {@link StringBuilder#append(char)}.
     *
     * @param method method visitor used for appending code to the method
     * @param value value to get pushed onto the stack
     *
     * @see #pushInt(MethodVisitor, int) can be used as a safe alternative
     */
    public void pushCharUnsafely(final @NonNull MethodVisitor method, final char value) {
        switch (value) {
            case 0: {
                method.visitInsn(ICONST_0);
                break;
            }
            case 1: {
                method.visitInsn(ICONST_1);
                break;
            }
            case 2: {
                method.visitInsn(ICONST_2);
                break;
            }
            case 3: {
                method.visitInsn(ICONST_3);
                break;
            }
            case 4: {
                method.visitInsn(ICONST_4);
                break;
            }
            case 5: {
                method.visitInsn(ICONST_5);
                break;
            }
            case 65535: {
                method.visitInsn(ICONST_M1);
                break;
            }
            default: {
                if (value <= Byte.MAX_VALUE) method.visitIntInsn(BIPUSH, (byte) value);
                else method.visitIntInsn(SIPUSH, (short) value);
            }
        }
    }
}
