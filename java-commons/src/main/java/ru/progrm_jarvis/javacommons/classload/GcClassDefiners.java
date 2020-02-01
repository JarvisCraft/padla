package ru.progrm_jarvis.javacommons.classload;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.javacommons.annotation.Internal;
import ru.progrm_jarvis.javacommons.invoke.FullAccessLookupFactories;
import ru.progrm_jarvis.javacommons.lazy.Lazy;
import ru.progrm_jarvis.javacommons.object.ObjectUtil;
import ru.progrm_jarvis.javacommons.pair.Pair;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.*;

/**
 * Utility for accessing {@link ClassDefiner class definers} capable od defining garbage-collected classes.
 */
@UtilityClass
public class GcClassDefiners {

    /**
     * {@link ClassDefiner class definer}
     * based on {@code sun.misc.Unsafe#defineAnonymousClass(Class, byte[], Object[])}.
     */
    private Lazy<@Nullable ClassDefiner> UNSAFE_CLASS_DEFINER = Lazy.createThreadSafe(() -> {
        try {
            return new UnsafeClassDefiner();
        } catch (final Throwable x) {
            return null;
        }
    });

    /**
     * {@link ClassDefiner class definer} based on {@link Lookup}{@code #defineClass(byte[])}.
     */
    private Lazy<@Nullable ClassDefiner> LOOKUP_CLASS_DEFINER = Lazy.createThreadSafe(() -> {
        try {
            return new LookupClassDefiner();
        } catch (final Throwable x) {
            return null;
        }
    });

    /**
     * {@link ClassDefiner class definer} creating temporary {@link ClassLoader class loaders} for defined classes.
     */
    private Lazy<@Nullable ClassDefiner> TMP_CLASS_LOADER_CLASS_DEFINER = Lazy.createThreadSafe(() -> {
        try {
            return new TmpClassLoaderClassDefiner();
        } catch (final Throwable x) {
            return null;
        }
    });

    /**
     * Default {@link ClassDefiner class definer}
     */
    private Lazy<Optional<ClassDefiner>> DEFAULT_CLASS_DEFINER = Lazy.createThreadSafe(() -> Optional
            .ofNullable(ObjectUtil.nonNull(UNSAFE_CLASS_DEFINER, LOOKUP_CLASS_DEFINER, TMP_CLASS_LOADER_CLASS_DEFINER))
    );

    /**
     * Gets the default {@link ClassDefiner class definer}.
     *
     * @return the default optional {@link ClassDefiner class definer} wrapped
     */
    public Optional<ClassDefiner> getDefault() {
        return DEFAULT_CLASS_DEFINER.get();
    }

    /**
     * {@link ClassDefiner class definer}
     * based on {@code sun.misc.Unsafe#defineAnonymousClass(Class, byte[], Object[])}.
     */
    private static final class UnsafeClassDefiner implements ClassDefiner {

        /**
         * Reference to {@code sun.misc.Unsafe#defineAnonymousClass(Class, byte[], Object[])}.
         */
        private static final AnonymousClassDefiner ANONYMOUS_CLASS_DEFINER;

        static {
            final Class<?> unsafeClass;
            try {
                unsafeClass = Class.forName("sun.misc.Unsafe");
            } catch (final ClassNotFoundException e) {
                throw new IllegalStateException("Cannot find `sun.misc.Unsafe` class", e);
            }

            final Object unsafe;

            try {
                // cannot use `invokeExact` here as the type of field (and thus return type of indy method) ...
                // ... is `Object` while it should be `Unsafe` for exact polymorphic signature
                unsafe = FullAccessLookupFactories.getDefault()
                        .orElseThrow(() -> new IllegalStateException("LookupFactory is unavailable"))
                        .create(unsafeClass)
                        .findStaticGetter(unsafeClass, "theUnsafe", unsafeClass).invoke();
            } catch (final Throwable x) {
                throw new IllegalStateException("Could not get `sun.misc.Unsafe#theUnsafe` field's value", x);
            }

            val methodType = MethodType.methodType(Class.class, Class.class, byte[].class, Object[].class);

            // LookupFactory can't be used as it's associated class-loader doesn't know about `AnonymousClassDefiner`
            val lookup = MethodHandles.lookup();
            try {
                ANONYMOUS_CLASS_DEFINER = (AnonymousClassDefiner) LambdaMetafactory.metafactory(
                        lookup, "defineAnonymousClass",
                        MethodType.methodType(AnonymousClassDefiner.class, unsafeClass),
                        methodType, lookup.findVirtual(unsafeClass, "defineAnonymousClass", methodType), methodType
                ).getTarget().invoke(unsafe);
            } catch (final Throwable x) {
                throw new IllegalStateException(
                        "Could not implement AnonymousClassDefiner "
                                + "via `sun.misc.Unsafe.defineAnonymousClass(Class, byte[], Object)` method"
                );
            }
        }

        @Override
        public Class<?> defineClass(@NonNull final Lookup owner,
                                    @Nullable final String name, @NonNull final byte[] bytecode) {
            return ANONYMOUS_CLASS_DEFINER.defineAnonymousClass(owner.lookupClass(), bytecode, null);
        }

        @Override
        public Class<?>[] defineClasses(final @NonNull Lookup owner, @NonNull final byte[]... bytecodes) {
            val length = bytecodes.length;
            val classes = new Class<?>[length];

            val lookupClass = owner.lookupClass();
            for (var i = 0; i < length; i++) classes[i] = ANONYMOUS_CLASS_DEFINER
                    .defineAnonymousClass(lookupClass, bytecodes[i], null);

            return classes;
        }

        @Override
        public List<Class<?>> defineClasses(final @NonNull Lookup owner,
                                            @NonNull final List<@NotNull byte[]> bytecodes) {
            val classes = new ArrayList<Class<?>>(bytecodes.size());

            val lookupClass = owner.lookupClass();
            for (val bytecode : bytecodes) classes.add(ANONYMOUS_CLASS_DEFINER.defineAnonymousClass(
                    lookupClass, bytecode, null
            ));

            return classes;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<?>[] defineClasses(@NonNull final Lookup owner,
                                        @NonNull final Pair<@Nullable String, @NotNull byte[]>... bytecodes) {
            val length = bytecodes.length;
            val classes = new Class<?>[length];

            val lookupClass = owner.lookupClass();
            for (var i = 0; i < length; i++) classes[i] = ANONYMOUS_CLASS_DEFINER
                    .defineAnonymousClass(lookupClass, bytecodes[i].getSecond(), null);

            return classes;
        }

        @Override
        public Map<String, Class<?>> defineClasses(@NonNull final Lookup owner,
                                                   @NonNull final Map<String, byte[]> namedBytecode) {
            val classes = new HashMap<String, Class<?>>(namedBytecode.size());

            val lookupClass = owner.lookupClass();
            for (val entry : namedBytecode.entrySet()) classes.put(
                    entry.getKey(), ANONYMOUS_CLASS_DEFINER.defineAnonymousClass(lookupClass, entry.getValue(), null)
            );

            return classes;
        }

        /**
         * Functional interface for referencing {@code sun.misc.Unsafe#defineAnonymousClass(Class, byte[], Object[])}.
         */
        @FunctionalInterface
        private interface AnonymousClassDefiner {

            /**
             * Defines an anonymous class.
             *
             * @param parentClass class whose permissions should be inherited
             * @param bytecode bytecode of the defined class
             * @param constantPoolPatches patches to the constant pool where each non-null value is a replacement
             * of the one at the same index in the class's constant pool
             * @return defined anonymous class
             */
            Class<?> defineAnonymousClass(Class<?> parentClass, byte[] bytecode, Object[] constantPoolPatches);
        }
    }

    /**
     * {@link ClassDefiner class definer} based on {@link Lookup}{@code #defineClass(byte[])}.
     */
    private static final class LookupClassDefiner implements ClassDefiner {

        /**
         * Reference to {@link Lookup}{@code #defineClass(byte[])}.
         */
        private static final LookupMethodClassDefiner CLASS_DEFINER;

        static {
            val methodType = MethodType.methodType(Class.class, Lookup.class, byte[].class);

            // LookupFactory can't be used as it's associated class-loader doesn't know about `AnonymousClassDefiner`
            val lookup = MethodHandles.lookup();
            try {
                CLASS_DEFINER = (LookupMethodClassDefiner) LambdaMetafactory.metafactory(
                        lookup, "defineClass", MethodType.methodType(LookupMethodClassDefiner.class), methodType,
                        lookup.findVirtual(
                                Lookup.class, "defineClass",
                                MethodType.methodType(Class.class, byte[].class)
                        ), methodType
                ).getTarget().invokeExact();
            } catch (final Throwable x) {
                throw new IllegalStateException(
                        "Could not implement LookupClassDefiner "
                                + "via `java.lang.invoke.MethodHandles.Lookup.defineClass(byte[])` method"
                );
            }
        }

        @Override
        public Class<?> defineClass(@NonNull final Lookup owner,
                                    @Nullable final String name, @NonNull final byte[] bytecode) {
            return CLASS_DEFINER.defineClass(owner, bytecode);
        }

        @Override
        public Class<?>[] defineClasses(final @NonNull Lookup owner, @NonNull final byte[]... bytecodes) {
            val length = bytecodes.length;
            val classes = new Class<?>[length];

            for (var i = 0; i < length; i++) classes[i] = CLASS_DEFINER.defineClass(owner, bytecodes[i]);

            return classes;
        }

        @Override
        public List<Class<?>> defineClasses(final @NonNull Lookup owner,
                                            @NonNull final List<@NotNull byte[]> bytecodes) {
            val classes = new ArrayList<Class<?>>(bytecodes.size());

            for (val bytecode : bytecodes) classes.add(CLASS_DEFINER.defineClass(owner, bytecode));

            return classes;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<?>[] defineClasses(@NonNull final Lookup owner,
                                        @NonNull final Pair<@Nullable String, @NotNull byte[]>... bytecodes) {
            val length = bytecodes.length;
            val classes = new Class<?>[length];

            for (var i = 0; i < length; i++) classes[i] = CLASS_DEFINER.defineClass(owner, bytecodes[i].getSecond());

            return classes;
        }

        @Override
        public Map<String, Class<?>> defineClasses(@NonNull final Lookup owner,
                                                   @NonNull final Map<String, byte[]> namedBytecode) {
            val classes = new HashMap<String, Class<?>>(namedBytecode.size());

            for (val entry : namedBytecode.entrySet()) classes.put(
                    entry.getKey(), CLASS_DEFINER.defineClass(owner, entry.getValue())
            );

            return classes;
        }

        /**
         * Functional interface for referencing {@link Lookup}{@code #defineClass(byte[])}
         */
        private interface LookupMethodClassDefiner {

            /**
             * Defines a class via the given {@link Lookup lookup}.
             *
             * @param lookup lookup to use for class definition
             * @param bytecode bytecode of the class
             * @return defined class
             */
            Class<?> defineClass(Lookup lookup, byte[] bytecode);
        }
    }

    /**
     * {@link ClassDefiner class definer} creating temporary {@link ClassLoader class loaders} for defined classes.
     */
    private static final class TmpClassLoaderClassDefiner implements ClassDefiner {

        @Override
        public Class<?> defineClass(@NonNull final Lookup owner,
                                    @Nullable final String name, @NonNull final byte[] bytecode) {
            return new TmpClassLoader().define(name, bytecode);
        }

        @Override
        public Class<?>[] defineClasses(final @NonNull Lookup owner, @NonNull final byte[]... bytecodes) {
            val classLoader = new TmpClassLoader();

            val length = bytecodes.length;
            val classes = new Class<?>[length];
            for (var i = 0; i < length; i++) classes[i] = classLoader.define(null, bytecodes[i]);

            return classes;
        }

        @Override
        public List<Class<?>> defineClasses(final @NonNull Lookup owner,
                                            @NonNull final List<@NotNull byte[]> bytecodes) {
            val classLoader = new TmpClassLoader();

            val classes = new ArrayList<Class<?>>(bytecodes.size());
            for (val bytecode : bytecodes) classes.add(classLoader.define(null, bytecode));

            return classes;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<?>[] defineClasses(@NonNull final Lookup owner,
                                        @NonNull final Pair<@Nullable String, @NotNull byte[]>... bytecodes) {
            val classLoader = new TmpClassLoader();

            val length = bytecodes.length;
            val classes = new Class<?>[length];
            for (var i = 0; i < length; i++) {
                val namedClass = bytecodes[i];
                classes[i] = classLoader.define(namedClass.getFirst(), namedClass.getSecond());
            }

            return classes;
        }

        @Override
        public Map<String, Class<?>> defineClasses(@NonNull final Lookup owner,
                                                   @NonNull final Map<String, byte[]> namedBytecode) {
            val classLoader = new TmpClassLoader();

            val classes = new HashMap<String, Class<?>>(namedBytecode.size());
            for (val entry : namedBytecode.entrySet()) {
                val name = entry.getKey();
                classes.put(name, classLoader.define(name, entry.getValue()));
            }

            return classes;
        }

        /**
         * Temporary class loader which should be instantiated for groups of related classes which may be unloaded.
         */
        @Internal(
                "This class loader is intended only for internal usage and should never be accessed outside "
                        + "as there should be no strong references to it"
        )
        private static class TmpClassLoader extends ClassLoader {

            /**
             * Defines a class by the given bytecode.
             *
             * @param name canonical name of the class
             * @param bytecode bytecode of the class
             * @return defined class
             */
            private Class<?> define(@Nullable final String name,
                                    @Internal("no need for check as the class is only locally available")
                                    @NotNull final byte[] bytecode) {
                return defineClass(name, bytecode, 0, bytecode.length);
            }
        }
    }
}
