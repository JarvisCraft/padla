package ru.progrm_jarvis.javacommons.annotation;

import java.lang.annotation.*;

/**
 * Marker indicating that usage of the annotated member requires using PADLA patcher on classes relying on this API.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME) // this ,ay be used by useragent-based implementations
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface RequiresClassPatching {}
