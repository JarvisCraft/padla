package ru.progrm_jarvis.javacommons.annotation;

import java.lang.annotation.*;

/**
 * Marker indicating that {@link Object#equals(Object)} and {@link Object#hashCode()} methods
 * are not overridden for this class for some reason.
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
public @interface DontOverrideEqualsAndHashCode {

    /**
     * Retrieves the reason why this class's {@link Object#equals(Object)} and {@link Object#hashCode()}
     * methods are not overridden.
     *
     * @return reason for not overriding {@link Object#hashCode()} and {@link Object#equals(Object)} methods
     */
    String value() default "";
}
