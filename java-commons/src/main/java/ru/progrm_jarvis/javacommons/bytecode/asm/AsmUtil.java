package ru.progrm_jarvis.javacommons.bytecode.asm;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import ru.progrm_jarvis.javacommons.bytecode.BytecodeLibrary;
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
@UsesBytecodeModification(BytecodeLibrary.ASM)
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
     * Signature of {@code String()} method
     */
    STRING_METHOD_SIGNATURE = getMethodDescriptor(STRING_TYPE),
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
     * Visits the class's static initializer method.
     *
     * @param clazz visitor of the class
     * @return static initializer block of the class
     */
    public MethodVisitor visitStaticInitializer(@NonNull final ClassVisitor clazz) {
        return clazz.visitMethod(ACC_STATIC, STATIC_INITIALIZER_METHOD_NAME, VOID_METHOD_DESCRIPTOR, null, null);
    }

    /**
     * Adds {@link Lookup the inner lookup class} to the class needed by the JVM via its visitor.
     *
     * @param clazz visitor of the class
     */
    public void addLookup(@NonNull final ClassVisitor clazz) {
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
    public void addEmptyConstructor(@NonNull final ClassVisitor classVisitor,
                                    @NonNull final String superClassInternalName) {
        // visit (create) empty constructor method
        val constructor = classVisitor.visitMethod(
                ACC_PUBLIC, CONSTRUCTOR_METHOD_NAME, VOID_METHOD_DESCRIPTOR,
                null /* no generics in signature as there are no parameters */, null /* no exceptions declared */
        );

        constructor.visitCode();
        // push `this` onto the stack
        constructor.visitIntInsn(ALOAD, 0);
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
    public void addEmptyConstructor(@NonNull final ClassVisitor classVisitor) {
        addEmptyConstructor(classVisitor, OBJECT_INTERNAL_NAME);
    }

    /**
     * Adds code to the method which pushes the given {@code int} value onto the stack.
     *
     * @param method method visitor used for appending code to the method
     * @param value value to get pushed onto the stack
     */
    public void pushInt(@NonNull final MethodVisitor method, final int value) {
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
    public void pushLong(@NonNull final MethodVisitor method, final long value) {
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
    public void pushFloat(@NonNull final MethodVisitor method, final float value) {
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
    public void pushDouble(@NonNull final MethodVisitor method, final double value) {
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
    public void pushCharUnsafely(@NonNull final MethodVisitor method, final char value) {
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
