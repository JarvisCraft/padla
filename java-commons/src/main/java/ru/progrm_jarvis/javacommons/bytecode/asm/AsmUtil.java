package ru.progrm_jarvis.javacommons.bytecode.asm;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import ru.progrm_jarvis.javacommons.bytecode.BytecodeLibrary;
import ru.progrm_jarvis.javacommons.bytecode.annotation.UsesBytecodeModification;

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
     * ASM type of {@link String}
     */
    STRING_TYPE = getType(String.class);
    ///////////////////////////////////////////////////////////////////////////
    // Strings
    ///////////////////////////////////////////////////////////////////////////
    /* ******************************************** Special Method names ******************************************** */
    /**
     * Prefix of generated fields after which the index will go
     */
   public final String STATIC_INITIALIZER_METHOD_NAME = "<clinit>",
    /**
     * Name of constructor-method
     */
    CONSTRUCTOR_METHOD_NAME = "<init>",
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
     * Internal name of {@link String}
     */
    STRING_INTERNAL_NAME = STRING_TYPE.getInternalName(),
    /* ********************************************* Class descriptors ********************************************* */
    /**
     * Descriptor of {@link Object}
     */
    OBJECT_DESCRIPTOR = OBJECT_TYPE.getDescriptor(),
    /**
     * Descriptor of {@link String}
     */
    STRING_DESCRIPTOR = STRING_TYPE.getDescriptor(),
    /* ************************************ Method descriptors (aka signatures) ************************************ */
    /**
     * Signature of {@code void()} method
     */
    VOID_METHOD_DESCRIPTOR = getMethodType(VOID_TYPE).getDescriptor(),
    /**
     * Signature of {@code boolean()} method
     */
    BOOLEAN_METHOD_DESCRIPTOR = getMethodType(BOOLEAN_TYPE).getDescriptor(),
    /**
     * Signature of {@code byte()} method
     */
    BYTE_METHOD_DESCRIPTOR = getMethodType(BYTE_TYPE).getDescriptor(),
    /**
     * Signature of {@code short()} method
     */
    SHORT_METHOD_DESCRIPTOR = getMethodType(SHORT_TYPE).getDescriptor(),
    /**
     * Signature of {@code int()} method
     */
    INT_METHOD_DESCRIPTOR = getMethodType(INT_TYPE).getDescriptor(),
    /**
     * Signature of {@code long()} method
     */
    LONG_METHOD_DESCRIPTOR = getMethodType(LONG_TYPE).getDescriptor(),
    /**
     * Signature of {@code float()} method
     */
    FLOAT_METHOD_DESCRIPTOR = getMethodType(FLOAT_TYPE).getDescriptor(),
    /**
     * Signature of {@code double()} method
     */
    DOUBLE_METHOD_DESCRIPTOR = getMethodType(DOUBLE_TYPE).getDescriptor(),
    /**
     * Signature of {@code char()} method
     */
    CHAR_METHOD_DESCRIPTOR = getMethodType(CHAR_TYPE).getDescriptor(),
    /**
     * Signature of {@code String()} method
     */
    STRING_METHOD_SIGNATURE = getMethodDescriptor(STRING_TYPE),
    /**
     * Signature of {@code void(boolean)} method
     */
    VOID_BOOLEAN_METHOD_DESCRIPTOR = getMethodType(VOID_TYPE, BOOLEAN_TYPE).getDescriptor(),
    /**
     * Signature of {@code void(byte)} method
     */
    VOID_BYTE_METHOD_DESCRIPTOR = getMethodType(VOID_TYPE, BYTE_TYPE).getDescriptor(),
    /**
     * Signature of {@code void(short)} method
     */
    VOID_SHORT_METHOD_DESCRIPTOR = getMethodType(VOID_TYPE, SHORT_TYPE).getDescriptor(),
    /**
     * Signature of {@code void(int)} method
     */
    VOID_INT_METHOD_DESCRIPTOR = getMethodType(VOID_TYPE, INT_TYPE).getDescriptor(),
    /**
     * Signature of {@code void(long)} method
     */
    VOID_LONG_METHOD_DESCRIPTOR = getMethodType(VOID_TYPE, LONG_TYPE).getDescriptor(),
    /**
     * Signature of {@code void(float)} method
     */
    VOID_FLOAT_METHOD_DESCRIPTOR = getMethodType(VOID_TYPE, FLOAT_TYPE).getDescriptor(),
    /**
     * Signature of {@code void(double)} method
     */
    VOID_DOUBLE_METHOD_DESCRIPTOR = getMethodType(VOID_TYPE, DOUBLE_TYPE).getDescriptor(),
    /**
     * Signature of {@code void(char)} method
     */
    VOID_CHAR_METHOD_DESCRIPTOR = getMethodType(VOID_TYPE, CHAR_TYPE).getDescriptor(),
    /**
     * Signature of {@code void(String)} method
     */
    VOID_STRING_METHOD_DESCRIPTOR = getMethodType(VOID_TYPE, STRING_TYPE).getDescriptor();

    /* ****************************************** Stored multi-opcodes ****************************************** */
    /**
     * Result of {@link Opcodes#ACC_PUBLIC} and {@link Opcodes#ACC_FINAL} flags disjunction
     */
    public final int OPCODES_ACC_PUBLIC_FINAL = ACC_PUBLIC | ACC_FINAL,
    /**
     * Result of {@link Opcodes#ACC_PUBLIC}, {@link Opcodes#ACC_FINAL}
     * and {@link Opcodes#ACC_SUPER} flags disjunction
     */
    OPCODES_ACC_PUBLIC_FINAL_SUPER = OPCODES_ACC_PUBLIC_FINAL | ACC_SUPER;

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
}
