package ru.progrm_jarvis.javacommons.collection;

import javassist.*;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import ru.progrm_jarvis.javacommons.bytecode.CommonBytecodeLibrary;
import ru.progrm_jarvis.javacommons.bytecode.annotation.UsesBytecodeModification;
import ru.progrm_jarvis.javacommons.cache.Cache;
import ru.progrm_jarvis.javacommons.cache.Caches;
import ru.progrm_jarvis.javacommons.classloading.ClassNamingStrategy;
import ru.progrm_jarvis.javacommons.classloading.GcClassDefiners;
import ru.progrm_jarvis.javacommons.lazy.Lazy;
import ru.progrm_jarvis.javacommons.object.Pair;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static java.util.Collections.unmodifiableSet;

/**
 * Factory used for creation of special {@link java.util.Collection collections} at classload.
 */
@UtilityClass
public class CollectionFactory {

    /**
     * {@link Lookup lookup} of this class.
     */
    private static final Lookup LOOKUP = MethodHandles.lookup();

    /**
     * Class naming strategy used to allocate names for generated immutable enum set classes
     */
    private static final @NonNull ClassNamingStrategy IMMUTABLE_ENUM_SET_CLASS_NAMING_STRATEGY = ClassNamingStrategy
            .createPaginated(CollectionFactory.class.getName() + "$$Generated$$ImmutableEnumSet$$");

    /**
     * Default javassist class pool
     */
    private final @NonNull Lazy<ClassPool> CLASS_POOL = Lazy.createThreadSafe(ClassPool::getDefault);

    /**
     * Cache of instances of generated enum sets using naturally sorted array of its elements as the key
     */
    private final @NonNull Cache<Enum<?>[], Set<Enum<?>>> IMMUTABLE_ENUM_SETS = Caches.weakValuesCache();

    /**
     * {@link CtClass} representation of {@link AbstractImmutableSet} wrapped in {@link Lazy}
     */
    private static final @NonNull Lazy<CtClass> ABSTRACT_IMMUTABLE_SET_CT_CLASS = Lazy
            .createThreadSafe(() -> toCtClass(AbstractImmutableSet.class));
    /**
     * Array storing single reference to {@link CtClass} representation of {@link Iterator} wrapped in {@link Lazy}
     */
    private static final @NonNull Lazy<CtClass[]> ITERATOR_CT_CLASS_ARRAY = Lazy
            .createThreadSafe(() -> new CtClass[]{toCtClass(Iterator.class)});

    /**
     * Empty array of {@link CtClass}es.
     */
    public static final @NotNull CtClass @NotNull @Unmodifiable [] EMPTY_CT_CLASS_ARRAY = new CtClass[0];

    /**
     * Creates an immutable enum {@link Set set} from the given array of stored enum constants.
     *
     * @param values enum constants to be stored in the given set
     * @param <E> type of enum
     * @return runtime-compiled optimized immutable enum {@link Set set} for the given enum values or a pre-compiled n
     * empty set if {@code values} is empty
     *
     * @apiNote at current, the instances are cached and not GC-ed (as their classes) so this should be used carefully
     * @apiNote this implementation of immutable enum {@link Set}s is specific and should be used carefully {@code
     * instanceof} and {@code switch} by {@link Enum#ordinal()} are used for containment-related checks and {@link}
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    @Deprecated // should be remade via ASM or totally removed due to specific behaviour of anonymous class referencing
    @UsesBytecodeModification(value = CommonBytecodeLibrary.JAVASSIST, optional = true)
    public <E extends Enum<E>> Set<E> createImmutableEnumSet(final @NonNull E... values) {
        if (values.length == 0) return Collections.emptySet();

        if (CommonBytecodeLibrary.JAVASSIST.isAvailable()) {
            // sort enum values so that the cache does not store different instances
            // for different orders of same elements
            @SuppressWarnings("SuspiciousArrayCast") val enumValues = (E[]) Arrays.stream(values)
                    .distinct()
                    .sorted()
                    .toArray(Enum[]::new);

            //noinspection unchecked
            return (Set<E>) IMMUTABLE_ENUM_SETS.get(enumValues, valuesArray -> {
                //<editor-fold desc="Class generation" defaultstate="collapsed">
                val enumType = valuesArray.getClass().getComponentType();
                // name of the class of the enum
                val enumTypeName = enumType.getCanonicalName();
                // number of enum constants
                val elementsCount = enumValues.length;
                // class pool instance
                val classPool = CLASS_POOL.get();
                // generated class
                val clazz = classPool.makeClass(IMMUTABLE_ENUM_SET_CLASS_NAMING_STRATEGY.get());
                setSuperClass(clazz, ABSTRACT_IMMUTABLE_SET_CT_CLASS.get());
                addEmptyConstructor(clazz);

                /* Generated methods */

                // `boolean contains(Object)` method
                {
                    val src = new StringBuilder("public boolean contains(Object element) {");
                    // simply check type if any enum values is included
                    if (Arrays.equals(enumValues, enumType.getEnumConstants())) src
                            .append("return element instanceof ").append(enumTypeName).append(';');
                        // use ordinal switch if not all enum values are included
                    else {
                        src
                                .append("if (element instanceof ").append(enumTypeName).append(")")
                                .append("switch (((Enum) element).ordinal()) {");

                        for (val enumValue : enumValues)
                            src
                                    .append("case ").append(enumValue.ordinal()).append(": return true;");

                        src.append("default: return false;").append("}").append("return false;");
                    }
                    addCtMethod(src.append('}').toString(), clazz);
                }

                // `boolean isEmpty()` method
                addCtMethod(
                        "public boolean isEmpty() {return false;}",
                        clazz
                );

                // `int size()` method
                addCtMethod(
                        "public int size() {return " + elementsCount + ";}",
                        clazz
                );


                val enumNames = Arrays.stream(enumValues)
                        .map(element -> enumTypeName + '.' + element.name())
                        .toArray(String[]::new);
                { // field for storing array of elements and `toArray` methods
                    val newArrayStatement = "new " + enumTypeName + "[]{" + String.join(",", enumNames) + '}';

                    // add field with maximal accessibility so that it can be JITed as much as possible
                    addCtField(
                            "public static final " + enumTypeName + "[] ARRAY_DATA = " + newArrayStatement + ';', clazz
                    );

                    // non-generic `toArray`
                    addCtMethod(
                            "public Object[] toArray() {return " + newArrayStatement + ";}",
                            clazz
                    );
                /* Not following the contract
                addCtMethod(""
                                + "public <T> T[] toArray(final T[] target) {"
                                + "if (!target.getClass().getComponentType()"
                                + ".isAssignableFrom(" + enumClassName + ".class)) throw new ArrayStoreException("
                                + "\"Attempt to call toArray(T[]) with wrong array type ("
                                + enumClassName + " or its super-type was expected)\");"
                                + "return (T[])" + newArrayStatement +";"
                                + "}",
                        clazz
                );
                 */
                    // generic `toArray`
                    // don't forget that javassist is not friendly with those and so use manual type erasure
                    addCtMethod(
                            "public Object[] toArray(Object[] target) {"
                                    + "if (target.length < " + elementsCount + ") return java.util.Arrays.copyOf("
                                    + "ARRAY_DATA, " + elementsCount + ", target.getClass()"
                                    + ");"
                                    + "System.arraycopy(ARRAY_DATA, 0, target, 0, " + elementsCount + ");"
                                    + "if (target.length > " + elementsCount + ") target[" + elementsCount + "] = null;"
                                    + "return target;"
                                    + "}",
                            clazz
                    );
                }

                {// `void forEach(Consumer)`
                    val src = new StringBuilder(
                            "public void forEach(java.util.function.Consumer consumer) {"
                                    + "if (consumer == null) throw new NullPointerException(\"consumer should not be "
                                    + "empty\");"
                    );
                    for (val enumName : enumNames)
                        src.append("consumer.accept(").append(enumName).append(')').append(';');

                    addCtMethod(src.append('}').toString(), clazz);
                }
                // `hashCode()`
                addCtMethod("int hashCode() {return " + Arrays.hashCode(valuesArray) + ";}", clazz);
                // `equals()`
                {// `boolean equals(Object)`
                    addCtMethod(
                            "public boolean equals(Object object) {"
                                    + "if (object == this) return true;"
                                    + "if (!(object instanceof java.util.Collection)) return false;"
                                    + "java.util.Collection set =  (java.util.Collection) object;"
                                    + "if (set.size() != " + elementsCount + ") return set.size()"
                                    + " == missingValue && set.containsAll(this);"
                                    + ""
                                    + "}",
                            clazz
                    );
                }

            /*
            Generate iterator
             */
                final CtClass iteratorClazz;
                {
                    // Use short name as Javassist appends it to parent class name
                    iteratorClazz = clazz.makeNestedClass("Iterator", true);
                    iteratorClazz.setInterfaces(ITERATOR_CT_CLASS_ARRAY.get());
                    addEmptyConstructor(iteratorClazz);

                    addCtField("public int index = 0;", iteratorClazz);
                    addCtMethod(
                            "public java.util.Iterator iterator() {"
                                    + "return new " + iteratorClazz.getName() + "();"
                                    + "}",
                            clazz
                    );

                    // boolean hasNext()
                    addCtMethod("public boolean hasNext() {return index < " + elementsCount + ";}", iteratorClazz);
                    {
                        // T next()
                        final StringBuilder src = new StringBuilder("public Object next() {switch (index++) {");

                        for (var i = 0; i < elementsCount; i++)
                            src
                                    .append("case ").append(i).append(':').append("return ").append(enumNames[i])
                                    .append(';');

                        addCtMethod(
                                src.append("default: throw new java.util.NoSuchElementException();}}")
                                        .toString(),
                                iteratorClazz
                        );
                    }
                }

                final Constructor<Set<Enum<?>>> constructor;
                {
                    final byte[] iteratorBytecode, bytecode;
                    try {
                        iteratorBytecode = iteratorClazz.toBytecode();
                        bytecode = clazz.toBytecode();
                    } catch (final IOException | CannotCompileException e) {
                        throw new RuntimeException("Cannot get bytecode for generated classes", e);
                    }

                    val loadedClass = GcClassDefiners.getDefault()
                            .defineClasses(
                                    LOOKUP,
                                    Pair.of(iteratorClazz.getName(), iteratorBytecode),
                                    Pair.of(clazz.getName(), bytecode)
                            )[1];
                    try {
                        constructor = (Constructor<Set<Enum<?>>>) loadedClass.getDeclaredConstructor();
                    } catch (final NoSuchMethodException e) {
                        throw new RuntimeException("Could not find generated constructor", e);
                    }
                }

                try {
                    return constructor.newInstance();
                } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Could not call generated constructor", e);
                }
                //</editor-fold>
            });
        } else {
            val length = values.length;
            switch (length) {
                case 1: return unmodifiableSet(EnumSet.of(values[0]));
                case 2: return unmodifiableSet(EnumSet.of(values[0], values[1]));
                case 3: return unmodifiableSet(EnumSet.of(values[0], values[1], values[2]));
                case 4: return unmodifiableSet(EnumSet.of(values[0], values[1], values[2], values[3]));
                case 5: return unmodifiableSet(EnumSet.of(values[0], values[1], values[2], values[3], values[4]));
                default: return unmodifiableSet(EnumSet.copyOf(Arrays.asList(values)));
            }
        }
    }

    /**
     * Gets {@link CtClass} representation of the given class.
     *
     * @param clazz class whose {@link CtClass} representation is needed
     * @return {@link CtClass} representation of the given class
     *
     * @throws IllegalStateException if it is impossible to find {@link CtClass} representation of the given class
     */
    private @NotNull CtClass toCtClass(final @NotNull Class<?> clazz) {
        try {
            return CLASS_POOL.get().get(clazz.getName());
        } catch (final NotFoundException e) {
            throw new IllegalStateException("Unable to get CtClass by name " + clazz.getName(), e);
        }
    }

    /**
     * Adds an empty constructor to given {@link CtClass}.
     *
     * @param targetClass class to which to add an empty constructor
     */
    @SneakyThrows(CannotCompileException.class)
    private void addEmptyConstructor(final @NotNull CtClass targetClass) {
        targetClass.addConstructor(CtNewConstructor.make(EMPTY_CT_CLASS_ARRAY, EMPTY_CT_CLASS_ARRAY, targetClass));
    }

    /**
     * Sets the super-class of the given {@link CtClass}.
     *
     * @param targetClass class to which to set the super-class
     * @param superClass super-class of the target class
     */
    @SneakyThrows(CannotCompileException.class)
    private void setSuperClass(final @NotNull CtClass targetClass, final @NotNull CtClass superClass) {
        targetClass.setSuperclass(superClass);
    }

    /**
     * Adds a field based on given source code to given {@link CtClass}.
     *
     * @param src source code of the field
     * @param targetClass class to which to add the field
     */
    @SneakyThrows(CannotCompileException.class)
    private void addCtField(
            @NonNull @Language(value = "java", prefix = "public class Foo extends GeneratedImmutableEnumSetTemplate {",
                               suffix = "}") final String src, final @NonNull CtClass targetClass) {
        targetClass.addField(CtField.make(src, targetClass));
    }

    /**
     * Adds a method based on given source code to given {@link CtClass}.
     *
     * @param src source code of the method
     * @param targetClass class to which to add the method
     */
    @SneakyThrows(CannotCompileException.class)
    private void addCtMethod(
            @NonNull @Language(value = "java", prefix = "public class Foo extends GeneratedImmutableEnumSetTemplate {",
                               suffix = "}") final String src, final @NonNull CtClass targetClass) {
        targetClass.addMethod(CtNewMethod.make(src, targetClass));
    }
}
