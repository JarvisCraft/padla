package ru.progrm_jarvis.javacommons.annotation;

import java.lang.annotation.*;

/**
 * Marker indicating that the annotated object is part of an internal API and so (in most cases)
 * should not be used by the end-user of the API.
 */
@Inherited
@Documented
public @interface Internal {

    /**
     * Retrieves the reason why this API is internal.
     *
     * @return the reason why this API is internal
     */
    String value() default "";
}
