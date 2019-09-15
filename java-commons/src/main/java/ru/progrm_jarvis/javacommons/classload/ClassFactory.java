package ru.progrm_jarvis.javacommons.classload;

import javassist.CannotCompileException;
import javassist.CtClass;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.pair.Pair;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for {@link Class}-related features.
 */
@UtilityClass
public class ClassFactory {

    /**
     * Defines a class which may be garbage-collected.
     *
     * @param name canonical name of the class to define
     * @param bytecode bytecode of the class
     * @return defined class
     *
     * @see #defineGCClass(String, byte[])
     * @see #defineGCClasses(Map)
     * @see #defineGCClass(CtClass)
     * @see #defineGCClasses(CtClass...)
     */
    public Class<?> defineGCClass(@NonNull final String name, @NonNull final byte[] bytecode) {
        return new TmpClassLoader().define(name, bytecode);
    }

    /**
     * Defines multiple classes which may be garbage-collected.
     *
     * @param bytecodes pairs whose first values are canonical class names
     * and the second values are those classes' bytecodes
     * @return defined class in the order their data was passed
     *
     * @see #defineGCClass(String, byte[])
     * @see #defineGCClasses(Map)
     * @see #defineGCClass(CtClass)
     * @see #defineGCClasses(CtClass...)
     */
    @SafeVarargs
    public Class<?>[] defineGCClasses(@NonNull final Pair<String, byte[]>... bytecodes) {
        val classLoader = new TmpClassLoader();

        val length = bytecodes.length;
        val classes = new Class<?>[length];
        for (var i = 0; i < length; i++) {
            val namedClass = bytecodes[i];
            classes[i] = classLoader.define(namedClass.getFirst(), namedClass.getSecond());
        }

        return classes;
    }

    /**
     * Defines multiple classes which may be garbage-collected.
     *
     * @param namedBytecode map containing bytecodes by their classes' canonical names
     * @return defined classes by their names
     *
     * @see #defineGCClass(String, byte[])
     * @see #defineGCClasses(Pair[])
     * @see #defineGCClass(CtClass)
     * @see #defineGCClasses(CtClass...)
     */
    public Map<String, Class<?>> defineGCClasses(@NonNull final Map<String, byte[]> namedBytecode) {
        val classLoader = new TmpClassLoader();

        val classes = new HashMap<String, Class<?>>();
        for (val entry : namedBytecode.entrySet()) {
            val name = entry.getKey();
            classes.put(name, classLoader.define(name, entry.getValue()));
        }

        return classes;
    }

    /**
     * Defines a class which may be garbage-collected.
     *
     * @param ctClass javassist's compile-time class
     * @return defined class
     *
     * @throws IOException if an Input/Output problem occurs while defining a class
     * @throws CannotCompileException if one of the compile-time classes cannot be compiled
     *
     * @see #defineGCClass(String, byte[])
     * @see #defineGCClasses(Map)
     * @see #defineGCClasses(CtClass...)
     * @see #defineGCClasses(CtClass...)
     */
    public Class<?> defineGCClass(@NonNull final CtClass ctClass)
            throws IOException, CannotCompileException {
        return new TmpClassLoader().define(ctClass.getName(), ctClass.toBytecode());
    }

    /**
     * Defines multiple classes which may be garbage-collected.
     *
     * @param classes javassist's compile-time classes
     * @return defined classes in the order their data was passed
     *
     * @throws IOException if an Input/Output problem occurs while defining a class
     * @throws CannotCompileException if one of the compile-time classes cannot be compiled
     *
     * @see #defineGCClass(String, byte[])
     * @see #defineGCClasses(CtClass...)
     * @see #defineGCClasses(Map)
     * @see #defineGCClass(CtClass)
     */
    public Class<?>[] defineGCClasses(@NonNull final CtClass... classes) throws IOException, CannotCompileException {
        val classLoader = new TmpClassLoader();

        val length = classes.length;
        val loadedClasses = new Class<?>[length];
        for (var i = 0; i < length; i++) {
            val ctClass = classes[i];
            loadedClasses[i] = classLoader.define(ctClass.getName(), ctClass.toBytecode());
        }

        return loadedClasses;
    }

    /**
     * Temporary classloader which should be instantiated for groups of related classes which may be unloaded.
     */
    private static class TmpClassLoader extends ClassLoader {

        /**
         * Defines a class by the given bytecode.
         *
         * @param name canonical name of the class
         * @param bytecode bytecode of the class
         * @return defined class
         */
        private Class<?> define(@NotNull/* internal API*/ final String name,
                                @NonNull/* internal API*/ final byte[] bytecode) {
            return defineClass(name, bytecode, 0, bytecode.length);
        }
    }
}
