package ru.progrm_jarvis.javacommons.bytecode.annotation;

import ru.progrm_jarvis.javacommons.bytecode.CommonBytecodeLibrary;

import java.lang.annotation.*;

/**
 * Marker indicating that the annotated element uses (or attempts to use) bytecode-modification library(ies).
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME) // to allow special handling of those classes
public @interface UsesBytecodeModification {

    /**
     * Bytecode libraries requires for the target to work properly.
     *
     * @return array of bytecode libraries required for the target to work properly
     */
    CommonBytecodeLibrary[] value();

    /**
     * Marker indicating whether bytecode modifications are optional
     * and (in case of failed attempt to use the needed libraries) will be handled via non-bytecode-modifying APIs.
     *
     * @return {@code true} if the target is able to do its jub without bytecode libraries (using alternative APIs)
     * and {@code false} otherwise
     */
    boolean optional() default false;
}
