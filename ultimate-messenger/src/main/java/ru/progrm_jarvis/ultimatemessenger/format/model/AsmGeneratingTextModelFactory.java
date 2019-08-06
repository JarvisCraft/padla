package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import ru.progrm_jarvis.javacommons.classload.ClassFactory;
import ru.progrm_jarvis.javacommons.lazy.Lazy;
import ru.progrm_jarvis.javacommons.util.ClassNamingStrategy;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static org.objectweb.asm.Opcodes.*;

/**
 * Implementation of {@link TextModelFactory text model factory} which uses runtime class generation.
 */
public class AsmGeneratingTextModelFactory<T> implements TextModelFactory<T> {

    /**
     * Lazy singleton of this text model factory
     */
    private static final Lazy<AsmGeneratingTextModelFactory> INSTANCE
            = Lazy.createThreadSafe(AsmGeneratingTextModelFactory::new);

    /**
     * Returns this {@link TextModelFactory text model factory} singleton.
     *
     * @param <T> generic type of got {@link TextModelFactory text model factory}
     * @return shared instance of this {@link TextModelFactory text model factory}
     */
    @SuppressWarnings("unchecked")
    public static <T> AsmGeneratingTextModelFactory<T> get() {
        return INSTANCE.get();
    }

    /**
     * Class naming strategy used to allocate names for generated classes
     */
    @NonNull private static final ClassNamingStrategy CLASS_NAMING_STRATEGY = ClassNamingStrategy
            .createPaginated(AsmGeneratingTextModelFactory.class.getCanonicalName() + "$$generated$$");

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

        //<editor-fold desc="Bytecode generation constants" defaultstate="collapsed">

        /* *********************************** JVM type names without delimiters *********************************** */
        /**
         * Prefix of generated fields after which the index will go
         */
        protected static final String GENERATED_FIELD_NAME_PREFIX = "d",
        /**
         * Name of constructor-method
         */
        CONSTRUCTOR_METHOD_NAME = "<init>",
        /**
         * Name of {@link TextModel#getText(Object)} method
         */
        GET_TEXT_METHOD_NAME = "getText",
        /**
         * Name of {@code StringBuilder#append(}<i>?</i>i{@code )} method
         */
        APPEND_METHOD_NAME = "append",
        /**
         * Name of {@link Object#toString()} method
         */
        TO_STRING_METHOD_NAME = "toString",
        /**
         * Signature of {@code void ()} method
         */
        VOID_METHOD_SIGNATURE = "()V",
        /**
         * Signature of {@code void (int)} method
         */
        VOID_INT_METHOD_SIGNATURE = "(I)V",
        /**
         * JVM-name of {@link Object} class
         */
        OBJECT_JVM_CLASS_NAME = toBytecodeClassName(Object.class),
        /**
         * JVM-name of {@link StringBuilder} class
         */
        STRING_BUILDER_JVM_CLASS_NAME = toBytecodeClassName(StringBuilder.class),
        /**
         * JVM-name of {@link TextModel} class
         */
        TEXT_MODEL_JVM_CLASS_NAME = toBytecodeClassName(TextModel.class),

        /* ************************************ JVM type names with descriptors ************************************ */
        /**
         * JVM-name of {@link Object} class with its descriptor
         */
        OBJECT_JVM_CLASS_NAME_WITH_DESCRIPTOR = 'L' + OBJECT_JVM_CLASS_NAME + ';',
        /**
         * JVM-name of {@link String} class with its descriptor
         */
        STRING_JVM_CLASS_NAME_WITH_DESCRIPTOR = 'L' + toBytecodeClassName(String.class) + ';',
        /**
         * JVM-name of {@link StringBuilder} class
         */
        STRING_BUILDER_JVM_CLASS_NAME_WITH_DESCRIPTOR = 'L' + toBytecodeClassName(StringBuilder.class) + ';',
        /**
         * JVM-name of {@link TextModel} class
         */
        TEXT_MODEL_JVM_CLASS_NAME_WITH_DESCRIPTOR = 'L' + TEXT_MODEL_JVM_CLASS_NAME + ';',
        /**
         * Signature of {@link TextModel#getText(Object)} method
         */
        GET_TEXT_METHOD_JVM_SIGNATURE
                = '(' + OBJECT_JVM_CLASS_NAME_WITH_DESCRIPTOR + ')' + STRING_JVM_CLASS_NAME_WITH_DESCRIPTOR,

        /* ************************************* Generic-related JVM type names ************************************* */
        /**
         * JVM-name of parent generic type
         */
        PARENT_GENERIC_JVM_CLASS_NAME_WITH_DESCRIPTOR = "TT;",
        /**
         * JVM-name of {@link TextModel} with its descriptor
         */
        TEXT_MODEL_JVM_CLASS_NAME_WITH_GENERIC_AND_DESCRIPTOR
                = 'L' + TEXT_MODEL_JVM_CLASS_NAME + '<' + PARENT_GENERIC_JVM_CLASS_NAME_WITH_DESCRIPTOR + ">;",

        /* *********************************************** Signatures *********************************************** */
        /**
         * Generic signature of the generated class
         *
         * @see #PARENT_GENERIC_JVM_CLASS_NAME_WITH_DESCRIPTOR name of the parent generic type
         */
        GENERIC_CLASS_SIGNATURE
                = "<T:" + OBJECT_JVM_CLASS_NAME_WITH_DESCRIPTOR + '>' + OBJECT_JVM_CLASS_NAME_WITH_DESCRIPTOR
                + TEXT_MODEL_JVM_CLASS_NAME_WITH_GENERIC_AND_DESCRIPTOR,
        /**
         * Generic signature of {@link TextModel#getText(Object)} method
         */
        GET_TEXT_METHOD_JVM_GENERIC_SIGNATURE
                = '(' + PARENT_GENERIC_JVM_CLASS_NAME_WITH_DESCRIPTOR + ')' + STRING_JVM_CLASS_NAME_WITH_DESCRIPTOR,
        /**
         * Signature of {@link Object#toString()} method
         */
        TO_STRING_METHOD_SIGNATURE = "()" + STRING_JVM_CLASS_NAME_WITH_DESCRIPTOR,
        /**
         * Generic signature of {@link StringBuilder#append(String)} method
         */
        APPEND_STRING_METHOD_SIGNATURE
                = '(' + STRING_JVM_CLASS_NAME_WITH_DESCRIPTOR + ')' + STRING_BUILDER_JVM_CLASS_NAME_WITH_DESCRIPTOR;

        /* *************************************** Precomputed string lengths *************************************** */
        /**
         * Length of {@link #TEXT_MODEL_JVM_CLASS_NAME_WITH_DESCRIPTOR}
         */
        private final int TEXT_MODEL_JVM_CLASS_NAME_WITH_DESCRIPTOR_LENGTH
                = TEXT_MODEL_JVM_CLASS_NAME_WITH_DESCRIPTOR.length(),
        /**
         * Length of {@link #TEXT_MODEL_JVM_CLASS_NAME_WITH_GENERIC_AND_DESCRIPTOR}
         */
        TEXT_MODEL_JVM_CLASS_NAME_WITH_GENERIC_AND_DESCRIPTOR_LENGTH
                = TEXT_MODEL_JVM_CLASS_NAME_WITH_GENERIC_AND_DESCRIPTOR.length();

        /* *********************************** Constant arrays of JVM type names *********************************** */
        /**
         * Array whose only value is {@link #TEXT_MODEL_JVM_CLASS_NAME}.
         */
        protected static final String[] TEXT_MODEL_JVM_CLASS_NAME_ARRAY = new String[]{TEXT_MODEL_JVM_CLASS_NAME};

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

        /**
         * Converts the given class name to the corresponding bytecode class name.
         *
         * @param className name of the class at runtime (using dot delimiter)
         * @return name of the class in bytecode
         *
         * @see #toBytecodeClassName(Class)
         */
        protected static String toBytecodeClassName(@NonNull final String className) {
            return className.replace('.', '/');
        }

        /**
         * Gets the given class's name used in bytecode.
         *
         * @param clazz class whose bytecode name should be computed
         * @return name of the class in bytecode
         *
         * @see #toBytecodeClassName(String)
         */
        protected static String toBytecodeClassName(@NonNull final Class<?> clazz) {
            return toBytecodeClassName(clazz.getCanonicalName());
        }

        @Override
        protected TextModel<T> performTextModelCreation(final boolean release) {
            val clazz = new ClassWriter(0); // MAXs are already computed 😎

            //<editor-fold desc="ASM class generation" defaultstate="collapsed">
            val className = CLASS_NAMING_STRATEGY.get();
            val bytecodeClassName = toBytecodeClassName(className);
            clazz.visit(
                    V1_8 /* generate bytecode for JVM1.8 */, OPCODES_ACC_PUBLIC_FINAL_SUPER,
                    bytecodeClassName, GENERIC_CLASS_SIGNATURE, OBJECT_JVM_CLASS_NAME /* inherit Object */,
                    TEXT_MODEL_JVM_CLASS_NAME_ARRAY /* implement TextModel interface */
            );
            { // Add fields to store passed dynamic text models
                for (var i = 0; i < dynamicElementCount; i++) clazz
                        .visitField(
                                OPCODES_ACC_PUBLIC_FINAL, GENERATED_FIELD_NAME_PREFIX + i /* name prefix + index */,
                                TEXT_MODEL_JVM_CLASS_NAME_WITH_DESCRIPTOR /* field type is TextModel<T> */,
                                TEXT_MODEL_JVM_CLASS_NAME_WITH_GENERIC_AND_DESCRIPTOR, null /* no default value [*] */
                        )
                        .visitEnd();
                // [*] Default value could have been anything what can be stored in const pool :(
                // It would be much easier if our values could have been passed there instead of constructor injection
            }

            MethodVisitor method; // reused method visitor field
            { // Add constructor through which all field get set
                final StringBuilder descriptor = new StringBuilder(
                        3 + TEXT_MODEL_JVM_CLASS_NAME_WITH_DESCRIPTOR_LENGTH * dynamicElementCount
                ).append('('),
                        signature = new StringBuilder(
                                3 + TEXT_MODEL_JVM_CLASS_NAME_WITH_GENERIC_AND_DESCRIPTOR_LENGTH * dynamicElementCount
                        ).append('(');
                for (var i = 0; i < dynamicElementCount; i++) {
                    descriptor.append(TEXT_MODEL_JVM_CLASS_NAME_WITH_DESCRIPTOR);
                    signature.append(TEXT_MODEL_JVM_CLASS_NAME_WITH_GENERIC_AND_DESCRIPTOR);
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
                        INVOKESPECIAL, OBJECT_JVM_CLASS_NAME, CONSTRUCTOR_METHOD_NAME, VOID_METHOD_SIGNATURE, false
                );
                // Set each field's value to constructor's parameter at the corresponding index
                for (var i = 0; i < dynamicElementCount; i++) {
                    method.visitVarInsn(ALOAD, 0);
                    method.visitVarInsn(ALOAD, i + 1);
                    method.visitFieldInsn(
                            PUTFIELD, bytecodeClassName, GENERATED_FIELD_NAME_PREFIX + i,
                            TEXT_MODEL_JVM_CLASS_NAME_WITH_DESCRIPTOR
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
                        ACC_PUBLIC, GET_TEXT_METHOD_NAME, GET_TEXT_METHOD_JVM_SIGNATURE,
                        GET_TEXT_METHOD_JVM_GENERIC_SIGNATURE, null
                );

                method.visitCode();
                //<editor-fold desc="Method code generation" defaultstate="collapsed">
                method.visitTypeInsn(NEW, STRING_BUILDER_JVM_CLASS_NAME);
                method.visitInsn(DUP);
                // Specify initial length of StringBuilder via its constructor
                {
                    val length = staticLength;
                    switch (length) {
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
                            if (length <= 127) method.visitIntInsn(BIPUSH, length);
                            else if (length <= 32767) method.visitIntInsn(SIPUSH, length);
                            else method.visitLdcInsn(length);
                        }
                    }
                }
                // Call constructor `StringBuilder(int)`
                method.visitMethodInsn(
                        INVOKESPECIAL, STRING_BUILDER_JVM_CLASS_NAME,
                        CONSTRUCTOR_METHOD_NAME, VOID_INT_METHOD_SIGNATURE, false
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
                                    GETFIELD, bytecodeClassName, GENERATED_FIELD_NAME_PREFIX + dynamicIndex,
                                    TEXT_MODEL_JVM_CLASS_NAME_WITH_DESCRIPTOR
                            );
                            // Get value of the field
                            method.visitVarInsn(ALOAD, 1);
                            // Invoke `TextModel.getText(T)` on field's value
                            method.visitMethodInsn(
                                    INVOKEINTERFACE, TEXT_MODEL_JVM_CLASS_NAME, GET_TEXT_METHOD_NAME,
                                    GET_TEXT_METHOD_JVM_SIGNATURE, true
                            );
                        } else method.visitLdcInsn(element.getStaticContent()); // get constant String value

                        // Invoke `StringBuilder.append(String)`
                        method.visitMethodInsn(
                                INVOKEVIRTUAL, STRING_BUILDER_JVM_CLASS_NAME,
                                APPEND_METHOD_NAME, APPEND_STRING_METHOD_SIGNATURE, false
                        );
                    }
                }

                method.visitMethodInsn(
                        INVOKEVIRTUAL, STRING_BUILDER_JVM_CLASS_NAME,
                        TO_STRING_METHOD_NAME, TO_STRING_METHOD_SIGNATURE, false
                );
                // Return String from method
                method.visitInsn(ARETURN);
                //</editor-fold>

                method.visitMaxs(3, 2 /* [StringBuilder instance] + [this|appended value] */);
                method.visitEnd();
            }
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
