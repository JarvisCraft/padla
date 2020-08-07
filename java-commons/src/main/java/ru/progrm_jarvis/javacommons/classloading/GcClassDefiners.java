package ru.progrm_jarvis.javacommons.classloading;

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
import ru.progrm_jarvis.javacommons.unsafe.UnsafeInternals;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.*;

/**
 * Utility for accessing {@link ClassDefiner class definers} capable od defining garbage-collected classes.
 */
@UtilityClass
public class GcClassDefiners {

    /**
     * {@link ClassDefiner class definer}
     * based on {@code Unsafe#defineAnonymousClass(Class, byte[], Object[])}.
     */
    private final Lazy<@Nullable ClassDefiner> UNSAFE_CLASS_DEFINER = Lazy.createThreadSafe(() -> {
        try {
            return new UnsafeClassDefiner();
        } catch (final Throwable x) {
            return null;
        }
    });

    /**
     * {@link ClassDefiner class definer} based on {@link Lookup}{@code #defineClass(byte[])}.
     */
    private final Lazy<@Nullable ClassDefiner> LOOKUP_CLASS_DEFINER = Lazy.createThreadSafe(() -> {
        try {
            return new LookupClassDefiner();
        } catch (final Throwable x) {
            return null;
        }
    });

    /**
     * {@link ClassDefiner class definer} creating temporary {@link ClassLoader class loaders} for defined classes.
     */
    private final Lazy<@Nullable ClassDefiner> TMP_CLASS_LOADER_CLASS_DEFINER = Lazy.createThreadSafe(() -> {
        try {
            return new TmpClassLoaderClassDefiner();
        } catch (final Throwable x) {
            return null;
        }
    });

    /**
     * Default {@link ClassDefiner class definer}
     */
    private final Lazy<Optional<ClassDefiner>> DEFAULT_CLASS_DEFINER = Lazy.createThreadSafe(() -> Optional
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
    private final class UnsafeClassDefiner implements ClassDefiner {

        /**
         * Reference to {@code sun.misc.Unsafe#defineAnonymousClass(Class, byte[], Object[])}.
         */
        private static final AnonymousClassDefiner ANONYMOUS_CLASS_DEFINER;

        static {
            final Class<?> unsafeClass;
            if ((unsafeClass = UnsafeInternals.UNSAFE_CLASS) == null) throw new Error("No Unsafe is available");
            final Object unsafe;

            {
                final MethodHandle theUnsafeGetter;
                {
                    val lookup  = FullAccessLookupFactories.getDefault()
                            .orElseThrow(() -> new IllegalStateException("LookupFactory is unavailable"))
                            .create(unsafeClass);
                    try {
                        theUnsafeGetter = lookup.findStaticGetter(unsafeClass, "theUnsafe", unsafeClass);
                    } catch (final IllegalAccessException | NoSuchFieldException e) {
                        throw new Error(
                                '`' + unsafeClass.getName() + ".theUnsafe` field's getter cannot be found", e
                        );
                    }
                }
                try {
                    unsafe = theUnsafeGetter.invoke();
                } catch (final Throwable x) {
                    throw new Error("Could not get value of field `" + unsafeClass.getName() + "`", x);
                }
            }

            val methodType = MethodType.methodType(Class.class, Class.class, byte[].class, Object[].class);

            // LookupFactory can't be used as it's associated class-loader doesn't know about `AnonymousClassDefiner`

            final MethodHandle defineAnonymousClassMethodHandle;
            {
                val lookup = MethodHandles.lookup();
                final MethodHandle methodHandle;
                try {
                    methodHandle = lookup.findVirtual(unsafeClass, "defineAnonymousClass", methodType);
                } catch (final NoSuchMethodException | IllegalAccessException e) {
                    throw new Error(
                            "Method " + unsafeClass.getName() + ".defineAnonymousClass(Class, byte[], Object[])` "
                                    + "cannot be found", e
                    );
                }
                final CallSite callSIte;
                try {
                    callSIte = LambdaMetafactory.metafactory(
                            lookup, "defineAnonymousClass",
                            MethodType.methodType(AnonymousClassDefiner.class, unsafeClass),
                            methodType, methodHandle, methodType
                    );
                } catch (final LambdaConversionException e) {
                    throw new Error(
                            "Cannot create lambda call-site for method `" + unsafeClass.getName()
                                    + ".defineAnonymousClass(Class, byte[], Object[])`", e
                    );
                }

                defineAnonymousClassMethodHandle = callSIte.getTarget();
            }

            try {
                ANONYMOUS_CLASS_DEFINER = (AnonymousClassDefiner) defineAnonymousClassMethodHandle.invoke(unsafe);
            } catch (final Throwable x) {
                throw new IllegalStateException(
                        "Could not implement AnonymousClassDefiner via method `" + unsafeClass.getName()
                                + ".defineAnonymousClass(Class, byte[], Object)`", x
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
                                            @NonNull final List<byte @NotNull []> bytecodes) {
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
                                        @NonNull final Pair<@Nullable String, byte @NotNull []>... bytecodes) {
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
    private final class LookupClassDefiner implements ClassDefiner {

        /**
         * Reference to {@link Lookup}{@code #defineClass(byte[])}.
         */
        private static final LookupMethodClassDefiner CLASS_DEFINER;

        static {
            final MethodHandle defineClassMethodHandle;
            {
                val methodType = MethodType.methodType(Class.class, Lookup.class, byte[].class);

                // LookupFactory can't be used as it's associated class-loader doesn't know about `AnonymousClassDefiner`
                val lookup = MethodHandles.lookup();
                final MethodHandle methodHandle;
                try {
                    methodHandle = lookup.findVirtual(
                            Lookup.class, "defineClass",
                            MethodType.methodType(Class.class, byte[].class)
                    );
                } catch (final NoSuchMethodException | IllegalAccessException e) {
                    throw new Error(
                            "Method `" + Lookup.class.getName() + ".defineClass(byte[])` method cannot be found", e
                    );
                }

                final CallSite callSite;
                try {
                    callSite = LambdaMetafactory.metafactory(
                            lookup, "defineClass", MethodType.methodType(LookupMethodClassDefiner.class),
                            methodType, methodHandle, methodType
                    );
                } catch (final LambdaConversionException e) {
                    throw new Error(
                            "Cannot create lambda call-site for `" + Lookup.class.getName()
                                    + ".defineClass(byte[])` method", e
                    );
                }
                defineClassMethodHandle = callSite.getTarget();
            }

            try {
                CLASS_DEFINER = (LookupMethodClassDefiner) defineClassMethodHandle.invokeExact();
            } catch (final Throwable x) {
                throw new Error(
                        "Could not implement LookupClassDefiner via method `" + Lookup.class
                                + ".defineClass(byte[])` method", x
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
                                            @NonNull final List<byte @NotNull []> bytecodes) {
            val classes = new ArrayList<Class<?>>(bytecodes.size());

            for (val bytecode : bytecodes) classes.add(CLASS_DEFINER.defineClass(owner, bytecode));

            return classes;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<?>[] defineClasses(@NonNull final Lookup owner,
                                        @NonNull final Pair<@Nullable String, byte @NotNull []>... bytecodes) {
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
        @FunctionalInterface
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
    private final class TmpClassLoaderClassDefiner implements ClassDefiner {

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
                                            @NonNull final List<byte @NotNull []> bytecodes) {
            val classLoader = new TmpClassLoader();

            val classes = new ArrayList<Class<?>>(bytecodes.size());
            for (val bytecode : bytecodes) classes.add(classLoader.define(null, bytecode));

            return classes;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<?>[] defineClasses(@NonNull final Lookup owner,
                                        @NonNull final Pair<@Nullable String, byte @NotNull []>... bytecodes) {
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
