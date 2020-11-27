package ru.progrm_jarvis.javacommons.ownership.annotation;

import java.lang.annotation.*;

/**
 * <p>Marker indicating the non-owner of a mutable object.
 * It is intended only for use with mutable types
 * for which it will indicate whether or not the operated object should be cloned for modification</p>
 * <p>Owner depends on the target:</p>
 * <dl>
 *     <dt>parameter</dt>
 *     <dd>caller of the method whose parameter it is</dd>
 *
 *     <dt>return type</dt>
 *     <dd>method whose returned value it is</dd>
 * </dl>
 *
 * @see Own counterpart
 */
@Documented
@Retention(RetentionPolicy.CLASS) // keep even if sources are unavailable
@Target(ElementType.TYPE_USE /* to allow on return type */)
public @interface Ref {}
