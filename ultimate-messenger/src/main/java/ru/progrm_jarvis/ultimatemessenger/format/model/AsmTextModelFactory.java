package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.java.Log;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.*;
import ru.progrm_jarvis.javacommons.annotation.Internal;
import ru.progrm_jarvis.javacommons.bytecode.CommonBytecodeLibrary;
import ru.progrm_jarvis.javacommons.bytecode.annotation.UsesBytecodeModification;
import ru.progrm_jarvis.javacommons.bytecode.asm.AsmUtil;
import ru.progrm_jarvis.javacommons.classloading.ClassNamingStrategy;
import ru.progrm_jarvis.javacommons.classloading.GcClassDefiners;
import ru.progrm_jarvis.javacommons.invoke.InvokeUtil;
import ru.progrm_jarvis.javacommons.lazy.Lazy;
import ru.progrm_jarvis.javacommons.object.valuestorage.SimpleValueStorage;
import ru.progrm_jarvis.javacommons.object.valuestorage.ValueStorage;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;
import static ru.progrm_jarvis.javacommons.bytecode.asm.AsmUtil.*;

/**
 * Implementation of {@link TextModelFactory text model factory} which uses runtime class generation.
 *
 * @param <T> type of object according to which the created text models are formatted
 * @param <C> type of configuration used by this text model factory
 */
@Log
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@UsesBytecodeModification(CommonBytecodeLibrary.ASM)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class AsmTextModelFactory<T, C extends AsmTextModelFactory.Configuration> implements TextModelFactory<T> {
    /**
     * Lazy singleton of this text model factory
     */
    private static final @NotNull Lazy<@NotNull TextModelFactory<?>> INSTANCE
            = Lazy.createThreadSafe(AsmTextModelFactory::create);

    /**
     * Internal storage of {@link TextModel dynamic text models} passed to {@code static final} fields.
     */
    private static final @NotNull ValueStorage<@NotNull String, @NotNull TextModel<?>> DYNAMIC_MODELS
            = SimpleValueStorage.create();

    /**
     * Flag indicating the availability of {@code java.lang.invoke.StringConcatFactory}
     */
    private static final boolean STRING_CONCAT_FACTORY_AVAILABLE;

    /**
     * Class of {@code java.lang.invoke.StringConcatFactory}
     */
    private static final @Nullable Class<?> STRING_CONCAT_FACTORY_CLASS;

    static {
        boolean stringConcatFactoryAvailable = false;
        { // StringConcatFactory class lookup attempt
            Class<?> stringConcatFactoryClass;
            try {
                stringConcatFactoryClass = Class.forName("java.lang.invoke.StringConcatFactory");
                stringConcatFactoryAvailable = true;
            } catch (ClassNotFoundException ignored) {
                stringConcatFactoryClass = null;
            } // StringConcatFactory is unavailable
            STRING_CONCAT_FACTORY_CLASS = stringConcatFactoryClass;
        }
        STRING_CONCAT_FACTORY_AVAILABLE = stringConcatFactoryAvailable;

        log.log(Level.FINE, // debug StringConcatFactory availability
                () -> "java.lang.invoke.StringConcatFactory is "
                        + (STRING_CONCAT_FACTORY_AVAILABLE ? "available" : "unavailable")
        );
    }

    /**
     * Configuration of this text model factory.
     *
     * @apiNote generic to support extension
     */
    @NonNull C configuration;

    /**
     * Creates a new ASM-based text model factory with the given configuration.
     *
     * @param configuration configuration to be used by the created text model factory
     * @param <T> type of object according to which the created text models are formatted
     * @return created ASM-based text model factory with the given configuration
     */
    public static <T> @NotNull TextModelFactory<T> create(final @NonNull Configuration configuration) {
        return new AsmTextModelFactory<>(configuration);
    }

    /**
     * Creates a new ASM-based text model factory with the default configuration.
     *
     * @param <T> type of object according to which the created text models are formatted
     * @return created AsmTextModelFactory ASM-based text model factory with the default configuration
     */
    public static <T> @NotNull TextModelFactory<T> create() {
        return create(SimpleConfiguration.getDefault());
    }

    /**
     * Returns this {@link TextModelFactory text model factory} singleton.
     *
     * @param <T> generic type of got {@link TextModelFactory text model factory}
     * @return shared instance of this {@link TextModelFactory text model factory}
     */
    @SuppressWarnings("unchecked")
    public static <T> @NotNull TextModelFactory<T> get() {
        return (TextModelFactory<T>) INSTANCE.get();
    }

    /**
     * Creates a new builder of the configuration.
     *
     * @return builder of a {@link Configuration}
     */
    public static @NotNull ConfigurationBuilder configuration() {
        return SimpleConfiguration.builder();
    }

    @Override
    public @NotNull TextModelFactory.TextModelBuilder<T> newBuilder() {
        return new AsmTextModelBuilder<>(configuration);
    }

    /**
     * Algorithm used for concatenation vid {@code java.lang.invoke.StringConcatFactory}.
     */
    public enum StringConcatFactoryAlgorithm {
        /**
         * Linear algorithm
         */
        VECTOR
        // TREE may be implemented later
    }

    /**
     * Configuration of this {@link AsmTextModelFactory text model factory}
     */
    public interface Configuration {

        /**
         * Tests whether the configured {@link AsmTextModelBuilder text model builder} should attempt to use {@code
         * java.lang.invoke.StringConcatFactory} for {@link String string}-concatenation.
         *
         * @return {@code true} if {@code StringConcatFactory} should be used (if available) for string concatenation
         * and {@code false} otherwise
         */
        @Contract(pure = true)
        boolean enableStringConcatFactory();

        /**
         * Gets the algorithm which should be used used for producing string concatenation logic via {@code
         * java.lang.invoke.StringConcatFactory} when it is {@link #enableStringConcatFactory() enabled}.
         *
         * @return algorithm which should be used for producing string concatenation logic
         */
        @Contract(pure = true)
        @NotNull StringConcatFactoryAlgorithm stringConcatFactoryAlgorithm();
    }

    /**
     * Builder of {@link Configuration}.
     */
    public interface ConfigurationBuilder {

        /**
         * Sets the value of {@link Configuration#enableStringConcatFactory()} for the built configuration.
         *
         * @param enableStringConcatFactory set flag
         * @return this builder
         *
         * @see Configuration#enableStringConcatFactory() meaning
         */
        @Contract("_ -> this")
        @NotNull ConfigurationBuilder enableStringConcatFactory(
                boolean enableStringConcatFactory
        );

        /**
         * Sets the value of {@link Configuration#stringConcatFactoryAlgorithm()} for the built configuration.
         *
         * @param stringConcatFactoryAlgorithm set flag
         * @return this builder
         * @throws NullPointerException if {@code stringConcatFactoryAlgorithm} is {@code null}
         *
         * @see Configuration#stringConcatFactoryAlgorithm() meaning
         */
        @Contract("null -> fail; _ -> this")
        @NotNull ConfigurationBuilder stringConcatFactoryAlgorithm(
                @NonNull StringConcatFactoryAlgorithm stringConcatFactoryAlgorithm
        );

        /**
         * Builds a new configuration from this builder.
         *
         * @return configuration of this object
         */
        @Contract("-> new")
        @NotNull Configuration build();
    }

    /**
     * Implementation of {@link TextModelFactory.TextModelBuilder text model builder} which uses runtime class
     * generation and is capable of joining nearby static text blocks and optimizing {@link #buildAndRelease()}.
     *
     * @param <T> type of object according to which the created text models are formatted
     * @implNote this class is {@code private} so that it is accessible by generated classes
     */
    @ToString
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = true) // simply, why not? :) (this will also allow caching of instances)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    // note: the class is `protected` so that it is available to generated classes
    protected static final class AsmTextModelBuilder<T> extends AbstractGeneratingTextModelFactoryBuilder<
            T, AsmNode<T>, StaticAsmNode<T>, DynamicAsmNode<T>> {

        /**
         * Lookup of this class.
         */
        private static final @NotNull MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

        /**
         * Class naming strategy used to allocate names for generated classes
         */
        private static final @NotNull ClassNamingStrategy CLASS_NAMING_STRATEGY = ClassNamingStrategy.createPaginated(
                AsmTextModelBuilder.class.getName() + "$$Generated$$TextModel$$"
        );

        //<editor-fold desc="Bytecode generation constants" defaultstate="collapsed">

        ///////////////////////////////////////////////////////////////////////////
        // Types
        ///////////////////////////////////////////////////////////////////////////
        /* ******************************************** ASM Type objects ******************************************** */
        /**
         * ASM type of {@link AsmTextModelBuilder}
         */
        private static final @NotNull Type TEXT_MODEL_BUILDER_TYPE = getType(AsmTextModelBuilder.class),
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
        private static final @NotNull String GENERATED_FIELD_NAME_PREFIX = "D",
        /**
         * Name of parent generic in current context
         */
        PARENT_T_GENERIC_DESCRIPTOR = "TT;",
        /* ********************************************** Method names ********************************************** */
        /**
         * Name of {@link TextModel#getText(Object)} method
         */
        GET_TEXT_METHOD_NAME = "getText",
        /**
         * Name of {@link StringBuilder}{@code .append(}<i>?</i>{@code )} method
         */
        APPEND_METHOD_NAME = "append",
        /**
         * Name of {@link AsmTextModelBuilder#internal$getDynamicTextModel(String)} method
         */
        INTERNAL_GET_DYNAMIC_TEXT_MODEL_METHOD_NAME = "internal$getDynamicTextModel",
        /* ********************************************* Internal names ********************************************* */
        /**
         * Internal name of {@link TextModel}
         */
        TEXT_MODEL_BUILDER_INTERNAL_NAME = TEXT_MODEL_BUILDER_TYPE.getInternalName(),
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
         * Descriptor of {@link TextModel}
         */
        TEXT_MODEL_BUILDER_DESCRIPTOR = TEXT_MODEL_BUILDER_TYPE.getDescriptor(),
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
        STRING_OBJECT_METHOD_DESCRIPTOR = getMethodDescriptor(STRING_TYPE, OBJECT_TYPE),
        /**
         * Signature of {@code void(int)} method
         */
        VOID_INT_METHOD_DESCRIPTOR = getMethodDescriptor(VOID_TYPE, INT_TYPE),
        /**
         * Signature of {@code void(String)} method
         */
        VOID_STRING_METHOD_DESCRIPTOR = getMethodDescriptor(VOID_TYPE, STRING_TYPE),
        /**
         * Signature of {@code String()} method
         */
        STRING_METHOD_DESCRIPTOR = getMethodDescriptor(STRING_TYPE),
        /**
         * Signature of {@code StringBuilder(String)} method
         */
        STRING_BUILDER_STRING_METHOD_DESCRIPTOR = getMethodDescriptor(STRING_BUILDER_TYPE, STRING_TYPE),
        /**
         * Signature of {@code TextModel(String)} method
         */
        TEXT_MODEL_STRING_METHOD_DESCRIPTOR = getMethodDescriptor(TEXT_MODEL_TYPE, STRING_TYPE),
        /**
         * Signature of {@code StringBuilder(char)} method
         */
        STRING_BUILDER_CHAR_METHOD_DESCRIPTOR = getMethodDescriptor(STRING_BUILDER_TYPE, CHAR_TYPE),
        /**
         * Generic signature of {@link TextModel#getText(Object)} method
         */
        STRING_GENERIC_T_METHOD_DESCRIPTOR = '(' + PARENT_T_GENERIC_DESCRIPTOR + ')' + STRING_DESCRIPTOR,
        /* ******************************************* Generic signatures ******************************************* */
        /**
         * Generic descriptor of {@link TextModel}
         */
        TEXT_MODEL_SIGNATURE = 'L' + TEXT_MODEL_INTERNAL_NAME + '<' + PARENT_T_GENERIC_DESCRIPTOR + ">;",
        /**
         * Generic signature of the generated class
         *
         * @see #PARENT_T_GENERIC_DESCRIPTOR name of the parent generic type
         */
        GENERIC_CLASS_SIGNATURE = "<T:" + OBJECT_DESCRIPTOR + '>' + OBJECT_DESCRIPTOR + TEXT_MODEL_SIGNATURE;

        ///////////////////////////////////////////////////////////////////////////
        // Ints
        ///////////////////////////////////////////////////////////////////////////
        /* ******************************************* Specific constants ******************************************* */
        /**
         * Maximal amount of dynamic arguments passed to {@code java.lang.invoke.StringConcatFactory} concatenation
         * methods
         */
        private static final int STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS = 200, // according to Javadocs
        /* *************************************** Precomputed string lengths *************************************** */
        /**
         * Length of {@link AsmUtil#STRING_DESCRIPTOR}
         */
        STRING_DESCRIPTOR_LENGTH = STRING_DESCRIPTOR.length(),
        /**
         * Length of {@link #TEXT_MODEL_DESCRIPTOR}
         */
        TEXT_MODEL_DESCRIPTOR_LENGTH = TEXT_MODEL_DESCRIPTOR.length(),
        /**
         * Length of {@link #TEXT_MODEL_SIGNATURE}
         */
        TEXT_MODEL_SIGNATURE_LENGTH = TEXT_MODEL_SIGNATURE.length();

        /**
         * Natural logarithm of {@link #STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS}
         */
        private static final double STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS_LOGARITHM
                = Math.log(STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS);

        /*  *******************************************************************************************************  */
        /* ************************** java.lang.invoke.StringConcatFactory specific stuff ************************** */
        /*  *******************************************************************************************************  */

        /**
         * ASM type of {@code java.lang.invoke.StringConcatFactory}
         */
        private static final @Nullable Type STRING_CONCAT_FACTORY_TYPE;
        /**
         * Name of {@code java.lang.invoke.StringConcatFactory.concat(Lookup, String, MethodType)}
         */
        private static final @NotNull String MAKE_CONCAT_METHOD_NAME = "makeConcat",
        /**
         * Name of {@code java.lang.invoke.StringConcatFactory .makeConcatWithConstants(Lookup, String, MethodType,
         * String, Object[])}
         */
        MAKE_CONCAT_WITH_CONSTANTS_METHOD_NAME = "makeConcatWithConstants";
        /**
         * Handle of {@code java.lang.invoke.StringConcatFactory.concat(Lookup, String, MethodType)}
         */
        private static final @Nullable Handle MAKE_CONCAT_HANDLE,
        /**
         * Handle of {@code java.lang.invoke.StringConcatFactory .makeConcatWithConstants(Lookup, String, MethodType,
         * String, Object[])}
         */
        MAKE_CONCAT_WITH_CONSTANTS_HANDLE;

        /* ************************************************ Nullable ************************************************ */
        /**
         * Internal name of {@link TextModel}
         */
        private static final @Nullable String STRING_CONCAT_FACTORY_INTERNAL_NAME,
        /**
         * Descriptor of {@link TextModel}
         */
        STRING_CONCAT_FACTORY_DESCRIPTOR;

        static {
            if (STRING_CONCAT_FACTORY_AVAILABLE) {
                assert STRING_CONCAT_FACTORY_CLASS != null; // should never happen as there is direct relation

                STRING_CONCAT_FACTORY_TYPE = getType(STRING_CONCAT_FACTORY_CLASS);
                STRING_CONCAT_FACTORY_INTERNAL_NAME = STRING_CONCAT_FACTORY_TYPE.getInternalName();
                STRING_CONCAT_FACTORY_DESCRIPTOR = STRING_CONCAT_FACTORY_TYPE.getDescriptor();
                MAKE_CONCAT_HANDLE = new Handle(
                        H_INVOKESTATIC, STRING_CONCAT_FACTORY_INTERNAL_NAME, MAKE_CONCAT_METHOD_NAME,
                        getMethodDescriptor(CALL_SITE_TYPE, LOOKUP_TYPE, STRING_TYPE, METHOD_TYPE_TYPE), false
                );
                MAKE_CONCAT_WITH_CONSTANTS_HANDLE = new Handle(
                        H_INVOKESTATIC, STRING_CONCAT_FACTORY_INTERNAL_NAME, MAKE_CONCAT_WITH_CONSTANTS_METHOD_NAME,
                        getMethodDescriptor(
                                CALL_SITE_TYPE, LOOKUP_TYPE, STRING_TYPE,
                                METHOD_TYPE_TYPE, STRING_TYPE, OBJECT_ARRAY_TYPE
                        ), false
                );
            } else {
                STRING_CONCAT_FACTORY_TYPE = null;
                STRING_CONCAT_FACTORY_INTERNAL_NAME = STRING_CONCAT_FACTORY_DESCRIPTOR = null;
                MAKE_CONCAT_HANDLE = MAKE_CONCAT_WITH_CONSTANTS_HANDLE = null;
            }
        }

        ///////////////////////////////////////////////////////////////////////////
        // Constant arrays
        ///////////////////////////////////////////////////////////////////////////
        /* ***************************************** Arrays of descriptors ***************************************** */
        /**
         * Array whose only value is {@link #TEXT_MODEL_INTERNAL_NAME}.
         */
        private static final @NotNull String @Unmodifiable @NotNull [] TEXT_MODEL_INTERNAL_NAME_ARRAY
                = {TEXT_MODEL_INTERNAL_NAME};

        //</editor-fold>

        /**
         * Configuration of the parent {@link AsmTextModelFactory text model factory}
         */
        @NonNull Configuration configuration;

        /**
         * Amount of {@link SimpleStaticAsmNode static nodes} whose text should be treated by {@code
         * java.lang.invoke.StringConcatFactory} as the one passed to bootstrap arguments
         *
         * @see SimpleStaticAsmNode#isTreatAsDynamicValueInStringConcatFactory()
         */
        @NonFinal int staticNodeHandledAsDynamicCount,
        /**
         * Length of texts of those {@link SimpleStaticAsmNode static nodes} whose text should be treated by {@code
         * java.lang.invoke.StringConcatFactory} as the one passed to bootstrap arguments
         *
         * @see SimpleStaticAsmNode#isTreatAsDynamicValueInStringConcatFactory()
         */
        staticSpecialNodeLength;

        /**
         * JIT-friendly (folded static final constant allows the JIT to use the fast approach here) checks if {@code
         * java.lang.invoke.StringConcatFactory} should be used by this builder for implementing {@link String string}
         * concatenation.
         *
         * @return {@code true} is {@link String string} concatenation via {@code java.lang.invoke.StringConcatFactory}
         * is available and enabled and {@code false} otherwise
         */
        private boolean isStringConcatFactoryEnabled() {
            return STRING_CONCAT_FACTORY_AVAILABLE && configuration.enableStringConcatFactory();
        }

        @Override
        protected void endModification(final @NotNull StaticAsmNode<T> staticNode) {
            super.endModification(staticNode);

            if (isStringConcatFactoryEnabled() && staticNode.isTreatAsDynamicValueInStringConcatFactory()) {
                staticNodeHandledAsDynamicCount++;
                staticSpecialNodeLength += staticNode.getTextLength();
            }
        }

        @Override
        protected @NotNull AsmNode<T> newStaticNode(final @NotNull String text) {
            return SimpleStaticAsmNode.from(text);
        }

        @Override
        protected @NotNull AsmNode<T> newDynamicNode(final @NotNull TextModel<T> content) {
            return SimpleDynamicAsmNode.from(content);
        }

        /**
         * Retrieves (gets and removes) {@link TextModel dynamic text model} stored in {@link #DYNAMIC_MODELS} by the
         * given key.
         *
         * @param uniqueKey unique key by which the value should be retrieved
         * @return dynamic text model stored by the given unique key
         *
         * @deprecated this method is internal
         */
        @Deprecated
        @Internal("This is expected to be invoked only by generated TextModels to initialize their fields")
        public static @NotNull TextModel<?> internal$getDynamicTextModel(final @NotNull String uniqueKey) {
            return DYNAMIC_MODELS.retrieveValue(uniqueKey);
        }

        @Override
        protected @NotNull TextModel<T> performTextModelBuild(final boolean release) {
            final ClassWriter clazz;
            //<editor-fold desc="ASM class generation" defaultstate="collapsed">
            final String className;
            final String internalClassName;
            (clazz = new ClassWriter(0) /* MAXs are already computed :sunglasses: */).visit(
                    V1_8, OPCODES_ACC_PUBLIC_FINAL_SUPER,
                    internalClassName = classNameToInternalName(className = CLASS_NAMING_STRATEGY.get()),
                    GENERIC_CLASS_SIGNATURE, OBJECT_INTERNAL_NAME /* inherit Object */,
                    TEXT_MODEL_INTERNAL_NAME_ARRAY /* implement TextModel interface */
            );
            // add an empty constructor
            addEmptyConstructor(clazz);

            if (isStringConcatFactoryEnabled()) asm$implementGetTextMethodViaStringConcatFactory(
                    clazz, internalClassName
            );
            else asm$implementGetTextMethodViaStringBuilder(clazz, internalClassName);

            clazz.visitEnd();
            //</editor-fold>

            final MethodHandle constructor;
            {
                final Class<? extends TextModel<T>> definedClass = uncheckedClassCast(
                        GcClassDefiners.getDefault()
                                .defineClass(LOOKUP, className, clazz.toByteArray())
                );

                try {
                    constructor = LOOKUP.findConstructor(definedClass, InvokeUtil.VOID__METHOD_TYPE);
                } catch (final NoSuchMethodException | IllegalAccessException e) {
                    throw new AssertionError(
                            "Generated class " + className + " should contain an available empty constructor", e
                    );
                }
            }
            try {
                // note: `invokeExact()` cannot be used as return-type is only resolved at runtime
                // because the constructed class is generated
                return uncheckedTextModelCast((TextModel<?>) constructor.invoke());
            } catch (final Throwable x) {
                throw new AssertionError(
                        "Generated class " + className + " cannot be instantiated", x
                );
            }
        }

        /**
         * Casts the given class object into the specific one.
         *
         * @param type raw-typed class object
         * @param <T> exact wanted type of class object
         * @return the provided class object with its type cast to the specific one
         *
         * @apiNote this is effectively no-op
         */
        // note: no nullability annotations are present on parameter and return type as cast of `null` is also safe
        @Contract("_ -> param1")
        @SuppressWarnings("unchecked")
        private static <T> Class<T> uncheckedClassCast(final Class<?> type) {
            return (Class<T>) type;
        }

        /**
         * Casts the given text model into the specific one.
         *
         * @param textModel raw-typed text model
         * @param <T> exact wanted type of text model
         * @return the provided text model with its type cast to the specific one
         *
         * @apiNote this is effectively no-op
         */
        // note: no nullability annotations are present on parameter and return type as cast of `null` is also safe
        @Contract("_ -> param1")
        @SuppressWarnings("unchecked")
        private static <T> TextModel<T> uncheckedTextModelCast(final TextModel<?> textModel) {
            return (TextModel<T>) textModel;
        }

        /**
         * Implements the {@link TextModel#getText(Object)} method in the generated class via {@code
         * java.lang.invoke.StringConcatFactory}.
         *
         * @param clazz class-writer used for generating the implementation
         * @param internalClassName internal name of the generated class
         */
        private void asm$implementGetTextMethodViaStringBuilder(final @NotNull ClassWriter clazz,
                                                                final @NotNull String internalClassName) {
            // Implement `TextModel#getText(T)` method and add fields
            final MethodVisitor method;
            (method = clazz.visitMethod(
                    ACC_PUBLIC, GET_TEXT_METHOD_NAME, STRING_OBJECT_METHOD_DESCRIPTOR,
                    STRING_GENERIC_T_METHOD_DESCRIPTOR, null
            )).visitCode();

            //<editor-fold desc="Method code generation" defaultstate="collapsed">
            {
                final MethodVisitor staticInitializer;
                (staticInitializer = visitStaticInitializer(clazz)).visitCode();

                final int staticLength;
                if ((staticLength = this.staticLength) == 0) { // there are no static nodes (and at least 2 dynamic)
                    /* ************************ Invoke `StringBuilder(int)` constructor ************************ */
                    String fieldName = GENERATED_FIELD_NAME_PREFIX + 0;
                    // Specify first `StringBuilder` node
                    asm$pushStaticTextModelFieldGetTextInvocationResult(method, internalClassName, fieldName);
                    // allocate new `StringBuilder`
                    method.visitTypeInsn(NEW, STRING_BUILDER_INTERNAL_NAME);
                    // duplicate `StringBuilder` having the last one pushed to the end of stack
                    method.visitInsn(DUP_X1);
                    // swap the first two stack nodes so that the `StringBuilder`'s constructor
                    // gets invoked with the needed parameter { SB = `StringBuilder`}:
                    // { StringBuilder, Parameter, StringBuilder } --> { StringBuilder, StringBuilder, Parameter }
                    // this tricks allows the max stack size be the same as if it was a call to #append(String)
                    // not having it increased by one for (bad case) { StringBuilder, StringBuilder, T, TextModel }
                    method.visitInsn(SWAP);
                    // Call constructor `StringBuilder(int)`
                    method.visitMethodInsn(
                            INVOKESPECIAL, STRING_BUILDER_INTERNAL_NAME,
                            CONSTRUCTOR_METHOD_NAME, VOID_STRING_METHOD_DESCRIPTOR, false
                    );

                    final Iterator<AsmNode<T>> iterator;
                    asm$addStaticFieldWithInitializer(
                            clazz, internalClassName, staticInitializer,
                            fieldName, (iterator = nodes.iterator()).next().asDynamic().getContent()
                    );
                    // dynamic nodes count is at least 2
                    var dynamicIndex = 0;
                    while (iterator.hasNext()) {
                        fieldName = GENERATED_FIELD_NAME_PREFIX + ++dynamicIndex;

                        asm$addStaticFieldWithInitializer(
                                clazz, internalClassName, staticInitializer,
                                fieldName, iterator.next().asDynamic().getContent()
                        );
                        asm$pushStaticTextModelFieldGetTextInvocationResult(method, internalClassName, fieldName);
                        asm$invokeStringBuilderAppendString(method);
                    }


                    /*
                     * As there are only dynamic nodes the maximal stack size is when it consists of:
                     * - the StringBuilder instance
                     * - the `TextModel` whose `getText` is being invoked
                     * - the parameter passed to `getText`
                     */
                } else { // there are static nodes
                    /* ************************ Invoke `StringBuilder(int)` constructor ************************ */
                    method.visitTypeInsn(NEW, STRING_BUILDER_INTERNAL_NAME);
                    method.visitInsn(DUP);
                    // Specify initial length of StringBuilder via its constructor
                    pushInt(method, staticLength);
                    // Call constructor `StringBuilder(int)`
                    method.visitMethodInsn(
                            INVOKESPECIAL, STRING_BUILDER_INTERNAL_NAME,
                            CONSTRUCTOR_METHOD_NAME, VOID_INT_METHOD_DESCRIPTOR, false
                    );
                    /* ********************************** Append all nodes ********************************** */
                    var dynamicIndex = -1;
                    // Lists are commonly faster with random access
                    for (val node : nodes) if (node.isDynamic()) { // Load static text value from dynamic constant
                        final String fieldName;
                        asm$addStaticFieldWithInitializer(
                                clazz, internalClassName, staticInitializer,
                                fieldName = GENERATED_FIELD_NAME_PREFIX + ++dynamicIndex,
                                node.asDynamic().getContent()
                        );
                        asm$pushStaticTextModelFieldGetTextInvocationResult(
                                method, internalClassName, fieldName
                        );
                        asm$invokeStringBuilderAppendString(method);
                    } else {
                        final String staticText;
                        if ((staticText = node.asStatic().getText()).length() == 1) {
                            pushCharUnsafely(method, staticText.charAt(0));
                            asm$invokeStringBuilderAppendChar(method);
                        } else {
                            method.visitLdcInsn(node.asStatic().getText()); // get constant String value
                            asm$invokeStringBuilderAppendString(method);
                        }
                    }
                    /*
                     * As there are dynamic nodes the maximal stack size is when it consists of:
                     * - the StringBuilder instance
                     * - the `TextModel` whose `getText` is being invoked
                     * - the parameter passed to `getText`
                     */
                }
                method.visitMaxs(3, 2 /* [this + local variable] */);

                staticInitializer.visitInsn(RETURN);
                staticInitializer.visitMaxs(2, 0);
                staticInitializer.visitEnd();
            }

            // invoke `StringBuilder#toString()`
            method.visitMethodInsn(
                    INVOKEVIRTUAL, STRING_BUILDER_INTERNAL_NAME,
                    TO_STRING_METHOD_NAME, STRING_METHOD_DESCRIPTOR, false
            );
            // Return String from method
            method.visitInsn(ARETURN);
            //</editor-fold>

            // Note: visitMaxs() happens above
            method.visitEnd();
        }

        /**
         * Implements the {@link TextModel#getText(Object)} method in the generated class via {@link StringBuilder}.
         *
         * @param clazz class-writer used for generating the implementation
         * @param internalClassName internal name of the generated class
         */
        private void asm$implementGetTextMethodViaStringConcatFactory(final @NotNull ClassWriter clazz,
                                                                      final @NotNull String internalClassName
        ) {
            // The Lookup will be needed by the runtime for `invokedynamic` usage
            addLookup(clazz);

            // Implement `TextModel#getText(T)` method and add fields
            final MethodVisitor method;
            (method = clazz.visitMethod(
                    ACC_PUBLIC, GET_TEXT_METHOD_NAME, STRING_OBJECT_METHOD_DESCRIPTOR,
                    STRING_GENERIC_T_METHOD_DESCRIPTOR, null
            )).visitCode();

            //<editor-fold desc="Method code generation" defaultstate="collapsed">
            {
                final MethodVisitor staticInitializer;
                (staticInitializer = visitStaticInitializer(clazz)).visitCode();

                final int dynamicNodeCount;
                if ((dynamicNodeCount = this.dynamicNodeCount) <= STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS) {
                    // The amount of dynamic nodes does not exceed the maximal amount of those
                    // passed into the `StringConcatFactory`'s `makeConcat` methods
                    //<editor-fold desc="Fast implementation" defaultstate="collapsed">
                    final int staticLength;
                    if ((staticLength = this.staticLength) == 0) { // there only are dynamic nodes
                        // dynamic nodes count is at least 2
                        var dynamicIndex = -1;
                        // add fields containing dynamic nodes and their invocation
                        for (val node : nodes) {
                            final String fieldName;
                            asm$addStaticFieldWithInitializer(
                                    clazz, internalClassName, staticInitializer,
                                    fieldName = GENERATED_FIELD_NAME_PREFIX + ++dynamicIndex,
                                    node.asDynamic().getContent()
                            );
                            asm$pushStaticTextModelFieldGetTextInvocationResult(method, internalClassName, fieldName);
                        }

                        method.visitInvokeDynamicInsn(
                                MAKE_CONCAT_METHOD_NAME, DescriptorCache.stringsToStringDescriptor(dynamicNodeCount),
                                MAKE_CONCAT_HANDLE /* no bootstrap arguments */
                        );
                    } else {
                        final int staticNodeHandledAsDynamicCount;
                        if ((staticNodeHandledAsDynamicCount = this.staticNodeHandledAsDynamicCount) == 0) {
                            // there are static nodes
                            val recipe = new StringBuilder(staticLength + dynamicNodeCount);

                            var dynamicIndex = -1;
                            // Lists are commonly faster with random access
                            for (val node : nodes) if (node.isDynamic()) {
                                final String fieldName;
                                // push String (got from dynamic TextModel's `getText(T)` invocation) ...
                                asm$addStaticFieldWithInitializer(
                                        clazz, internalClassName, staticInitializer,
                                        fieldName = GENERATED_FIELD_NAME_PREFIX + ++dynamicIndex,
                                        node.asDynamic().getContent()
                                );
                                asm$pushStaticTextModelFieldGetTextInvocationResult(
                                        method, internalClassName, fieldName
                                );
                                // ... which is referenced in the recipe as a dynamic one (it may differ from
                                // call to call)
                                recipe.append('\1');
                            } else recipe.append(node.asStatic().getText());

                            method.visitInvokeDynamicInsn(
                                    MAKE_CONCAT_WITH_CONSTANTS_METHOD_NAME,
                                    DescriptorCache.stringsToStringDescriptor(dynamicNodeCount),
                                    MAKE_CONCAT_WITH_CONSTANTS_HANDLE, recipe.toString() /* bootstrap argument */
                            );
                        } else {
                            val recipe = new StringBuilder(
                                    staticLength + dynamicNodeCount + staticNodeHandledAsDynamicCount
                            );
                            val bootstrapArguments = new Object[1 + staticNodeHandledAsDynamicCount];

                            int dynamicIndex = -1, bootstrapArgumentIndex = 0;
                            // Lists are commonly faster with random access
                            for (val node : nodes) if (node.isDynamic()) {
                                final String fieldName;
                                // push String (got from dynamic TextModel's `getText(T)` invocation) ...
                                asm$addStaticFieldWithInitializer(
                                        clazz, internalClassName, staticInitializer,
                                        fieldName = GENERATED_FIELD_NAME_PREFIX + ++dynamicIndex,
                                        node.asDynamic().getContent()
                                );
                                asm$pushStaticTextModelFieldGetTextInvocationResult(
                                        method, internalClassName, fieldName
                                );
                                // ... which is referenced in the recipe as a dynamic one (it may differ from
                                // call to call)
                                recipe.append('\1');
                            } else {
                                final StaticAsmNode<T> staticNode;
                                if ((staticNode = node.asStatic()).isTreatAsDynamicValueInStringConcatFactory()) {
                                    // add as static value pushed as bootstrap argument because
                                    // StringConcatFactory ...
                                    // ... would otherwise consider `\1` or `\2` as parts of pattern)

                                    // add the String value (which cannot be part of the raw recipe) to the array
                                    // ...
                                    // ... of bootstrap arguments at index (starting from [1] as [0] is for the
                                    // recipe)
                                    bootstrapArguments[++bootstrapArgumentIndex] = staticNode.getText();
                                    // ... and make the recipe aware of this always static node
                                    recipe.append('\2'); // this one is used only for strings containing \1 anf \2
                                } else recipe.append(staticNode.getText());
                            }

                            bootstrapArguments[0] = recipe.toString();
                            method.visitInvokeDynamicInsn(
                                    MAKE_CONCAT_WITH_CONSTANTS_METHOD_NAME,
                                    DescriptorCache.stringsToStringDescriptor(this.dynamicNodeCount),
                                    MAKE_CONCAT_WITH_CONSTANTS_HANDLE, bootstrapArguments
                            );
                        }
                    }
                    /*
                     * Each dynamic node gets pushed because it gets passed as a dynamic parameter
                     * The `TextModel` used *currently* for getting the text is no extra slot as
                     * the reference to it gets replaced in the stack by the result of invocation
                     * but an extra slot of stack should also be allocated fot the target of `getText(T)`
                     * as it gets pushed onto the stack from the local variable.
                     * However, if the amount of dynamic nodes is more than 200 then multiple calls to
                     * StringConcatFactory happen and as those get passed to thus (except fot the first one) the
                     * worst case
                     */
                    method.visitMaxs(this.dynamicNodeCount + 1, 2 /* [this + local variable] */);
                    //</editor-fold>
                    // The amount of dynamic nodes exceeds the maximal amount of those
                    // passed into the `StringConcatFactory`'s `makeConcat` methods
                } else if (configuration.stringConcatFactoryAlgorithm()
                        == StringConcatFactoryAlgorithm.VECTOR) {
                    //<editor-fold desc="Not as fast implementation" defaultstate="collapsed">
                    // linearly append nodes making the result of the last `makeConcat` call
                    // the first argument of the new one
                    // Nodes -> Concatenations:
                    // 200 -> impossible here, (min) 201 -> 2, 399 -> 2, 400 -> 3...

                    // create an iterator to go through nodes between loops
                    val nodes = this.nodes.iterator();

                    // index of the dynamic TextModel field

                    // for the first `makeConcat` use all slots for the dynamic elements
                    // also, don't add static elements after the last dynamic one

                    final List<Object> bootstrapArguments;
                    (bootstrapArguments = new ArrayList<>(1)).add(null); // gets set to `recipe` when needed
                    val recipe = new StringBuilder(dynamicNodeCount);

                    var containsConstants = false;
                    var dynamicIndex = -1;
                    var dynamicSlotsRemaining = STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS;
                    while (true) {
                        final AsmNode<T> node;
                        if ((node = nodes.next()).isDynamic()) {
                            final String fieldName;
                            asm$addStaticFieldWithInitializer(
                                    clazz, internalClassName, staticInitializer,
                                    fieldName = GENERATED_FIELD_NAME_PREFIX + ++dynamicIndex,
                                    node.asDynamic().getContent()
                            );
                            asm$pushStaticTextModelFieldGetTextInvocationResult(
                                    method, internalClassName, fieldName
                            );
                            recipe.append('\1');

                            // when all dynamic slots get occupied, do `makeConcat`
                            if (--dynamicSlotsRemaining == 0) break;
                        } else {
                            final StaticAsmNode<T> staticNode;
                            if ((staticNode = node.asStatic()).isTreatAsDynamicValueInStringConcatFactory()) {
                                bootstrapArguments.add(staticNode.getText());
                                recipe.append('\2');
                            } else {
                                recipe.append(staticNode.getText());
                                containsConstants = true;
                            }
                        }
                    }

                    /*
                     * Make the first concatenation
                     */
                    val maxDynamicArgumentsStringDescriptor
                            = DescriptorCache.stringsToStringDescriptor(STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS);
                    if (containsConstants) {
                        bootstrapArguments.set(0, recipe.toString());
                        method.visitInvokeDynamicInsn(
                                MAKE_CONCAT_WITH_CONSTANTS_METHOD_NAME, maxDynamicArgumentsStringDescriptor,
                                MAKE_CONCAT_WITH_CONSTANTS_HANDLE, bootstrapArguments.toArray()
                        );
                    } else method.visitInvokeDynamicInsn(
                            MAKE_CONCAT_METHOD_NAME,
                            maxDynamicArgumentsStringDescriptor,
                            MAKE_CONCAT_HANDLE /* no bootstrap arguments */
                    );
                    // < now the result of concatenation is on stack >
                    /*
                     * Make all other concatenations
                     */

                    while (nodes.hasNext()) {
                        // the beginning of the new concatenation group
                        if (dynamicSlotsRemaining == 0) {
                            // 1 dynamic slot get occupied for the result of the previous concatenation
                            dynamicSlotsRemaining = STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS - 1;

                            // reset the recipe without reallocating the `StringBuilder` object
                            // indicating that the String starts from the dynamic value (previous String)
                            recipe.delete(1, recipe.length()).setCharAt(0, '\1');
                            containsConstants = false;
                            bootstrapArguments.clear();
                            bootstrapArguments.add(null);
                        }

                        // add the node
                        {
                            final AsmNode<T> node;
                            if ((node = nodes.next()).isDynamic()) {
                                final String fieldName;
                                asm$addStaticFieldWithInitializer(
                                        clazz, internalClassName, staticInitializer,
                                        fieldName = GENERATED_FIELD_NAME_PREFIX + ++dynamicIndex,
                                        node.asDynamic().getContent()
                                );
                                asm$pushStaticTextModelFieldGetTextInvocationResult(
                                        method, internalClassName, fieldName
                                );
                                recipe.append('\1');

                                // when all dynamic slots get occupied, do `makeConcat`

                                // If needed, make the concatenation not breaking out of the loop
                                // BREAK ME OOOOUT (c) Muse 2017
                                // this happens as soon as the dynamic element boofer is filled
                                // so that the bigger String gets created closer to the end
                                if (--dynamicSlotsRemaining == 0) if (containsConstants) {
                                    bootstrapArguments.set(0, recipe.toString());
                                    method.visitInvokeDynamicInsn(
                                            MAKE_CONCAT_WITH_CONSTANTS_METHOD_NAME,
                                            maxDynamicArgumentsStringDescriptor,
                                            MAKE_CONCAT_WITH_CONSTANTS_HANDLE, bootstrapArguments.toArray()
                                    );
                                } else method.visitInvokeDynamicInsn(
                                        MAKE_CONCAT_METHOD_NAME,
                                        maxDynamicArgumentsStringDescriptor,
                                        MAKE_CONCAT_HANDLE /* no bootstrap arguments */
                                );
                            } else {
                                final StaticAsmNode<T> staticNode;
                                if ((staticNode = node.asStatic()).isTreatAsDynamicValueInStringConcatFactory()) {
                                    bootstrapArguments.add(staticNode.getText());
                                    recipe.append('\2');
                                } else {
                                    recipe.append(staticNode.getText());
                                    containsConstants = true;
                                }
                            }
                        }
                    }

                    // There are no more unhandled nodes but some the last ones)
                    // might have not been used for concatenation

                    // Non-zero indicates that the last stack content was not used for concatenation
                    if (dynamicSlotsRemaining != 0) if (containsConstants) {
                        bootstrapArguments.set(0, recipe.toString());
                        method.visitInvokeDynamicInsn(
                                MAKE_CONCAT_WITH_CONSTANTS_METHOD_NAME, DescriptorCache.stringsToStringDescriptor(
                                        STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS - dynamicSlotsRemaining
                                ), MAKE_CONCAT_WITH_CONSTANTS_HANDLE, bootstrapArguments.toArray()
                        );
                    } else method.visitInvokeDynamicInsn(
                            MAKE_CONCAT_METHOD_NAME,
                            maxDynamicArgumentsStringDescriptor,
                            MAKE_CONCAT_HANDLE /* no bootstrap arguments */
                    );

                    // The worst stack size happens for the following situation:
                    // - ([maximal possible amount of dynamic arguments ] - 1) elements are on the stack
                    // - target is on the stack
                    // - currently `getText`ed TextModel is on the stack
                    method.visitMaxs(
                            STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS + 1, 2 /* [this + local variable] */
                    );
                } else throw new IllegalStateException("Unknown StringConcatFactory algorithm");
                //</editor-fold>

                staticInitializer.visitInsn(RETURN);
                staticInitializer.visitMaxs(2, 0);
                staticInitializer.visitEnd();
            }

            // Return String from method
            method.visitInsn(ARETURN);
            //</editor-fold>

            // Note: visitMaxs() happens above
            method.visitEnd();
        }

        /**
         * Adds code to the method so that it invokes {@link TextModel#getText(Object)} taking object for it from the
         * field.
         *
         * @param method method visitor through which the code should be updated
         * @param internalClassName internal name of this class
         * @param fieldName name of the field of type {@link TextModel}
         */
        private static void asm$pushStaticTextModelFieldGetTextInvocationResult(
                final @NotNull MethodVisitor method,
                final @NotNull String internalClassName,
                final @NotNull String fieldName
        ) {
            // Get value of field storing dynamic value
            method.visitFieldInsn(GETSTATIC, internalClassName, fieldName, TEXT_MODEL_DESCRIPTOR);
            // Push target
            method.visitVarInsn(ALOAD, 1);
            // Invoke `TextModel.getText(T)` on field's value
            method.visitMethodInsn(
                    INVOKEINTERFACE, TEXT_MODEL_INTERNAL_NAME, GET_TEXT_METHOD_NAME,
                    STRING_OBJECT_METHOD_DESCRIPTOR, true
            );
        }

        /**
         * Adds code to the method so that it invokes {@link StringBuilder#append(String)}.
         *
         * @param method method visitor through which the code should be updated
         */
        private static void asm$invokeStringBuilderAppendString(final @NotNull MethodVisitor method) {
            // Invoke `StringBuilder.append(String)`
            method.visitMethodInsn(
                    INVOKEVIRTUAL, STRING_BUILDER_INTERNAL_NAME,
                    APPEND_METHOD_NAME, STRING_BUILDER_STRING_METHOD_DESCRIPTOR, false
            );
        }

        /**
         * Adds code to the method so that it invokes {@link StringBuilder#append(String)}.
         *
         * @param method method visitor through which the code should be updated
         */
        private static void asm$invokeStringBuilderAppendChar(final @NotNull MethodVisitor method) {
            // Invoke `StringBuilder.append(char)`
            method.visitMethodInsn(
                    INVOKEVIRTUAL, STRING_BUILDER_INTERNAL_NAME,
                    APPEND_METHOD_NAME, STRING_BUILDER_CHAR_METHOD_DESCRIPTOR, false
            );
        }

        /**
         * Adds a {@code static final} field of type {@link TextModel} initialized via static-initializer block invoking
         * {@link #internal$getDynamicTextModel(String)} to the class.
         *
         * @param clazz class to which the field should be added
         * @param internalClassName internal name of this class
         * @param staticInitializer static initializer block
         * @param fieldName name of the field to store value
         * @param value value of the field (dynamic text model)
         */
        private static void asm$addStaticFieldWithInitializer(final @NotNull ClassVisitor clazz,
                                                              final @NotNull String internalClassName,
                                                              final @NotNull MethodVisitor staticInitializer,
                                                              final @NotNull String fieldName,
                                                              final @NotNull TextModel<?> value) {
            // add field
            clazz.visitField(
                    OPCODES_ACC_PUBLIC_STATIC_FINAL /* less access checks & possible JIT folding */,
                    fieldName, TEXT_MODEL_DESCRIPTOR /* field type is TextModel<T> */,
                    TEXT_MODEL_SIGNATURE, null /* no default value [*] */
            ).visitEnd();

            // push unique key
            staticInitializer.visitLdcInsn(DYNAMIC_MODELS.storeValue(value));
            // invoke `TextModel internal$getDynamicTextModel(String)`
            staticInitializer.visitMethodInsn(
                    INVOKESTATIC, TEXT_MODEL_BUILDER_INTERNAL_NAME,
                    INTERNAL_GET_DYNAMIC_TEXT_MODEL_METHOD_NAME, TEXT_MODEL_STRING_METHOD_DESCRIPTOR, false
            );

            // set the field to the computed value
            staticInitializer.visitFieldInsn(PUTSTATIC, internalClassName, fieldName, TEXT_MODEL_DESCRIPTOR);
        }

        /**
         * Internal cache of specific dynamic descriptors.
         */
        private static final class DescriptorCache {

            /**
             * Cache of descriptors of methods accepting {@link String string}
             * returning a {@link String string}.
             */
            private static final @Nullable SoftReference<String> @NotNull [] STRINGS_TO_STRING_METHOD_DESCRIPTOR_CACHE
                    = uncheckedSoftReferenceArrayCast(
                    new SoftReference<?>[STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS + 1]
            );

            /**
             * Casts the given array of wild-carded {@link SoftReference soft references}
             * into the array of {@link SoftReference soft references} with generic type being {@link String}.
             *
             * @param type raw-typed array of soft references
             * @return the provided array of soft references with its generic type cast to {@link String}
             *
             * @apiNote this is effectively no-op
             */
            // note: no nullability annotations are present on parameter and return type as cast of `null` is also safe
            @Contract("_ -> param1")
            @SuppressWarnings("unchecked")
            private static SoftReference<String>[] uncheckedSoftReferenceArrayCast(final SoftReference<?>[] type) {
                return (SoftReference<String>[]) type;
            }

            /**
             * Creates or gets a cached descriptor for a method
             * accepting the given amount of {@link String strings} which returns a {@link String string}.
             *
             * @param stringArgumentsCount amount of {@link String string arguments} accepted by the method
             * @return descriptor of the name with the specified signature
             */
            private static @NotNull String stringsToStringDescriptor(final int stringArgumentsCount) {
                assert 0 <= stringArgumentsCount && stringArgumentsCount <= STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS
                        : "stringArgumentsCount should be in range ["
                        + 0 + "; " + STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS + "] but got " + stringArgumentsCount;

                // note: there is no need for providing concurrency safety here because the function is idempotent
                // i.e. if two concurrent calls happen to get the value for the same `stringArgumentsCount`
                // then the equivalent values will just be written twice to the corresponding cache cell
                // which is not a problem at all as there is no need for string identity

                String descriptor;
                { // try getting from cache
                    final SoftReference<String> descriptorReference;
                    if ((descriptorReference = STRINGS_TO_STRING_METHOD_DESCRIPTOR_CACHE[stringArgumentsCount]) != null
                            && (descriptor = descriptorReference.get()) != null /* this acquires strong reference */
                    ) return descriptor; // return the already cached value
                }
                { // generate new cache entry
                    val result = new StringBuilder(
                            STRING_DESCRIPTOR_LENGTH * (stringArgumentsCount + 1) /* all arguments + return */
                                    + 2 /* parentheses */
                    ).append('(');
                    for (var i = 0; i < stringArgumentsCount; i++) result.append(STRING_DESCRIPTOR);

                    descriptor = result.append(')').append(STRING_DESCRIPTOR).toString();
                }
                // cache the computed value
                STRINGS_TO_STRING_METHOD_DESCRIPTOR_CACHE[stringArgumentsCount] = new SoftReference<>(descriptor);

                return descriptor;
            }
        }
    }

    //<editor-fold desc="Configuration implementation" defaultstate="collapsed">
    /**
     * Configuration of this {@link AsmTextModelFactory text model factory}/
     */
    @Value
    @Builder
    @Accessors(fluent = true)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    private static class SimpleConfiguration implements Configuration {

        /**
         * Default configuration instance
         *
         * @implNote this may be replaced with {@link Lazy lazy wrapper} if configurations become stateful or involve
         * multiple inner objects
         */
        private static final @NotNull Configuration DEFAULT = builder().build();

        /**
         * Gets a default configuration.
         *
         * @return default configuration
         */
        @Contract(pure = true)
        private static @NotNull Configuration getDefault() {
            return DEFAULT;
        }

        /**
         * Marker indicating whether {@code StringConcatFactory}-based string concatenation is enabled
         */
        @Builder.Default boolean enableStringConcatFactory = true;

        /**
         * Algorithm which should be used used for producing string concatenation logic via {@code
         * java.lang.invoke.StringConcatFactory}
         */
        @Builder.Default AsmTextModelFactory.StringConcatFactoryAlgorithm stringConcatFactoryAlgorithm
                = AsmTextModelFactory.StringConcatFactoryAlgorithm.VECTOR;

        /**
         * Simple implementation of {@link ConfigurationBuilder}.
         */
        @SuppressWarnings("unused") // Lombok-generated builder class
        private static final class SimpleConfigurationBuilder implements ConfigurationBuilder {}
    }
    //</editor-fold>

    /**
     * {@link AbstractGeneratingTextModelFactoryBuilder.Node Node}
     * specific to {@link AsmTextModelFactory ASM-based text model factory}.
     *
     * @param <T> type of object according to which the created text models are formatted
     */
    private interface AsmNode<T>
            extends AbstractGeneratingTextModelFactoryBuilder.Node<T, StaticAsmNode<T>, DynamicAsmNode<T>> {}

    /**
     * {@link AbstractGeneratingTextModelFactoryBuilder.StaticNode Static node}
     * specific to {@link AsmTextModelFactory ASM-based text model factory}.
     *
     * @param <T> type of object according to which the created text models are formatted
     */
    private interface StaticAsmNode<T> extends AbstractGeneratingTextModelFactoryBuilder.StaticNode<T>, AsmNode<T> {

        /**
         * Checks if this node's text cannot be passed as a raw part
         * of {@code java.lang.invoke.StringConcatFactory}'s concatenation recipe,
         * e.g. due to it containing special characters ({@code '\1'} and {@code '\2'})
         *
         * @return {@code true} if this node's text should be treated as dynamic when used by string concat factory
         */
        boolean isTreatAsDynamicValueInStringConcatFactory();
    }

    /**
     * {@link AbstractGeneratingTextModelFactoryBuilder.DynamicNode Dynamic node}
     * specific to {@link AsmTextModelFactory ASM-based text model factory}.
     *
     * @param <T> type of object according to which the created text models are formatted
     */
    private interface DynamicAsmNode<T> extends AbstractGeneratingTextModelFactoryBuilder.DynamicNode<T>, AsmNode<T> {}

    //<editor-fold desc="Node implementations" defaultstate="collapsed">
    /**
     * Simple implementation of {@link StaticAsmNode}.
     *
     * @param <T> type of object according to which the created text models are formatted
     */
    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static class SimpleStaticAsmNode<T> implements StaticAsmNode<T> {

        /**
         * Text of this node
         */
        @SuppressWarnings("StringBufferField") @NotNull StringBuilder text;

        /**
         * Marker indicating whether this node's text cannot be passed as a raw part of {@code
         * java.lang.invoke.StringConcatFactory}'s concatenation recipe due to it containing special characters
         * ({@code '\1'} and {@code '\2'})
         */
        @SuppressWarnings("BooleanVariableAlwaysNegated") // simpler assignment
        @NonFinal boolean treatAsDynamicValueInStringConcatFactory;

        /**
         * Checks if the given string cannot be used as static by {@code java.lang.invoke.StringConcatFactory}.
         * This happens when the string contains symbols which have special meaning in the latter's templates
         * i.e. {@code \1} and {@code \2}.
         *
         * @param string string to get check
         * @return {@code true} if the given string cannot be treated as static when used by string concat factory
         */
        private static boolean cannotBeStaticInStringConcatFactory(final @NotNull String string) {
            return string.indexOf('\1') != -1 || string.indexOf('\2') != -1;
        }

        @Override
        public boolean isDynamic() {
            return false;
        }

        @Override
        public @NotNull StaticAsmNode<T> asStatic() {
            return this;
        }

        @Override
        public @NotNull DynamicAsmNode<T> asDynamic() {
            throw new UnsupportedOperationException("This is not a dynamic node");
        }

        @Override
        public @NotNull String getText() {
            return text.toString();
        }

        @Override
        public int getTextLength() {
            return text.length();
        }

        @Override
        public void appendText(final @NotNull String text) {
            this.text.append(text);

            // operator `|=` cannot be used here as it is not lazy
            if (!treatAsDynamicValueInStringConcatFactory) treatAsDynamicValueInStringConcatFactory
                    = cannotBeStaticInStringConcatFactory(text);
        }

        /**
         * Creates a new {@link StaticAsmNode} from the gicen text.
         *
         * @param text initial text of the created node
         * @param <T> type of object according to which the created text models are formatted
         * @return created node
         */
        public static <T> @NotNull StaticAsmNode<T> from(final @NotNull String text) {
            return new SimpleStaticAsmNode<>(new StringBuilder(text), cannotBeStaticInStringConcatFactory(text));
        }
    }

    /**
     * Simple implementation of {@link DynamicAsmNode}.
     *
     * @param <T> type of object according to which the created text models are formatted
     */
    @Value
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class SimpleDynamicAsmNode<T> implements DynamicAsmNode<T> {

        /**
         * Dynamic content of this node
         */
        @NotNull TextModel<T> content;

        @Override
        public boolean isDynamic() {
            return true;
        }

        @Override
        public @NotNull DynamicAsmNode<T> asDynamic() {
            return this;
        }

        @Override
        public @NotNull StaticAsmNode<T> asStatic() {
            throw new UnsupportedOperationException("This is not a static node");
        }

        /**
         * Creates a new {@link DynamicAsmNode} from the given {@link TextModel text model}.
         *
         * @param content content of the created node
         * @param <T> type of object according to which the created text models are formatted
         * @return created node
         */
        public static <T> @NotNull DynamicAsmNode<T> from(final @NotNull TextModel<T> content) {
            return new SimpleDynamicAsmNode<>(content);
        }
    }
    //</editor-fold>
}
