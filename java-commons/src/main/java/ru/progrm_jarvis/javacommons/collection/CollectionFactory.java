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
import ru.progrm_jarvis.javacommons.lazy.Lazy;
import ru.progrm_jarvis.javacommons.util.ClassNamingStrategy;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.lang.Integer.getInteger;

/**
 * Factory used for creation of special {@link java.util.Collection collections} at runtime.
 */
@UtilityClass
public class CollectionFactory {

    /**
     * Canonical name of this class
     */
    private static final String THIS_CANONICAL_CLASS_NAME = CollectionFactory.class.getCanonicalName();

    /**
     * Class naming strategy used to allocate names for generated classes
     */
    @NonNull private static final ClassNamingStrategy CLASS_NAMING_STRATEGY = ClassNamingStrategy
            .createPaginated(THIS_CANONICAL_CLASS_NAME + "$$generated$$");

    /**
     * Name of a property specifying concurrency level of {@link #IMMUTABLE_ENUM_SETS instances cache}
     */
    @NonNull private final String IMMUTABLE_ENUM_SET_INSTANCE_CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME
            = THIS_CANONICAL_CLASS_NAME + ".immutable-enum-set-instance-cache-concurrency-level";

    /**
     * Default javassist class pool
     */
    @NonNull private final Lazy<ClassPool> CLASS_POOL = Lazy.createThreadSafe(ClassPool::getDefault);

    /**
     * Cache of instances of generated enum sets using naturally sorted array of its elements as the key
     */
    @SuppressWarnings("UnstableApiUsage") @NonNull
    private final Cache<Enum<?>[], Set<Enum<?>>> IMMUTABLE_ENUM_SETS = CacheBuilder
            .newBuilder()
            .concurrencyLevel(
                    Math.max(4, getInteger(IMMUTABLE_ENUM_SET_INSTANCE_CACHE_CONCURRENCY_LEVEL_SYSTEM_PROPERTY_NAME, 4))
            )
            .build();

    /**
     * {@link CtClass} representation of {@link AbstractImmutableSet} wrapped in {@link Lazy}
     */
    @NonNull private static final Lazy<CtClass> ABSTRACT_IMMUTABLE_SET_CT_CLASS = Lazy
            .createThreadSafe(() -> toCtClass(AbstractImmutableSet.class));
    /**
     * Array storing single reference to {@link CtClass} representation of {@link Iterator} wrapped in {@link Lazy}
     */
    @NonNull private static final Lazy<CtClass[]> ITERATOR_CT_CLASS_ARRAY = Lazy
            .createThreadSafe(() -> new CtClass[]{toCtClass(Iterator.class)});

    /**
     * Gets {@link CtClass} representation of the given class.
     *
     * @param clazz class whose {@link CtClass} representation is needed
     * @return {@link CtClass} representation of the given class
     *
     * @throws IllegalStateException if it is impossible to find {@link CtClass} representation of the given class
     */
    private CtClass toCtClass(@NonNull final Class<?> clazz) {
        val className = clazz.getCanonicalName();
        try {
            return CLASS_POOL.get().get(className);
        } catch (final NotFoundException e) {
            throw new IllegalStateException("Unable to get CtClass by name " + className);
        }
    }

    /**
     * Adds an empty constructor to given {@link CtClass}.
     *
     * @param targetClass class to which to add an empty constructor
     *
     * @throws CannotCompileException if a javassist compilation exception occurs
     */
    private void addEmptyConstructor(@NonNull final CtClass targetClass) throws CannotCompileException {
        val constructor = CtNewConstructor.make(new CtClass[0], new CtClass[0], targetClass);
        targetClass.addConstructor(constructor);
    }

    /**
     * Adds a field based on given source code to given {@link CtClass}.
     *
     * @param src source code of the field
     * @param targetClass class to which to add the field
     *
     * @throws CannotCompileException if a javassist compilation exception occurs
     */
    private void addCtField(
            @NonNull @Language(value = "java", prefix = "public class Foo extends GeneratedImmutableEnumSetTemplate {",
                               suffix = "}") final String src, @NonNull final CtClass targetClass)
            throws CannotCompileException {
        val field = CtField.make(src, targetClass);
        targetClass.addField(field);
    }

    /**
     * Adds a method based on given source code to given {@link CtClass}.
     *
     * @param src source code of the method
     * @param targetClass class to which to add the method
     *
     * @throws CannotCompileException if a javassist compilation exception occurs
     */
    private void addCtMethod(
            @NonNull @Language(value = "java", prefix = "public class Foo extends GeneratedImmutableEnumSetTemplate {",
                               suffix = "}") final String src, @NonNull final CtClass targetClass)
            throws CannotCompileException {
        val method = CtNewMethod.make(src, targetClass);
        targetClass.addMethod(method);
    }

    // TODO GC-capable class-loading
    // TODO equals
    /**
     * Creates an immutable enum {@link Set set} from the given array of stored enum constants.
     *
     * @param values enum constants to be stored in the given set
     * @param <E> type of enum
     * @return runtime-compiled optimized immutable enum {@link Set set} for the given enum values
     * or a pre-compiled n empty set if {@code values} is empty
     *
     * @apiNote at current, the instances are cached and not GC-ed (as their classes) so this should be used carefully
     * @apiNote this implementation of immutable enum {@link Set}s is specific and should be used carefully
     * {@code instanceof} and {@code switch} by {@link Enum#ordinal()} are used for containment-related checks
     * and {@link}
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    @SneakyThrows(ExecutionException.class)
    public <E extends Enum<E>> Set<E> createImmutableEnumSet(@NonNull final E... values) {
        if (values.length == 0) return Collections.emptySet();

        // sort enum values so that the cache does not store different instances for different orders of same
        // elements
        //noinspection unchecked,SuspiciousToArrayCall
        val enumValues = (E[]) Arrays.stream(values)
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
            val clazz = classPool.makeClass(CLASS_NAMING_STRATEGY.get());
            clazz.setSuperclass(ABSTRACT_IMMUTABLE_SET_CT_CLASS.get());
            addEmptyConstructor(clazz);

            /* Generated methods */

            // `boolean contains(Object)` method
            //noinspection ConstantConditions
            addCtMethod(
                    "public boolean contains(Object element) {"
                            + (
                            Arrays.equals(enumValues, enumType.getEnumConstants())
                                    // simple check type if any enum values is included
                                    ? "return element instanceof " + enumTypeName + ';'
                                    // use switch if not all enum values are included
                                    : "if (element instanceof " + enumTypeName + ")"
                                            + "switch (((Enum) element).ordinal()) {"
                                            // enums switch forces usage of short names
                                            + Arrays.stream(enumValues)
                                            .map(element -> "case " + element.ordinal() + ": return true;")
                                            .collect(Collectors.joining())
                                            + "default: return false;"
                                            + "}"
                                            + "return false;"
                    ) + "}",
                    clazz
            );

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
                        + "if (consumer == null) throw new NullPointerException(\"consumer should not be empty\");"
                );
                for (val enumName : enumNames) src.append("consumer.accept(").append(enumName).append(')').append(';');

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
                                + "if (set.size() != " + elementsCount + ") return false;"
                                + "return set.containsAll(this);"
                                + "}",
                        clazz
                );
            }

            /*
            Generate iterator
             */
            {
                val iteratorClazz = clazz.makeNestedClass(CLASS_NAMING_STRATEGY.get(), true);
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
                iteratorClazz.toClass();
            }

            return (Set<Enum<?>>) clazz.toClass().newInstance();
            //</editor-fold>
        });
    }
}
