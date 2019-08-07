package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import ru.progrm_jarvis.javacommons.classload.ClassFactory;
import ru.progrm_jarvis.javacommons.lazy.Lazy;
import ru.progrm_jarvis.javacommons.util.ClassNamingStrategy;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * Implementation of {@link TextModelFactory text model factory} which uses runtime class generation.
 */
public class AsmTextModelFactory<T> implements TextModelFactory<T> {

    /**
     * Lazy singleton of this text model factory
     */
    private static final Lazy<AsmTextModelFactory> INSTANCE
            = Lazy.createThreadSafe(AsmTextModelFactory::new);

    /**
     * Returns this {@link TextModelFactory text model factory} singleton.
     *
     * @param <T> generic type of got {@link TextModelFactory text model factory}
     * @return shared instance of this {@link TextModelFactory text model factory}
     */
    @SuppressWarnings("unchecked")
    public static <T> AsmTextModelFactory<T> get() {
        return INSTANCE.get();
    }

    @Override
    public TextModelFactory.TextModelTemplate<T> newTemplate() {
        return new TextModelTemplate<>();
    }

    /**
     * Implementation of
     * {@link TextModelFactory.TextModelTemplate text model template}
     * which uses runtime class generation
     * and is capable of joining nearby static text blocks and optimizing {@link #createAndRelease()}.
     *
     * @param <T> type of object according to which the created text models are formatted
     */
    @ToString
    @EqualsAndHashCode(callSuper = true) // simply, why not? :) (this will also allow caching of instances)
    @FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
    protected static class TextModelTemplate<T> extends AbstractGeneratingTextModelFactoryTemplate<T> {

        /**
         * Class naming strategy used to allocate names for generated classes
         */
        @NonNull private static final ClassNamingStrategy CLASS_NAMING_STRATEGY = ClassNamingStrategy.createPaginated(
                TextModelTemplate.class.getName() + "$$Generated$$TextModel$$"
        );

        //<editor-fold desc="Bytecode generation constants" defaultstate="collapsed">

        ///////////////////////////////////////////////////////////////////////////
        // Types
        ///////////////////////////////////////////////////////////////////////////
        /* ******************************************** ASM Type objects ******************************************** */
        /**
         * ASM type of {@link Object}
         */
        protected static final Type OBJECT_TYPE = getType(Object.class),
        /**
         * ASM type of {@link String}
         */
        STRING_TYPE = getType(String.class),
        /**
         * ASM type of {@link StringBuilder}
         */
        STRING_BUILDER_TYPE = getType(StringBuilder.class),
        /**
         * ASM type of {@link TextModel}
         */
        TEXT_MODEL_TYPE = getType(TextModel.class);
        ///////////////////////////////////////////////////////////////////////////
        // Strings
        ///////////////////////////////////////////////////////////////////////////
        /* ******************************************* Parts of this API ******************************************* */
        /**
         * Prefix of generated fields after which the index will go
         */
        protected static final String GENERATED_FIELD_NAME_PREFIX = "d",
        /**
         * Name of parent generic in current context
         */
        PARENT_T_GENERIC_DESCRIPTOR = "TT;",
        /* ********************************************** Method names ********************************************** */
        /**
         * Name of constructor-method
         */
        CONSTRUCTOR_METHOD_NAME = "<init>",
        /**
         * Name of {@link TextModel#getText(Object)} method
         */
        GET_TEXT_METHOD_NAME = "getText",
        /**
         * Name of {@link StringBuilder}{@code .append(}<i>?</i>i{@code )} method
         */
        APPEND_METHOD_NAME = "append",
        /**
         * Name of {@link Object#toString()} method
         */
        TO_STRING_METHOD_NAME = "toString",
        /* ********************************************* Internal names ********************************************* */
        /**
         * Internal name of {@link Object}
         */
        OBJECT_INTERNAL_NAME = OBJECT_TYPE.getInternalName(),
        /**
         * Internal name of {@link StringBuilder}
         */
        STRING_BUILDER_INTERNAL_NAME = STRING_BUILDER_TYPE.getInternalName(),
        /**
         * Internal name of {@link TextModel}
         */
        TEXT_MODEL_INTERNAL_NAME = TEXT_MODEL_TYPE.getInternalName(),
        /* ********************************************** Descriptors ********************************************** */
        /**
         * Descriptor of {@link Object}
         */
        OBJECT_DESCRIPTOR = OBJECT_TYPE.getDescriptor(),
        /**
         * Descriptor of {@link String}
         */
        STRING_DESCRIPTOR = STRING_TYPE.getDescriptor(),
        /**
         * Descriptor of {@link StringBuilder}
         */
        STRING_BUILDER_DESCRIPTOR = STRING_BUILDER_TYPE.getDescriptor(),
        /**
         * Descriptor of {@link TextModel}
         */
        TEXT_MODEL_DESCRIPTOR = TEXT_MODEL_TYPE.getDescriptor(),
        /* ********************************** Method descriptors (aka signatures) ********************************** */
        /**
         * Signature of {@code TextModel(Object)} method
         */
        STRING_OBJECT_METHOD_DESCRIPTOR = getMethodType(STRING_TYPE, OBJECT_TYPE).getDescriptor(),
        /**
         * Signature of {@code void()} method
         */
        VOID_METHOD_DESCRIPTOR = getMethodType(VOID_TYPE).getDescriptor(),
        /**
         * Signature of {@code void(int)} method
         */
        VOID_INT_METHOD_DESCRIPTOR = getMethodType(VOID_TYPE, INT_TYPE).getDescriptor(),
        /**
         * Signature of {@code String()} method
         */
        STRING_METHOD_SIGNATURE = getMethodDescriptor(STRING_TYPE),
        /**
         * Signature of {@code StringBuilder(String)} method
         */
        STRING_BUILDER_STRING_METHOD_SIGNATURE = getMethodDescriptor(STRING_BUILDER_TYPE, STRING_TYPE),
        /**
         * Generic signature of {@link TextModel#getText(Object)} method
         */
        STRING_GENERIC_T_METHOD_SIGNATURE = '(' + PARENT_T_GENERIC_DESCRIPTOR + ')' + STRING_DESCRIPTOR,
        /* ******************************************* Generic signatures ******************************************* */
        /**
         * Generic descriptor of {@link TextModel}
         */
        TEXT_MODEL_SIGNATURE
                = 'L' + TEXT_MODEL_INTERNAL_NAME + '<' + PARENT_T_GENERIC_DESCRIPTOR + ">;",
        /**
         * Generic signature of the generated class
         *
         * @see #PARENT_T_GENERIC_DESCRIPTOR name of the parent generic type
         */
        GENERIC_CLASS_SIGNATURE
                = "<T:" + OBJECT_DESCRIPTOR + '>' + OBJECT_DESCRIPTOR + TEXT_MODEL_SIGNATURE;

        ///////////////////////////////////////////////////////////////////////////
        // Ints
        ///////////////////////////////////////////////////////////////////////////
        /* *************************************** Precomputed string lengths *************************************** */
        /**
         * Length of {@link #TEXT_MODEL_DESCRIPTOR}
         */
        private final int TEXT_MODEL_DESCRIPTOR_LENGTH = TEXT_MODEL_DESCRIPTOR.length(),
        /**
         * Length of {@link #TEXT_MODEL_SIGNATURE}
         */
        TEXT_MODEL_GENERIC_DESCRIPTOR_LENGTH = TEXT_MODEL_SIGNATURE.length();

        ///////////////////////////////////////////////////////////////////////////
        // Constant String arrays
        ///////////////////////////////////////////////////////////////////////////
        /* ***************************************** Arrays of descriptors ***************************************** */
        /**
         * Array whose only value is {@link #TEXT_MODEL_INTERNAL_NAME}.
         */
        protected static final String[] TEXT_MODEL_JVM_CLASS_NAME_ARRAY = new String[]{TEXT_MODEL_INTERNAL_NAME};

        /* ****************************************** Stored multi-opcodes ****************************************** */
        /**
         * Result of {@link Opcodes#ACC_PUBLIC} and {@link Opcodes#ACC_FINAL} flags disjunction
         */
        private static final int OPCODES_ACC_PUBLIC_FINAL = ACC_PUBLIC | ACC_FINAL;
        /**
         * Result of {@link Opcodes#ACC_PUBLIC}, {@link Opcodes#ACC_FINAL}
         * and {@link Opcodes#ACC_SUPER} flags disjunction
         */
        private static final int OPCODES_ACC_PUBLIC_FINAL_SUPER = OPCODES_ACC_PUBLIC_FINAL | ACC_SUPER;

        //</editor-fold>

        @Override
        protected TextModel<T> performTextModelCreation(final boolean release) {
            val clazz = new ClassWriter(0); // MAXs are already computed 😎

            //<editor-fold desc="ASM class generation" defaultstate="collapsed">
            val className = CLASS_NAMING_STRATEGY.get();

            // ASM does not provide any comfortable method fot this :(
            // PS yet ASM is <3
            val internalClassName = className.replace('.', '/');
            clazz.visit(
                    V1_8 /* generate bytecode for JVM1.8 */, OPCODES_ACC_PUBLIC_FINAL_SUPER,
                    internalClassName, GENERIC_CLASS_SIGNATURE, OBJECT_INTERNAL_NAME /* inherit Object */,
                    TEXT_MODEL_JVM_CLASS_NAME_ARRAY /* implement TextModel interface */
            );
            { // Add fields to store passed dynamic text models
                for (var i = 0; i < dynamicElementCount; i++) clazz
                        .visitField(
                                OPCODES_ACC_PUBLIC_FINAL, GENERATED_FIELD_NAME_PREFIX + i /* name prefix + index */,
                                TEXT_MODEL_DESCRIPTOR /* field type is TextModel<T> */,
                                TEXT_MODEL_SIGNATURE, null /* no default value [*] */
                        )
                        .visitEnd();
                // [*] Default value could have been anything what can be stored in const pool :(
                // It would be much easier if our values could have been passed there instead of constructor injection
            }

            MethodVisitor method; // reused method visitor field
            { // Add constructor through which all field get set
                final StringBuilder descriptor = new StringBuilder(
                        3 + TEXT_MODEL_DESCRIPTOR_LENGTH * dynamicElementCount
                ).append('('),
                        signature = new StringBuilder(
                                3 + TEXT_MODEL_GENERIC_DESCRIPTOR_LENGTH * dynamicElementCount
                        ).append('(');
                for (var i = 0; i < dynamicElementCount; i++) {
                    descriptor.append(TEXT_MODEL_DESCRIPTOR);
                    signature.append(TEXT_MODEL_SIGNATURE);
                }
                // Add constructor `void <init>(TextModel[...TextModel])`
                method = clazz.visitMethod(
                        ACC_PUBLIC, CONSTRUCTOR_METHOD_NAME,
                        descriptor.append(')').append('V').toString(),
                        signature.append(')').append('V').toString(), null
                );

                method.visitCode();
                //<editor-fold desc="Constructor code generation" defaultstate="collapsed">
                // Get `this`
                method.visitVarInsn(ALOAD, 0);
                // Invoke Object constructor
                method.visitMethodInsn(
                        INVOKESPECIAL, OBJECT_INTERNAL_NAME, CONSTRUCTOR_METHOD_NAME, VOID_METHOD_DESCRIPTOR, false
                );
                // Set each field's value to constructor's parameter at the corresponding index
                for (var i = 0; i < dynamicElementCount; i++) {
                    method.visitVarInsn(ALOAD, 0);
                    method.visitVarInsn(ALOAD, i + 1);
                    method.visitFieldInsn(
                            PUTFIELD, internalClassName, GENERATED_FIELD_NAME_PREFIX + i,
                            TEXT_MODEL_DESCRIPTOR
                    );
                }
                // Return from method
                method.visitInsn(RETURN);
                //</editor-fold>

                method.visitMaxs(
                        2 /* Always 2 for simple field-settings constructors */,
                        dynamicElementCount + 1 /* {parameters} + [this] */
                );
                method.visitEnd();
            }

            val dynamicModels = new TextModel[dynamicElementCount];
            { // Add `TextModel#getText(T)` method
                // Implement `String getText(T)` method
                method = clazz.visitMethod(
                        ACC_PUBLIC, GET_TEXT_METHOD_NAME, STRING_OBJECT_METHOD_DESCRIPTOR,
                        STRING_GENERIC_T_METHOD_SIGNATURE, null
                );

                method.visitCode();
                //<editor-fold desc="Method code generation" defaultstate="collapsed">
                method.visitTypeInsn(NEW, STRING_BUILDER_INTERNAL_NAME);
                method.visitInsn(DUP);
                // Specify initial length of StringBuilder via its constructor
                {
                    val length = staticLength;
                    switch (length) {
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
                            if (length <= Byte.MAX_VALUE) method.visitIntInsn(BIPUSH, length);
                            else if (length <= Short.MAX_VALUE) method.visitIntInsn(SIPUSH, length);
                            else method.visitLdcInsn(length);
                        }
                    }
                }
                // Call constructor `StringBuilder(int)`
                method.visitMethodInsn(
                        INVOKESPECIAL, STRING_BUILDER_INTERNAL_NAME,
                        CONSTRUCTOR_METHOD_NAME, VOID_INT_METHOD_DESCRIPTOR, false
                );

                { // Append text to string builder
                    int dynamicIndex = -1;

                    for (val element : elements) {
                        // Load static text value from dynamic constant
                        if (element.isDynamic()) {
                            dynamicModels[++dynamicIndex] = element.getDynamicContent();

                            // Get `this`
                            method.visitVarInsn(ALOAD, 0);
                            // Get value of field storing dynamic value
                            method.visitFieldInsn(
                                    GETFIELD, internalClassName, GENERATED_FIELD_NAME_PREFIX + dynamicIndex,
                                    TEXT_MODEL_DESCRIPTOR
                            );
                            // Get value of the field
                            method.visitVarInsn(ALOAD, 1);
                            // Invoke `TextModel.getText(T)` on field's value
                            method.visitMethodInsn(
                                    INVOKEINTERFACE, TEXT_MODEL_INTERNAL_NAME, GET_TEXT_METHOD_NAME,
                                    STRING_OBJECT_METHOD_DESCRIPTOR, true
                            );
                        } else method.visitLdcInsn(element.getStaticContent()); // get constant String value

                        // Invoke `StringBuilder.append(String)`
                        method.visitMethodInsn(
                                INVOKEVIRTUAL, STRING_BUILDER_INTERNAL_NAME,
                                APPEND_METHOD_NAME, STRING_BUILDER_STRING_METHOD_SIGNATURE, false
                        );
                    }
                }

                method.visitMethodInsn(
                        INVOKEVIRTUAL, STRING_BUILDER_INTERNAL_NAME,
                        TO_STRING_METHOD_NAME, STRING_METHOD_SIGNATURE, false
                );
                // Return String from method
                method.visitInsn(ARETURN);
                //</editor-fold>

                method.visitMaxs(3, 2 /* [StringBuilder instance] + [this|appended value] */);
                method.visitEnd();
            }
            clazz.visitEnd();
            //</editor-fold>

            val constructorSignature = new Class<?>[dynamicElementCount];
            Arrays.fill(constructorSignature, TextModel.class);
            try {
                val constructor = ClassFactory.defineGCClass(className, clazz.toByteArray())
                        .getDeclaredConstructor(constructorSignature);
                constructor.setAccessible(true);
                //noinspection unchecked,RedundantCast
                return (TextModel<T>) constructor.newInstance((Object[]) dynamicModels);
            } catch (final NoSuchMethodException | InstantiationException
                    | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Could not compile and instantiate TextModel from the given elements");
            }
        }
    }
}
