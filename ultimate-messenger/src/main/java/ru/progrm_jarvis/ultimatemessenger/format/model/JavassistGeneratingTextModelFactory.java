package ru.progrm_jarvis.ultimatemessenger.format.model;

import javassist.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.progrm_jarvis.javacommons.classload.ClassFactory;
import ru.progrm_jarvis.javacommons.lazy.Lazy;
import ru.progrm_jarvis.javacommons.util.ClassNamingStrategy;
import ru.progrm_jarvis.ultimatemessenger.format.util.StringMicroOptimizationUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Implementation of {@link TextModelFactory text model factory} which uses runtime class generation.
 */
public class JavassistGeneratingTextModelFactory<T> implements TextModelFactory<T> {

    /**
     * Lazy singleton of this text model factory
     */
    private static final Lazy<JavassistGeneratingTextModelFactory> INSTANCE
            = Lazy.createThreadSafe(JavassistGeneratingTextModelFactory::new);

    /**
     * Returns this {@link TextModelFactory text model factory} singleton.
     *
     * @param <T> generic type of got {@link TextModelFactory text model factory}
     * @return shared instance of this {@link TextModelFactory text model factory}
     */
    @SuppressWarnings("unchecked")
    public static <T> JavassistGeneratingTextModelFactory<T> get() {
        return INSTANCE.get();
    }

    private static Lazy<ClassPool> CLASS_POOL = Lazy.createThreadSafe(ClassPool::getDefault);
    private static Lazy<CtClass> TEXT_MODEL_CT_CLASS = Lazy.createThreadSafe(() -> {
        val className = TextModel.class.getCanonicalName();
        try {
            return CLASS_POOL.get().getCtClass(className);
        } catch (final NotFoundException e) {
            throw new IllegalStateException("Unable to get CtClass by name " + className);
        }
    });
    private static final int PUBLIC_FINAL_MODIFIERS = Modifier.PUBLIC | Modifier.FINAL;

    /**
     * Class naming strategy used to allocate names for generated classes
     */
    @NonNull private static final ClassNamingStrategy CLASS_NAMING_STRATEGY = ClassNamingStrategy
            .createPaginated(JavassistGeneratingTextModelFactory.class.getCanonicalName() + "$$generated$$");

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

        @Override
        public TextModel<T> performTextModelCreation(final boolean release) {
            if (elements.isEmpty()) return TextModel.empty();

            if (dynamicElementCount == 0) return StaticTextModel.of(
                    elements.stream().map(Element::getStaticContent).collect(Collectors.joining())
            );

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
                final StringBuilder src = new StringBuilder(
                        "public String getText(Object t){return new StringBuilder("
                ).append(staticLength).append(')');
                {
                    int dynamicIndex = 0;

                    for (val element : elements) if (element.isDynamic()) {
                        dynamicModels[dynamicIndex] = element.getDynamicContent();
                        src.append(".append(d").append(dynamicIndex++).append(".getText(t))"); // .append(d#.getText(t))
                    } else src.append(".append(\"").append(
                            StringMicroOptimizationUtil.escapeJavaStringLiteral(element.getStaticContent())
                    ).append('"').append(')');

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
