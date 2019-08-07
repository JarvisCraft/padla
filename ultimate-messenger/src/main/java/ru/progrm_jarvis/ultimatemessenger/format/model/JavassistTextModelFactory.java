package ru.progrm_jarvis.ultimatemessenger.format.model;

import javassist.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.classload.ClassFactory;
import ru.progrm_jarvis.javacommons.lazy.Lazy;
import ru.progrm_jarvis.javacommons.util.ClassNamingStrategy;
import ru.progrm_jarvis.ultimatemessenger.format.util.StringMicroOptimizationUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Implementation of {@link TextModelFactory text model factory} which uses runtime class generation.
 */
public class JavassistTextModelFactory<T> implements TextModelFactory<T> {

    /**
     * Lazy singleton of this text model factory
     */
    private static final Lazy<JavassistTextModelFactory> INSTANCE
            = Lazy.createThreadSafe(JavassistTextModelFactory::new);

    /**
     * Returns this {@link TextModelFactory text model factory} singleton.
     *
     * @param <T> generic type of got {@link TextModelFactory text model factory}
     * @return shared instance of this {@link TextModelFactory text model factory}
     */
    @SuppressWarnings("unchecked")
    @NotNull public static <T> JavassistTextModelFactory<T> get() {
        return INSTANCE.get();
    }

    @Override
    public TextModelFactory.TextModelBuilder<T> newBuilder() {
        return new TextModelBuilder<>();
    }

    /**
     * Implementation of
     * {@link TextModelFactory.TextModelBuilder text model builder}
     * which uses runtime class generation
     * and is capable of joining nearby static text blocks and optimizing {@link #createAndRelease()}.
     *
     * @param <T> type of object according to which the created text models are formatted
     */
    @ToString
    @EqualsAndHashCode(callSuper = true) // simply, why not? :) (this will also allow caching of instances)
    @FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
    protected static class TextModelBuilder<T> extends AbstractGeneratingTextModelFactoryBuilder<T> {

        /**
         * Lazily initialized {@link ClassPool Javassist class pool}
         */
        private static Lazy<ClassPool> CLASS_POOL = Lazy.createThreadSafe(ClassPool::getDefault);

        /**
         * Lazily initialized {@link CtClass compile-time class} of {@link TextModel text model}
         */
        private static Lazy<CtClass> TEXT_MODEL_CT_CLASS = Lazy.createThreadSafe(() -> {
            val className = TextModel.class.getCanonicalName();
            try {
                return CLASS_POOL.get().getCtClass(className);
            } catch (final NotFoundException e) {
                throw new IllegalStateException("Unable to get CtClass by name " + className);
            }
        });

        /**
         * Result of {@link Modifier#PUBLIC} and {@link Modifier#FINAL} flags disjunction
         */
        private static final int PUBLIC_FINAL_MODIFIERS = Modifier.PUBLIC | Modifier.FINAL;

        /**
         * Class naming strategy used to allocate names for generated classes
         */
        @NonNull private static final ClassNamingStrategy CLASS_NAMING_STRATEGY = ClassNamingStrategy.createPaginated(
                TextModelBuilder.class.getCanonicalName() + "$$Generated$$TextModel$$"
        );

        @Override
        @NotNull public TextModel<T> performTextModelCreation(final boolean release) {
            val clazz = CLASS_POOL.get().makeClass(CLASS_NAMING_STRATEGY.get());
            val textModelCtClass = TEXT_MODEL_CT_CLASS.get();
            clazz.setModifiers(PUBLIC_FINAL_MODIFIERS);
            clazz.setInterfaces(new CtClass[]{textModelCtClass});

            val dynamicModels = new TextModel[dynamicElementCount];

            { // Constructor (initializing fields)
                val constructorSignature = new CtClass[dynamicElementCount];
                Arrays.fill(constructorSignature, textModelCtClass);
                try {
                    clazz.addConstructor(CtNewConstructor.skeleton(constructorSignature, null, clazz));
                } catch (final CannotCompileException e) {
                    throw new IllegalStateException("Could not add constructor to generated TextModel");
                }
            }
            { // Fields (holding dynamic text models)
                for (var i = 0; i < dynamicElementCount; i++) {
                    try {
                        val field = new CtField(textModelCtClass, "d" + i, clazz);
                        field.setModifiers(PUBLIC_FINAL_MODIFIERS);
                        clazz.addField(field, CtField.Initializer.byParameter(i));
                    } catch (final CannotCompileException e) {
                        throw new IllegalStateException("Could not add field to generated TextModel");
                    }
                }
            }

            { // Method (#getText(T))
                StringBuilder src;
                if (staticLength == 0) { // constructor StringBuilder from the first object
                    // only dynamic elements (yet, there are multiple of those)
                    src = new StringBuilder("public String getText(Object t){return new StringBuilder(d0.getText(t))");
                    dynamicModels[0] = elements.get(0).getDynamicContent();
                    // dynamic elements count is at least 2
                    for (var index = 1; index < dynamicElementCount; index++) {
                        dynamicModels[index] = elements.get(index).getDynamicContent();
                        src.append(".append(d").append(index).append(".getText(t))"); // .append(d#.getText(t))
                    }
                } else {
                    src = new StringBuilder(
                            "public String getText(Object t){return new StringBuilder("
                    ).append(staticLength).append(')');
                    // there are static elements
                    int dynamicIndex = 0;
                    for (val element : elements) if (element.isDynamic()) {
                        dynamicModels[dynamicIndex] = element.getDynamicContent();
                        src.append(".append(d").append(dynamicIndex++).append(".getText(t))"); // .append(d#.getText(t))
                    } else {
                        val staticContent = element.getStaticContent();
                        if (staticContent.length() == 1) src.append(".append(\'").append(
                                StringMicroOptimizationUtil.escapeJavaCharacterLiteral(staticContent.charAt(0))
                        ).append('\'').append(')');
                        else src.append(".append(\"").append(
                                StringMicroOptimizationUtil.escapeJavaStringLiteral(staticContent)
                        ).append('"').append(')');
                    }
                }

                try {
                    clazz.addMethod(CtMethod.make(src.append(".toString();}").toString(), clazz));
                } catch (final CannotCompileException e) {
                    throw new IllegalStateException("Could not add method to generated TextModel");
                }
            }

            val constructorSignature = new Class<?>[dynamicElementCount];
            Arrays.fill(constructorSignature, TextModel.class);
            try {
                val constructor = ClassFactory.defineGCClass(clazz).getDeclaredConstructor(constructorSignature);
                constructor.setAccessible(true);
                //noinspection unchecked,RedundantCast
                return (TextModel<T>) constructor.newInstance((Object[]) dynamicModels);
            } catch (final IOException | CannotCompileException | NoSuchMethodException
                    | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Could not compile and instantiate TextModel from the given elements");
            }
        }
    }
}
