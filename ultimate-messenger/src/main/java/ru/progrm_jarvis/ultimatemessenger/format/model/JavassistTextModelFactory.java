package ru.progrm_jarvis.ultimatemessenger.format.model;

import javassist.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.annotation.Internal;
import ru.progrm_jarvis.javacommons.bytecode.CommonBytecodeLibrary;
import ru.progrm_jarvis.javacommons.bytecode.annotation.UsesBytecodeModification;
import ru.progrm_jarvis.javacommons.classloading.ClassNamingStrategy;
import ru.progrm_jarvis.javacommons.classloading.GcClassDefiners;
import ru.progrm_jarvis.javacommons.lazy.Lazy;
import ru.progrm_jarvis.javacommons.object.valuestorage.SimpleValueStorage;
import ru.progrm_jarvis.javacommons.object.valuestorage.ValueStorage;
import ru.progrm_jarvis.ultimatemessenger.format.model.AbstractGeneratingTextModelFactoryBuilder.DynamicNode;
import ru.progrm_jarvis.ultimatemessenger.format.model.AbstractGeneratingTextModelFactoryBuilder.Node;
import ru.progrm_jarvis.ultimatemessenger.format.model.AbstractGeneratingTextModelFactoryBuilder.StaticNode;
import ru.progrm_jarvis.ultimatemessenger.format.util.StringMicroOptimizationUtil;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * Implementation of {@link TextModelFactory text model factory} which uses runtime class generation.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@UsesBytecodeModification(CommonBytecodeLibrary.JAVASSIST)
public final class JavassistTextModelFactory<T> implements TextModelFactory<T> {

    /**
     * Lazy singleton of this text model factory
     */
    private static final @NotNull Lazy<@NotNull TextModelFactory<?>> INSTANCE
            = Lazy.createThreadSafe(JavassistTextModelFactory::new);

    /**
     * Creates a Javassist-based {@link TextModelFactory text model factory}.
     *
     * @param <T> generic type of created text model factory
     * @return created text model factory
     *
     * @implNote returned instance may be a singleton
     */
    @SuppressWarnings("unchecked")
    public static <T> @NotNull TextModelFactory<T> create() {
        return (TextModelFactory<T>) INSTANCE.get();
    }

    @Override
    public @NotNull TextModelFactory.TextModelBuilder<T> newBuilder() {
        return new JavassistTextModelBuilder<>();
    }

    /**
     * Implementation of
     * {@link TextModelFactory.TextModelBuilder text model builder}
     * which uses runtime class generation
     * and is capable of joining nearby static text blocks and optimizing {@link #buildAndRelease()}.
     *
     * @param <T> type of object according to which the created text models are formatted
     *
     * @implNote this class is {@code protected} so that it is accessible by generated classes
     */
    @ToString
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = true) // simply, why not? :) (this will also allow caching of instances)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    protected static final class JavassistTextModelBuilder<T>
            extends AbstractGeneratingTextModelFactoryBuilder<
            T, Node<T, StaticNode<T>, DynamicNode<T>>, StaticNode<T>, DynamicNode<T>
            > {

        /**
         * Lookup of this class.
         */
        private static final @NotNull MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

        @Override
        protected @NotNull Node<T, StaticNode<T>, DynamicNode<T>> newStaticNode(final @NotNull String text) {
            return new SimpleStaticNode<>(text);
        }

        @Override
        protected @NotNull Node<T, StaticNode<T>, DynamicNode<T>> newDynamicNode(final @NotNull TextModel<T> content) {
            return new SimpleDynamicNode<>(content);
        }

        /**
         * Full name (including canonical class name) of {@link #internal$getDynamicTextModel(String)} method
         */
        private static final @NotNull String INTERNAL_GET_DYNAMIC_TEXT_MODEL_METHOD_FULL_NAME
                = JavassistTextModelBuilder.class.getCanonicalName() + ".internal$getDynamicTextModel",
        /**
         * Prefix of generated fields after which the index will go
         */
        GENERATED_FIELD_NAME_PREFIX = "D";

        /**
         * Internal storage of {@link TextModel dynamic text models} passed to {@code static final} fields.
         */
        private static final @NotNull ValueStorage<@NotNull String, @NotNull TextModel<?>> DYNAMIC_MODELS
                = SimpleValueStorage.create();

        /**
         * Lazily initialized {@link ClassPool Javassist class pool}
         */
        private static final @NotNull Lazy<@NotNull ClassPool> CLASS_POOL = Lazy.createThreadSafe(ClassPool::getDefault);

        /**
         * Lazily initialized {@link CtClass compile-time class} of {@link TextModel text model}
         */
        private static final @NotNull Lazy<@NotNull CtClass> TEXT_MODEL_CT_CLASS = Lazy.createThreadSafe(() -> {
            try {
                return CLASS_POOL.get().getCtClass(TextModel.class.getName());
            } catch (final NotFoundException e) {
                throw new IllegalStateException("Unable to get CtClass by name " + TextModel.class.getName(), e);
            }
        });

        /**
         * Result of {@link Modifier#PUBLIC}, {@link Modifier#STATIC} and {@link Modifier#FINAL} flags disjunction
         */
        private static final int PUBLIC_FINAL_MODIFIERS = Modifier.PUBLIC | Modifier.FINAL,
                PUBLIC_STATIC_FINAL_MODIFIERS = PUBLIC_FINAL_MODIFIERS | Modifier.STATIC;

        /**
         * Class naming strategy used to allocate names for generated classes
         */
        private static final @NotNull ClassNamingStrategy CLASS_NAMING_STRATEGY = ClassNamingStrategy.createPaginated(
                JavassistTextModelBuilder.class.getName() + "$$Generated$$TextModel$$"
        );

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
        public static @NotNull TextModel<?> internal$getDynamicTextModel(final @NotNull String uniqueKey) {
            return DYNAMIC_MODELS.retrieveValue(uniqueKey);
        }

        @Override
        public @NotNull TextModel<T> performTextModelBuild(final boolean release) {
            val clazz = CLASS_POOL.get().makeClass(CLASS_NAMING_STRATEGY.get());
            val textModelCtClass = TEXT_MODEL_CT_CLASS.get();
            clazz.setModifiers(PUBLIC_FINAL_MODIFIERS);
            clazz.setInterfaces(new CtClass[]{textModelCtClass});

            // Default constructor
            try {
                clazz.addConstructor(CtNewConstructor.defaultConstructor(clazz));
            } catch (final CannotCompileException e) {
                throw new IllegalStateException("Could not add defaul constructor to generated TextModel", e);
            }

            { // Method (#getText(T))
                final StringBuilder src;
                val staticLength = this.staticLength;
                if (staticLength == 0) { // constructor StringBuilder from the first object
                    // only dynamic elements (yet, there are multiple of those)
                    String fieldName = GENERATED_FIELD_NAME_PREFIX + 0;
                    src = new StringBuilder("public String getText(Object t){return new StringBuilder(")
                            .append(fieldName).append(".getText(t))");

                    val iterator = nodes.iterator();

                    javassist$addStaticFieldWithInitializer(clazz, fieldName, iterator.next().asDynamic().getContent());
                    // dynamic elements count is at least 2
                    var index = 0;
                    while (iterator.hasNext()) {
                        fieldName = GENERATED_FIELD_NAME_PREFIX + (++index);
                        javassist$addStaticFieldWithInitializer(
                                clazz, fieldName, iterator.next().asDynamic().getContent()
                        );
                        src.append(".append(").append(fieldName).append(".getText(t))"); // .append(d#.getText(t))
                    }
                } else {
                    src = new StringBuilder(
                            "public String getText(Object t){return new StringBuilder("
                    ).append(staticLength + minDynamicLength).append(')');
                    // there are static elements
                    int dynamicIndex = -1;
                    for (val element : nodes) if (element.isDynamic()) {
                        val fieldName = GENERATED_FIELD_NAME_PREFIX + (++dynamicIndex);
                        javassist$addStaticFieldWithInitializer(clazz, fieldName, element.asDynamic().getContent());
                        src.append(".append(").append(fieldName).append(".getText(t))"); // .append(d#.getText(t))
                    } else {
                        val staticText = element.asStatic().getText();
                        if (staticText.length() == 1) { // handle single char String as a char
                            val character = staticText.charAt(0);
                            // There seems to be a Javassist bug with characters less than \32
                            if (character < 32) src.append(".append((char)").append((int) character).append(')');
                            else src.append(".append('").append(
                                    StringMicroOptimizationUtil.escapeJavaCharacterLiteral(character)
                            ).append('\'').append(')');
                        } else src.append(".append(\"").append(
                                StringMicroOptimizationUtil.escapeJavaStringLiteral(staticText)
                        ).append('"').append(')');
                    }
                }

                try {
                    clazz.addMethod(CtMethod.make(src.append(".toString();}").toString(), clazz));
                } catch (final CannotCompileException e) {
                    throw new IllegalStateException("Could not add method to generated TextModel", e);
                }
            }

            try {
                val constructor = GcClassDefiners.getDefault()
                        .defineClass(LOOKUP, clazz.getName(), clazz.toBytecode()).getDeclaredConstructor();
                constructor.setAccessible(true);
                // noinspection unchecked
                return (TextModel<T>) constructor.newInstance();
            } catch (final IOException | CannotCompileException | NoSuchMethodException
                    | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException(
                        "Could not compile and instantiate TextModel from the given elements", e
                );
            }
        }

        /**
         * Adds a {@code static final} field of type {@link TextModel} initialized via static-initializer block
         * invoking {@link #internal$getDynamicTextModel(String)} to the class.
         *
         * @param clazz class to which the field should be added
         * @param fieldName name of the field to store value
         * @param value value of the field (dynamic text model)
         */
        private static void javassist$addStaticFieldWithInitializer(final @NotNull CtClass clazz,
                                                                    final @NotNull String fieldName,
                                                                    final @NotNull TextModel<?> value) {
            try {
                val field = new CtField(TEXT_MODEL_CT_CLASS.get(), fieldName, clazz);
                field.setModifiers(PUBLIC_STATIC_FINAL_MODIFIERS);
                clazz.addField(
                        field,
                        INTERNAL_GET_DYNAMIC_TEXT_MODEL_METHOD_FULL_NAME + "(\""
                                + StringMicroOptimizationUtil.escapeJavaStringLiteral(DYNAMIC_MODELS.storeValue(value))
                                + "\")"
                );
            } catch (final CannotCompileException e) {
                throw new IllegalStateException(
                        "Could not add public static final field \"" + fieldName + " \" to generated TextModel "
                                + "to " + clazz + " to store dynamic TextModel element", e
                );
            }
        }
    }
}
