package ru.progrm_jarvis.javacommons.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

/**
 * Marker indicating that the annotated type parameter does not matter
 * thus any unchecked casts from this type to <b>any</b> other are safe and never lead to unspecified behaviour.
 */
@Inherited
@Documented
@Target(ElementType.TYPE_PARAMETER)
public @interface Any {}
