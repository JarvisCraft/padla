package ru.progrm_jarvis.ultimatemessenger.format.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.java.Log;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;
import ru.progrm_jarvis.javacommons.annotation.Internal;
import ru.progrm_jarvis.javacommons.bytecode.BytecodeLibrary;
import ru.progrm_jarvis.javacommons.bytecode.annotation.UsesBytecodeModification;
import ru.progrm_jarvis.javacommons.bytecode.asm.AsmUtil;
import ru.progrm_jarvis.javacommons.classload.GcClassDefiners;
import ru.progrm_jarvis.javacommons.lazy.Lazy;
import ru.progrm_jarvis.javacommons.util.ClassNamingStrategy;
import ru.progrm_jarvis.javacommons.util.valuestorage.SimpleValueStorage;
import ru.progrm_jarvis.javacommons.util.valuestorage.ValueStorage;

import javax.annotation.Nonnegative;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
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
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@UsesBytecodeModification(BytecodeLibrary.ASM)
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class AsmTextModelFactory<T, C extends AsmTextModelFactory.Configuration> implements TextModelFactory<T> {
    /**
     * Lazy singleton of this text model factory
     */
    private static final Lazy<AsmTextModelFactory> INSTANCE = Lazy.createThreadSafe(AsmTextModelFactory::create);

    /**
     * Internal storage of {@link TextModel dynamic text models} passed to {@code static final} fields.
     */
    protected static final ValueStorage<String, TextModel<?>> DYNAMIC_MODELS = new SimpleValueStorage<>();

    /**
     * Flag indicating the availability of {@code java.lang.invoke.StringConcatFactory}
     */
    protected static final boolean STRING_CONCAT_FACTORY_AVAILABLE;

    /**
     * Class of {@code java.lang.invoke.StringConcatFactory}
     */
    @Nullable protected static final Class<?> STRING_CONCAT_FACTORY_CLASS;

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

        log.log(Level.FINE, // debug StrinConcatFactory availability
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
     * Creates a new {@link AsmTextModelFactory ASM-based text model factory} with the given configuration.
     *
     * @param configuration configuration to be used by the created text model factory
     * @param <T> type of object according to which the created text models are formatted
     * @return created {@link AsmTextModelFactory ASM-based text model factory} with the given configuration
     */
    public static <T> AsmTextModelFactory<T, ?> create(@NonNull final Configuration configuration) {
        return new AsmTextModelFactory<>(configuration);
    }

    /**
     * Creates a new {@link AsmTextModelFactory ASM-based text model factory} with the default configuration.
     *
     * @param <T> type of object according to which the created text models are formatted
     * @return created {@link AsmTextModelFactory ASM-based text model factory} with the default configuration
     */
    public static <T> AsmTextModelFactory<T, ?> create() {
        return create(SimpleConfiguration.getDefault());
    }

    /**
     * Returns this {@link TextModelFactory text model factory} singleton.
     *
     * @param <T> generic type of got {@link TextModelFactory text model factory}
     * @return shared instance of this {@link TextModelFactory text model factory}
     */
    @SuppressWarnings("unchecked")
    public static <T> AsmTextModelFactory<T, ?> get() {
        return INSTANCE.get();
    }

    @Override
    @NotNull public TextModelFactory.TextModelBuilder<T> newBuilder() {
        return new TextModelBuilder<>(configuration);
    }

    /**
     * Implementation of
     * {@link TextModelFactory.TextModelBuilder text model builder}
     * which uses runtime class generation
     * and is capable of joining nearby static text blocks and optimizing {@link #buildAndRelease()}.
     *
     * @param <T> type of object according to which the created text models are formatted
     */
    @ToString
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = true) // simply, why not? :) (this will also allow caching of instances)
    @FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
    protected static class TextModelBuilder<T> extends AbstractGeneratingTextModelFactoryBuilder
            <T, TextModelBuilder.Node<T>, TextModelBuilder.Node.StaticNode<T>, TextModelBuilder.Node.DynamicNode<T>> {

        /**
         * Lookup of this class.
         */
        protected static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

        public static final String STRINGS_TO_STRING_METHOD_DESCRIPTOR_CACHE_CONCURRENCY_SYSTEM_PROPERTY_NAME
                = TextModelBuilder.class.getCanonicalName() + ".strings-to-string-method-descriptor-cache-concurrency";

        /**
         * Cache of descriptors of methods accepting {@link String string arguments} returning {@link String a string}.
         */
        protected static Cache<Integer, String> STRINGS_TO_STRING_METHOD_DESCRIPTOR_CACHE = CacheBuilder
                .newBuilder()
                .softValues()
                .concurrencyLevel(Math.max(1, Integer.getInteger(
                        STRINGS_TO_STRING_METHOD_DESCRIPTOR_CACHE_CONCURRENCY_SYSTEM_PROPERTY_NAME, 4
                )))
                .build();

        /**
         * Class naming strategy used to allocate names for generated classes
         */
        @NonNull private static final ClassNamingStrategy CLASS_NAMING_STRATEGY = ClassNamingStrategy.createPaginated(
                TextModelBuilder.class.getName() + "$$Generated$$TextModel$$"
        );

        //<editor-fold desc="Bytecode generation constants" defaultstate="collapsed">

        ///////////////////////////////////////////////////////////////////////////
        // Types
        ///////////////////////////////////////////////////////////////////////////
        /* ******************************************** ASM Type objects ******************************************** */
        /**
         * ASM type of {@link TextModelBuilder}
         */
        @NonNull protected static final Type TEXT_MODEL_BUILDER_TYPE = getType(TextModelBuilder.class),
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
        @NonNull protected static final String GENERATED_FIELD_NAME_PREFIX = "D",
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
         * Name of {@link StringBuilder}{@code .append(}<i>?</i>i{@code )} method
         */
        APPEND_METHOD_NAME = "append",
        /**
         * Name of {@link TextModelBuilder#internal$getDynamicTextModel(String)} method
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
         * Signature of {@code void()} method
         */
        VOID_METHOD_DESCRIPTOR = getMethodDescriptor(VOID_TYPE),
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
        STRING_METHOD_SIGNATURE = getMethodDescriptor(STRING_TYPE),
        /**
         * Signature of {@code StringBuilder(String)} method
         */
        STRING_BUILDER_STRING_METHOD_SIGNATURE = getMethodDescriptor(STRING_BUILDER_TYPE, STRING_TYPE),
        /**
         * Signature of {@code TextModel(String)} method
         */
        TEXT_MODEL_STRING_METHOD_SIGNATURE = getMethodDescriptor(TEXT_MODEL_TYPE, STRING_TYPE),
        /**
         * Signature of {@code StringBuilder(char)} method
         */
        STRING_BUILDER_CHAR_METHOD_SIGNATURE = getMethodDescriptor(STRING_BUILDER_TYPE, CHAR_TYPE),
        /**
         * Generic signature of {@link TextModel#getText(Object)} method
         */
        STRING_GENERIC_T_METHOD_SIGNATURE = '(' + PARENT_T_GENERIC_DESCRIPTOR + ')' + STRING_DESCRIPTOR,
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
         * Maximal amount of dynamic arguments passed
         * to {@code java.lang.invoke.StringConcatFactory} concatenation methods
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
        TEXT_MODEL_GENERIC_DESCRIPTOR_LENGTH = TEXT_MODEL_SIGNATURE.length();

        /*  *******************************************************************************************************  */
        /* ************************** java.lang.invoke.StringConcatFactory specific stuff ************************** */
        /*  *******************************************************************************************************  */

        /**
         * ASM type of {@code java.lang.invoke.StringConcatFactory}
         */
        @Nullable private static final Type STRING_CONCAT_FACTORY_TYPE;
        /**
         * Name of {@code java.lang.invoke.StringConcatFactory.concat(Lookup, String, MethodType)}
         */
        @NonNull private static final String MAKE_CONCAT_METHOD_NAME = "makeConcat",
        /**
         * Name of {@code java.lang.invoke.StringConcatFactory
         * .makeConcatWithConstants(Lookup, String, MethodType, String, Object[])}
         */
        MAKE_CONCAT_WITH_CONSTANTS_METHOD_NAME = "makeConcatWithConstants";
        /**
         * Handle of {@code java.lang.invoke.StringConcatFactory.concat(Lookup, String, MethodType)}
         */
        @Nullable private static final Handle MAKE_CONCAT_HANDLE,
        /**
         * Handle of {@code java.lang.invoke.StringConcatFactory
         * .makeConcatWithConstants(Lookup, String, MethodType, String, Object[])}
         */
        MAKE_CONCAT_WITH_CONSTANTS_HANDLE;

        /* ************************************************ Nullable ************************************************ */
        /**
         * Internal name of {@link TextModel}
         */
        @Nullable private static final String STRING_CONCAT_FACTORY_INTERNAL_NAME,
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
        protected static final String[] TEXT_MODEL_INTERNAL_NAME_ARRAY = new String[]{TEXT_MODEL_INTERNAL_NAME};

        //</editor-fold>

        /**
         * Configuration of the parent {@link AsmTextModelFactory text model factory}
         */
        @NonNull Configuration configuration;

        /**
         * Amount of {@link Node.StaticNode static nodes} whose text should be treated
         * by {@code java.lang.invoke.StringConcatFactory} as the one passed to bootstrap arguments
         *
         * @see Node.StaticNode#isTreatAsDynamicValueInStringConcatFactory()
         */
        @NonFinal int staticNodeHandledAsDynamicCount,
        /**
         * Length of texts of those {@link Node.StaticNode static nodes} whose text
         * should be treated by {@code java.lang.invoke.StringConcatFactory} as the one passed to bootstrap arguments
         *
         * @see Node.StaticNode#isTreatAsDynamicValueInStringConcatFactory()
         */
        staticSpecialNodeLength;

        /**
         * JIT-friendly (folded static final constant allows the JIT to use the fast approach here) checks if
         * {@code java.lang.invoke.StringConcatFactory} should be used by this builder
         * for implementing {@link String string} concatenation.
         *
         * @return {@code true} is {@link String string} concatenation via {@code java.lang.invoke.StringConcatFactory}
         * is available and enabled and {@code false} otherwise
         */
        protected boolean isStringConcatFactoryEnabled() {
            return STRING_CONCAT_FACTORY_AVAILABLE && configuration.enableStringConcatFactory();
        }

        @Override
        protected void endModification(@NotNull final Node.StaticNode<T> staticNode) {
            super.endModification(staticNode);

            if (isStringConcatFactoryEnabled()) {
                if (staticNode.isTreatAsDynamicValueInStringConcatFactory()) {
                    staticNodeHandledAsDynamicCount++;
                    staticSpecialNodeLength += staticNode.getTextLength();
                }
            }
        }

        @Override
        @NotNull protected Node<T> newStaticNode(@NotNull final String text) {
            return new Node.StaticNode<>(text);
        }

        @Override
        @NotNull protected Node<T> newDynamicNode(@NotNull final TextModel<T> content) {
            return new Node.DynamicNode<>(content);
        }

        /**
         * Retrieves (gets and removes) {@link TextModel dynamic text model}
         * stored in {@link #DYNAMIC_MODELS} by the given key.
         *
         * @param uniqueKey unique key by which the value should be retrieved
         * @return dynamic text model stored by the given unique key
         * @deprecated this method is internal
         */
        @Deprecated
        @Internal("This is expected to be invoked only by generated TextModels to initialize their fields")
        public static TextModel<?> internal$getDynamicTextModel(@NotNull final String uniqueKey) {
            return DYNAMIC_MODELS.retrieveValue(uniqueKey);
        }

        /**
         * Creates or gets a cache descriptor for a method accepting the given amount of {@link String strings}
         * which returns a {@link String string}.
         *
         * @param stringArgumentsCount amount of {@link String string arguments} accepted by the method
         * @return descriptor of the name with the specified signature
         */
        @SneakyThrows(ExecutionException.class)
        protected static String stringsToStringDescriptor(@Nonnegative final int stringArgumentsCount) {
            return STRINGS_TO_STRING_METHOD_DESCRIPTOR_CACHE.get(stringArgumentsCount, () -> {
                val result = new StringBuilder(
                        STRING_DESCRIPTOR_LENGTH * (stringArgumentsCount + 1) /* all arguments + return */ + 2 /* parentheses */
                ).append('(');
                for (var i = 0; i < stringArgumentsCount; i++) result.append(STRING_DESCRIPTOR);

                return result.append(')').append(STRING_DESCRIPTOR).toString();
            });
        }

        @Override
        @NotNull protected TextModel<T> performTextModelBuild(final boolean release) {
            val clazz = new ClassWriter(0); // MAXs are already computed 😎

            //<editor-fold desc="ASM class generation" defaultstate="collapsed">
            val className = CLASS_NAMING_STRATEGY.get();

            // ASM does not provide any comfortable method fot this :(
            // PS yet ASM is <3
            val internalClassName = className.replace('.', '/');
            clazz.visit(
                    V1_8 /* generate bytecode for JVM1.8 */, OPCODES_ACC_PUBLIC_FINAL_SUPER,
                    internalClassName, GENERIC_CLASS_SIGNATURE, OBJECT_INTERNAL_NAME /* inherit Object */,
                    TEXT_MODEL_INTERNAL_NAME_ARRAY /* implement TextModel interface */
            );
            // add an empty constructor
            AsmUtil.addEmptyConstructor(clazz);

            if (isStringConcatFactoryEnabled()) asm$implementGetTextMethodViaStringConcatFactory(
                    clazz, internalClassName
            );
            else asm$implementGetTextMethodViaStringBuilder(clazz, internalClassName);

            clazz.visitEnd();
            //</editor-fold>

            try {
                val constructor = GcClassDefiners.getDefault()
                        .orElseThrow(() -> new IllegalStateException("GC-ClassDefiner is unavailable"))
                        .defineClass(LOOKUP, className, clazz.toByteArray()).getDeclaredConstructor();
                constructor.setAccessible(true);
                //noinspection unchecked
                return (TextModel<T>) constructor.newInstance();
            } catch (final NoSuchMethodException | InstantiationException
                    | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Could not compile and instantiate TextModel from the given nodes");
            }
        }

        /**
         * Implements the {@link TextModel#getText(Object)} method in the generated class
         * via {@code java.lang.invoke.StringConcatFactory}.
         *
         * @param clazz class-writer used for generating the implementation
         * @param internalClassName internal name of the generated class
         */
        protected void asm$implementGetTextMethodViaStringBuilder(@NotNull final ClassWriter clazz,
                                                                  @NotNull final String internalClassName) {
            // Implement `TextModel#getText(T)` method and add fields
            val method = clazz.visitMethod(
                    ACC_PUBLIC, GET_TEXT_METHOD_NAME, STRING_OBJECT_METHOD_DESCRIPTOR,
                    STRING_GENERIC_T_METHOD_SIGNATURE, null
            );

            method.visitCode();

            //<editor-fold desc="Method code generation" defaultstate="collapsed">
            {
                val staticInitializer = AsmUtil.visitStaticInitializer(clazz);
                staticInitializer.visitCode();

                val staticLength = this.staticLength;
                if (staticLength == 0) { // there are no static nodes (and at least 2 dynamic)
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

                    val iterator = nodes.iterator();
                    asm$addStaticFieldWithInitializer(
                            clazz, internalClassName, staticInitializer,
                            fieldName, iterator.next().asDynamic().getContent()
                    );
                    // dynamic nodes count is at least 2
                    var dynamicIndex = 0;
                    while (iterator.hasNext()) {
                        fieldName = (GENERATED_FIELD_NAME_PREFIX + (++dynamicIndex));

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
                    method.visitMaxs(3, 2 /* [this + local variable] */);
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
                    for (val node : nodes) {
                        // Load static text value from dynamic constant
                        if (node.isDynamic()) {
                            val fieldName = GENERATED_FIELD_NAME_PREFIX + (++dynamicIndex);
                            asm$addStaticFieldWithInitializer(
                                    clazz, internalClassName, staticInitializer,
                                    fieldName, node.asDynamic().getContent()
                            );
                            asm$pushStaticTextModelFieldGetTextInvocationResult(
                                    method, internalClassName, fieldName
                            );
                            asm$invokeStringBuilderAppendString(method);
                        } else {
                            val staticText = node.asStatic().getText();
                            if (staticText.length() == 1) {
                                pushCharUnsafely(method, staticText.charAt(0));
                                asm$invokeStringBuilderAppendChar(method);
                            } else {
                                method.visitLdcInsn(node.asStatic().getText()); // get constant String value
                                asm$invokeStringBuilderAppendString(method);
                            }
                        }
                    }
                    /*
                     * As there are dynamic nodes the maximal stack size is when it consists of:
                     * - the StringBuilder instance
                     * - the `TextModel` whose `getText` is being invoked
                     * - the parameter passed to `getText`
                     */
                    method.visitMaxs(3, 2 /* [this + local variable] */);
                }
                staticInitializer.visitInsn(RETURN);
                staticInitializer.visitMaxs(2, 0);
                staticInitializer.visitEnd();
            }

            // invoke `StringBuilder#toString()`
            method.visitMethodInsn(
                    INVOKEVIRTUAL, STRING_BUILDER_INTERNAL_NAME,
                    TO_STRING_METHOD_NAME, STRING_METHOD_SIGNATURE, false
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
        protected void asm$implementGetTextMethodViaStringConcatFactory(@NotNull final ClassWriter clazz,
                                                                        @NotNull final String internalClassName
        ) {
            // The Lookup will be needed by the runtime for `invokedynamic` usage
            AsmUtil.addLookup(clazz);

            // Implement `TextModel#getText(T)` method and add fields
            val method = clazz.visitMethod(
                    ACC_PUBLIC, GET_TEXT_METHOD_NAME, STRING_OBJECT_METHOD_DESCRIPTOR,
                    STRING_GENERIC_T_METHOD_SIGNATURE, null
            );

            method.visitCode();

            //<editor-fold desc="Method code generation" defaultstate="collapsed">
            {
                val staticInitializer = AsmUtil.visitStaticInitializer(clazz);
                staticInitializer.visitCode();

                val dynamicNodes = dynamicNodeCount;
                if (dynamicNodeCount <= STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS) {
                    // The amount of dynamic nodes does not exceed the maximal amount of those
                    // passed into the `StringConcatFactory`'s `makeConcat` methods
                    //<editor-fold desc="Fast implementation" defaultstate="collapsed">
                    val staticLength = this.staticLength;
                    if (staticLength == 0) { // there only are dynamic nodes
                        // dynamic nodes count is at least 2
                        var dynamicIndex = -1;
                        // add fields containing dynamic nodes and their invocation
                        for (val node : nodes) {
                            val fieldName = (GENERATED_FIELD_NAME_PREFIX + (++dynamicIndex));
                            asm$addStaticFieldWithInitializer(
                                    clazz, internalClassName, staticInitializer,
                                    fieldName, node.asDynamic().getContent()
                            );
                            asm$pushStaticTextModelFieldGetTextInvocationResult(method, internalClassName, fieldName);
                        }

                        method.visitInvokeDynamicInsn(
                                MAKE_CONCAT_METHOD_NAME, stringsToStringDescriptor(dynamicNodes),
                                MAKE_CONCAT_HANDLE /* no bootstrap arguments */
                        );
                    } else {// there are static nodes
                        if (staticNodeHandledAsDynamicCount == 0) {
                            val recipe = new StringBuilder(staticLength + dynamicNodes);

                            var dynamicIndex = -1;
                            // Lists are commonly faster with random access
                            for (val node : nodes) {
                                if (node.isDynamic()) {
                                    val fieldName = GENERATED_FIELD_NAME_PREFIX + (++dynamicIndex);
                                    // push String (got from dynamic TextModel's `getText(T)` invocation) ...
                                    asm$addStaticFieldWithInitializer(
                                            clazz, internalClassName, staticInitializer,
                                            fieldName, node.asDynamic().getContent()
                                    );
                                    asm$pushStaticTextModelFieldGetTextInvocationResult(
                                            method, internalClassName, fieldName
                                    );
                                    // ... which is referenced in the recipe as a dynamic one (it may differ from call to call)
                                    recipe.append('\1');
                                } else recipe.append(node.asStatic().getText());
                            }

                            method.visitInvokeDynamicInsn(
                                    MAKE_CONCAT_WITH_CONSTANTS_METHOD_NAME, stringsToStringDescriptor(dynamicNodes),
                                    MAKE_CONCAT_WITH_CONSTANTS_HANDLE, recipe.toString() /* bootstrap argument */
                            );
                        } else {
                            val recipe = new StringBuilder(
                                    staticLength + dynamicNodes + staticNodeHandledAsDynamicCount
                            );

                            Object[] bootstrapArguments = new Object[1 + staticNodeHandledAsDynamicCount];

                            int dynamicIndex = -1, bootstrapArgumentIndex = 0;
                            // Lists are commonly faster with random access
                            for (val node : nodes) {
                                if (node.isDynamic()) {
                                    val fieldName = GENERATED_FIELD_NAME_PREFIX + (++dynamicIndex);
                                    // push String (got from dynamic TextModel's `getText(T)` invocation) ...
                                    asm$addStaticFieldWithInitializer(
                                            clazz, internalClassName, staticInitializer,
                                            fieldName, node.asDynamic().getContent()
                                    );
                                    asm$pushStaticTextModelFieldGetTextInvocationResult(
                                            method, internalClassName, fieldName
                                    );
                                    // ... which is referenced in the recipe as a dynamic one (it may differ from call to call)
                                    recipe.append('\1');
                                } else {
                                    val staticNode = node.asStatic();
                                    if (staticNode.isTreatAsDynamicValueInStringConcatFactory()) {
                                        // add as static value pushed as bootstrap argument because StringConcatFactory ...
                                        // ... would otherwise consider `\1` or `\2` as parts of pattern)

                                        // add the String value (which cannot be part of the raw recipe) to the array ...
                                        // ... of bootstrap arguments at index (starting from [1] as [0] is for the recipe)
                                        bootstrapArguments[++bootstrapArgumentIndex] = staticNode.getText();
                                        // ... and make the recipe aware of this always static node
                                        recipe.append('\2'); // this one is used only for strings containing \1 anf \2
                                    } else recipe.append(staticNode.getText());
                                }
                            }

                            bootstrapArguments[0] = recipe.toString();
                            method.visitInvokeDynamicInsn(
                                    MAKE_CONCAT_WITH_CONSTANTS_METHOD_NAME, stringsToStringDescriptor(dynamicNodeCount),
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
                     * StringConcatFactory happen and as those get passed to thus (except fot the first one) the worst case
                     */
                    method.visitMaxs(dynamicNodeCount + 1, 2 /* [this + local variable] */);
                    //</editor-fold>
                } else {
                    // The amount of dynamic nodes exceeds the maximal amount of those
                    // passed into the `StringConcatFactory`'s `makeConcat` methods
                    //<editor-fold desc="Not as fast implementation" defaultstate="collapsed">
                    switch (configuration.stringConcatFactoryAlgorithm()) {
                        case TREE: {
                            if (false) { // FIXME: 12.09.2019
                                // log(STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS, dynamicNodes)
                                val stringConcatFactoryCalls = Math.round(
                                        Math.log(dynamicNodes) / Math.log(STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS)
                                );

                                val nodes = this.nodes.iterator();

                                // The worst stack size happens for the following situation:
                                // - ([maximal possible amount of dynamic arguments ] - 1) elements are on the stack
                                // - target is on the stack
                                // - currently `getText`ed TextModel is on the stack
                                method.visitMaxs(
                                        STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS + 1, 2 /* [this + local variable] */
                                );

                                break;
                            }
                            log.warning("\n\nTREE is not implemented\n\n");
                        }
                        case VECTOR: {
                            // linearly append nodes making the result of the last `makeConcat` call the
                            // first argument of the new one
                            // 200 -> impossible here, (min) 201 -> 2, 399 -> 2, 400 -> 3...
                            val concatenations
                                    = (dynamicNodes - 1) / STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS + 1;

                            // create an iterator to go through nodes between loops
                            val nodes = this.nodes.iterator();

                            // index of the dynamic TextModel field
                            var dynamicIndex = -1;

                            // for the first `makeConcat` use all slots for the dynamic elements
                            // also, don't add static elements after the last dynamic one
                            var dynamicSlotsRemaining = STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS;

                            val bootstrapArguments = new ArrayList<Object>(1);
                            bootstrapArguments.add(null); // gets set to `recipe` when needed
                            val recipe = new StringBuilder(dynamicNodes);

                            var containsConstants = false;
                            while (true) {
                                val node = nodes.next();
                                if (node.isDynamic()) {
                                    val fieldName = GENERATED_FIELD_NAME_PREFIX + (++dynamicIndex);
                                    asm$addStaticFieldWithInitializer(
                                            clazz, internalClassName, staticInitializer,
                                            fieldName, node.asDynamic().getContent()
                                    );
                                    asm$pushStaticTextModelFieldGetTextInvocationResult(
                                            method, internalClassName, fieldName
                                    );
                                    recipe.append('\1');

                                    // when all dynamic slots get occupied, do `makeConcat`
                                    if (--dynamicSlotsRemaining == 0) break;
                                } else {
                                    val staticNode = node.asStatic();
                                    if (staticNode.isTreatAsDynamicValueInStringConcatFactory()) {
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
                                    = stringsToStringDescriptor(STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS);
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
                                    val node = nodes.next();
                                    if (node.isDynamic()) {
                                        val fieldName = GENERATED_FIELD_NAME_PREFIX + (++dynamicIndex);
                                        asm$addStaticFieldWithInitializer(
                                                clazz, internalClassName, staticInitializer,
                                                fieldName, node.asDynamic().getContent()
                                        );
                                        asm$pushStaticTextModelFieldGetTextInvocationResult(
                                                method, internalClassName, fieldName
                                        );
                                        recipe.append('\1');

                                        // when all dynamic slots get occupied, do `makeConcat`
                                        if (--dynamicSlotsRemaining == 0) {
                                            // make the concatenation not breaking out of the loop
                                            // BREAK ME OOOOUT (c) Muse 2017
                                            // this happens as soon as the dynamic element boofer is filled
                                            // so that the bigger String gets created closer to the end
                                            if (containsConstants) {
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
                                        }
                                    } else {
                                        val staticNode = node.asStatic();
                                        if (staticNode.isTreatAsDynamicValueInStringConcatFactory()) {
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

                            if (dynamicSlotsRemaining != 0) { // the last stack content was not used for concatenation
                                // note: amount of dynamic elements may even be

                                if (containsConstants) {
                                    bootstrapArguments.set(0, recipe.toString());
                                    method.visitInvokeDynamicInsn(
                                            MAKE_CONCAT_WITH_CONSTANTS_METHOD_NAME, stringsToStringDescriptor(
                                                    STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS - dynamicSlotsRemaining
                                            ), MAKE_CONCAT_WITH_CONSTANTS_HANDLE, bootstrapArguments.toArray()
                                    );
                                } else method.visitInvokeDynamicInsn(
                                        MAKE_CONCAT_METHOD_NAME,
                                        maxDynamicArgumentsStringDescriptor,
                                        MAKE_CONCAT_HANDLE /* no bootstrap arguments */
                                );

                            }

                            // The worst stack size happens for the following situation:
                            // - ([maximal possible amount of dynamic arguments ] - 1) elements are on the stack
                            // - target is on the stack
                            // - currently `getText`ed TextModel is on the stack
                            method.visitMaxs(
                                    STRING_CONCAT_FACTORY_MAX_DYNAMIC_ARGUMENTS + 1, 2 /* [this + local variable] */
                            );

                            break;
                        }
                        default: throw new IllegalStateException("Unknown StringConcatFactory algorithm");
                    }
                    //</editor-fold>
                }

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
         * Adds code to the method so that it invokes {@link TextModel#getText(Object)}
         * taking object for it from the field.
         *
         * @param method method visitor through which the code should be updated
         * @param internalClassName internal name of this class
         * @param fieldName name of the field of type {@link TextModel}
         */
        protected static void asm$pushStaticTextModelFieldGetTextInvocationResult(
                @NotNull final MethodVisitor method,
                @NotNull final String internalClassName,
                @NotNull final String fieldName) {
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
        protected static void asm$invokeStringBuilderAppendString(@NotNull final MethodVisitor method) {
            // Invoke `StringBuilder.append(String)`
            method.visitMethodInsn(
                    INVOKEVIRTUAL, STRING_BUILDER_INTERNAL_NAME,
                    APPEND_METHOD_NAME, STRING_BUILDER_STRING_METHOD_SIGNATURE, false
            );
        }

        /**
         * Adds code to the method so that it invokes {@link StringBuilder#append(String)}.
         *
         * @param method method visitor through which the code should be updated
         */
        protected static void asm$invokeStringBuilderAppendChar(@NotNull final MethodVisitor method) {
            // Invoke `StringBuilder.append(char)`
            method.visitMethodInsn(
                    INVOKEVIRTUAL, STRING_BUILDER_INTERNAL_NAME,
                    APPEND_METHOD_NAME, STRING_BUILDER_CHAR_METHOD_SIGNATURE, false
            );
        }

        /**
         * Adds a {@code static final} field of type {@link TextModel} initialized via static-initializer block
         * invoking {@link #internal$getDynamicTextModel(String)} to the class.
         *
         * @param clazz class to which the field should be added
         * @param internalClassName internal name of this class
         * @param staticInitializer static initializer block
         * @param fieldName name of the field to store value
         * @param value value of the field (dynamic text model)
         */
        protected static void asm$addStaticFieldWithInitializer(@NotNull final ClassVisitor clazz,
                                                                @NotNull final String internalClassName,
                                                                @NotNull final MethodVisitor staticInitializer,
                                                                @NotNull final String fieldName,
                                                                @NotNull final TextModel value) {
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
                    INTERNAL_GET_DYNAMIC_TEXT_MODEL_METHOD_NAME, TEXT_MODEL_STRING_METHOD_SIGNATURE, false
            );

            // set the field to the computed value
            staticInitializer.visitFieldInsn(PUTSTATIC, internalClassName, fieldName, TEXT_MODEL_DESCRIPTOR);
        }

        /**
         * A {@link ru.progrm_jarvis.ultimatemessenger.format.model.AbstractGeneratingTextModelFactoryBuilder.Node node}
         * specific for {@link AsmTextModelFactory ASM-based text model factory}.
         *
         * @param <T> type of object according to which the created text models are formatted
         */
        protected interface Node<T>
                extends AbstractGeneratingTextModelFactoryBuilder.Node<T, Node.StaticNode<T>, Node.DynamicNode<T>> {

            /**
             * A {@link ru.progrm_jarvis.ultimatemessenger.format.model.AbstractGeneratingTextModelFactoryBuilder.StaticNode
             * static node} specific for {@link AsmTextModelFactory ASM-based text model factory}.
             *
             * @param <T> type of object according to which the created text models are formatted
             */
            @Value
            @FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
            class StaticNode<T> implements AbstractGeneratingTextModelFactoryBuilder.StaticNode<T>, Node<T> {

                /**
                 * Text of this node
                 */
                @NotNull StringBuilder text;

                /**
                 * Marker indicating whether this node's text cannot be passed as a raw part of
                 * {@code java.lang.invoke.StringConcatFactory}'s concatenation recipe due to it
                 * containing special characters ({@code '\1'} and {@code '\2'})
                 */
                @NonFinal boolean treatAsDynamicValueInStringConcatFactory;

                public StaticNode(@NonNull final String text) {
                    this.text = new StringBuilder(text);

                    treatAsDynamicValueInStringConcatFactory = text.indexOf('\1') != -1 || text.indexOf('\2') != -1;
                }

                @Override
                public boolean isDynamic() {
                    return false;
                }

                @Override
                public StaticNode<T> asStatic() {
                    return this;
                }

                @Override
                @NotNull public String getText() {
                    return text.toString();
                }

                @Override
                public int getTextLength() {
                    return text.length();
                }

                @Override
                public void appendText(@NotNull final String text) {
                    this.text.append(text);

                    // operator `|=` cannot be used here as it is not lazy (the right side of expression is always used)
                    if (!treatAsDynamicValueInStringConcatFactory) treatAsDynamicValueInStringConcatFactory
                            = text.indexOf('\1') != -1 || text.indexOf('\2') != -1;
                }
            }

            /**
             * A {@link
             * ru.progrm_jarvis.ultimatemessenger.format.model.AbstractGeneratingTextModelFactoryBuilder.DynamicNode
             * dynamic node} specific for {@link AsmTextModelFactory ASM-based text model factory}.
             *
             * @param <T> type of object according to which the created text models are formatted
             */
            @Value
            @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
            class DynamicNode<T> implements AbstractGeneratingTextModelFactoryBuilder.DynamicNode<T>, Node<T> {

                /**
                 * Dynamic content of this node
                 */
                @NotNull final TextModel<T> content;

                @Override
                public boolean isDynamic() {
                    return true;
                }

                @Override
                public DynamicNode<T> asDynamic() {
                    return this;
                }
            }
        }
    }

    /**
     * Configuration of this {@link AsmTextModelFactory text model factory}
     */
    public interface Configuration {

        /**
         * Tests whether the configured {@link TextModelBuilder text model builder}
         * should attempt to use {@code java.lang.invoke.StringConcatFactory} for {@link String string}-concatenation.
         *
         * @return {@code true} if {@code StringConcatFactory} should be used (if available) for string concatenation
         * and {@code false} otherwise
         */
        @Contract(pure = true)
        boolean enableStringConcatFactory();

        /**
         * Gets the algorithm which should be used used for producing string concatenation logic
         * via {@code java.lang.invoke.StringConcatFactory} when it is {@link #enableStringConcatFactory() enabled}.
         *
         * @return algorithm which should be used for producing string concatenation logic
         */
        @Contract(pure = true)
        StringConcatFactoryAlgorithm stringConcatFactoryAlgorithm();

        enum StringConcatFactoryAlgorithm {
            TREE,
            VECTOR
        }
    }

    /**
     * Creates a new builder of the configuration.
     *
     * @return builder of a {@link Configuration}
     */
    public static SimpleConfiguration.SimpleConfigurationBuilder configuration() {
        return SimpleConfiguration.builder();
    }

    /**
     * Configuration of this {@link AsmTextModelFactory text model factory}/
     */
    @Value
    @Builder
    @Accessors(fluent = true)
    @FieldDefaults(level = AccessLevel.PROTECTED)
    @NonFinal protected static class SimpleConfiguration implements Configuration {

        /**
         * Default configuration instance
         *
         * @implNote this may be replaced with {@link Lazy lazy wrapper}
         * if configurations become stateful or involve multiple inner objects
         */
        protected static SimpleConfiguration DEFAULT = builder().build();

        /**
         * Gets a default configuration.
         *
         * @return default configuration
         */
        @Contract(pure = true)
        protected static SimpleConfiguration getDefault() {
            return DEFAULT;
        }

        /**
         * Marker indicating whether {@code StringConcatFactory}-based string concatenation is enabled
         */
        @Builder.Default boolean enableStringConcatFactory = true;

        /**
         * Algorithm which should be used used for producing string concatenation logic
         * via {@code java.lang.invoke.StringConcatFactory}
         */
        @Builder.Default StringConcatFactoryAlgorithm stringConcatFactoryAlgorithm = StringConcatFactoryAlgorithm.TREE;
    }
}
