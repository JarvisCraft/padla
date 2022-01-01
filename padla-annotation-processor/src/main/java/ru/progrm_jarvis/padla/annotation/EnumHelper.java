package ru.progrm_jarvis.padla.annotation;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.lang.annotation.*;

/**
 * <p>Annotation enabling the generation of enum-helper - class providing helper methods for the annotated enum.</p>
 * <p>This annotation is only applicable to {@code enum}s.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumHelper {

    /**
     * Name of the generated helper class (including the package).
     * {@link Placeholders Placeholders} can be used in this one.
     *
     * @return name of the generated class
     */
    String value() default Placeholders.PACKAGE + '.' + Placeholders.NAME + 's';

    /**
     * Gets the codegen configuration.
     *
     * @return codegen configuration
     */
    Generate generate() default @Generate;

    /**
     * Gets the annotations' configuration.
     *
     * @return annotations' configuration
     */
    Annotations annotations() default @Annotations;

    /**
     * Codegen configuration.
     * This specifies which members should be generated.
     */
    @Target({})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Generate {

        /**
         * Special value which should be used whenever a member should not be generated
         */
        @NotNull String NONE = "";

        /**
         * Name of the generated list method or {@link #NONE} if none should be generated.
         *
         * @return name of the generated list method or {@link #NONE} if none should be generated.
         */
        String list() default "list";

        /**
         * Name of the generated set method or {@link #NONE} if none should be generated.
         *
         * @return name of the generated set method or {@link #NONE} if none should be generated.
         */
        String set() default "set";

        /**
         * Name of the generated stream method or {@link #NONE} if none should be generated.
         *
         * @return name of the generated stream method or {@link #NONE} if none should be generated.
         */
        String stream() default "stream";
    }


    /**
     * Annotations' configuration.
     * This specifies which annotations should be used for annotating members of the generated class.
     */
    @Target({})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Annotations {

        /**
         * Annotation to mark not-null values or {@link None}{@code .class} if none.
         *
         * @return annotation to mark not-null values or {@link None}{@code .class} if none
         */
        Class<? extends Annotation> notNull() default NotNull.class;

        /**
         * Annotation to mark nullable values or {@link None}{@code .class} if none.
         *
         * @return annotation to mark nullable values or {@link None}{@code .class} if none
         */
        Class<? extends Annotation> nullable() default Nullable.class;

        /**
         * Annotation to mark unmodifiable values or {@link None}{@code .class} if none.
         *
         * @return annotation to mark unmodifiable values or {@link None}{@code .class} if none
         */
        Class<? extends Annotation> unmodifiable() default Unmodifiable.class;

        /**
         * Annotation to mark unmodifiable value views or {@link None}{@code .class} if none.
         *
         * @return annotation to mark unmodifiable value views or {@link None}{@code .class} if none
         */
        Class<? extends Annotation> unmodifiableView() default UnmodifiableView.class;

        /**
         * Special annotation which should be used whenever no annotation should be present.
         */
        @Target({})
        @Retention(RetentionPolicy.RUNTIME)
        @interface None {}
    }

    /**
     * Placeholders. Applicable in {@link #value()}.
     */
    @UtilityClass
    class Placeholders {

        /**
         * Placeholder to be replaced with the package of the annotated enum.
         */
        public final @NotNull String PACKAGE = "{package}";

        /**
         * Placeholder to be replaced with the name of the annotated enum.
         */
        public final @NotNull String NAME = "{name}";
    }
}
