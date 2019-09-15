package ru.progrm_jarvis.javacommons.bytecode.asm.annotation;

/**
 * Marker indicating how the annotated {@link org.objectweb.asm.MethodVisitor method's} maximums get involved.
 */
public @interface MethodMaximums {

    /**
     * Gets the maximal value by which the stack should grow for this method to work normally.
     *
     * @return the value by which the stack should grow maximally
     */
    int stackSize() default 0;

    /**
     * Gets the maximal value by which the amount of locals should grow for this method to work normally.
     *
     * @return the value by which the amount of locals should grow maximally
     */
    int localsCount() default 0;
}
