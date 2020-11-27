package ru.progrm_jarvis.javacommons.collection;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import ru.progrm_jarvis.javacommons.classloading.ClassNamingStrategy;
import ru.progrm_jarvis.javacommons.classloading.GcClassDefiners;
import ru.progrm_jarvis.javacommons.lazy.Lazy;
import ru.progrm_jarvis.javacommons.object.Pair;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static java.lang.Integer.getInteger;
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
     * Name of a property specifying concurrency level of {@link #IMMUTABLE_ENUM_SETS instances cache}
     */
    private final @NonNull String IMMUTABLE_ENUM_SET_INSTANCE_CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME
            = CollectionFactory.class.getCanonicalName() + ".immutable-enum-set-instance-cache-concurrency-level";

    /**
     * Default javassist class pool
     */
    private final @NonNull Lazy<ClassPool> CLASS_POOL = Lazy.createThreadSafe(ClassPool::getDefault);

    /**
     * Cache of instances of generated enum sets using naturally sorted array of its elements as the key
     */
    private final @NonNull Cache<Enum<?>[], Set<Enum<?>>> IMMUTABLE_ENUM_SETS
            = CacheBuilder
            .newBuilder()
            .concurrencyLevel(
                    Math.max(4, getInteger(IMMUTABLE_ENUM_SET_INSTANCE_CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME, 4))
            )
            .weakValues()
            .build();

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
     * Gets {@link CtClass} representation of the given class.
     *
     * @param clazz class whose {@link CtClass} representation is needed
     * @return {@link CtClass} representation of the given class
     *
     * @throws IllegalStateException if it is impossible to find {@link CtClass} representation of the given class
     */
    private CtClass toCtClass(final @NonNull Class<?> clazz) {
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
     * @throws CannotCompileException if a javassist compilation exception occurs
     */
    private void addEmptyConstructor(final @NonNull CtClass targetClass) throws CannotCompileException {
        targetClass.addConstructor(CtNewConstructor.make(EMPTY_CT_CLASS_ARRAY, EMPTY_CT_CLASS_ARRAY, targetClass));
    }

    /**
     * Adds a field based on given source code to given {@link CtClass}.
     *
     * @param src source code of the field
     * @param targetClass class to which to add the field
     * @throws CannotCompileException if a javassist compilation exception occurs
     */
    private void addCtField(
            @NonNull @Language(value = "java", prefix = "public class Foo extends GeneratedImmutableEnumSetTemplate {",
                               suffix = "}") final String src, final @NonNull CtClass targetClass)
            throws CannotCompileException {
        targetClass.addField(CtField.make(src, targetClass));
    }

    /**
     * Adds a method based on given source code to given {@link CtClass}.
     *
     * @param src source code of the method
     * @param targetClass class to which to add the method
     * @throws CannotCompileException if a javassist compilation exception occurs
     */
    private void addCtMethod(
            @NonNull @Language(value = "java", prefix = "public class Foo extends GeneratedImmutableEnumSetTemplate {",
                               suffix = "}") final String src, final @NonNull CtClass targetClass)
            throws CannotCompileException {
        targetClass.addMethod(CtNewMethod.make(src, targetClass));
    }

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
    @SneakyThrows(ExecutionException.class)
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
            return (Set<E>) IMMUTABLE_ENUM_SETS.get(enumValues, () -> {
                //<editor-fold desc="Class generation" defaultstate="collapsed">
                val enumType = values.getClass().getComponentType();
                // name of the class of the enum
                val enumTypeName = enumType.getCanonicalName();
                // number of enum constants
                val elementsCount = enumValues.length;
                // class pool instance
                val classPool = CLASS_POOL.get();
                // generated class
                val clazz = classPool.makeClass(IMMUTABLE_ENUM_SET_CLASS_NAMING_STRATEGY.get());
                clazz.setSuperclass(ABSTRACT_IMMUTABLE_SET_CT_CLASS.get());
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
                addCtMethod("int hashCode() {return " + Arrays.hashCode(values) + ";}", clazz);
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

                return (Set<Enum<?>>) GcClassDefiners.getDefault()
                        .orElseThrow(() -> new IllegalStateException("GC-ClassDefiner is unavailable"))
                        .defineClasses(
                                LOOKUP,
                                Pair.of(iteratorClazz.getName(), iteratorClazz.toBytecode()),
                                Pair.of(clazz.getName(), clazz.toBytecode())
                        )[1].getDeclaredConstructor().newInstance();
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
}
