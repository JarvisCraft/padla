package ru.progrm_jarvis.javacommons.classloading;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.javacommons.annotation.Internal;
import ru.progrm_jarvis.javacommons.classloading.extension.LegacyClassExtensions;
import ru.progrm_jarvis.javacommons.invoke.FullAccessLookupFactories;
import ru.progrm_jarvis.javacommons.object.Pair;
import ru.progrm_jarvis.javacommons.unsafe.UnsafeInternals;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.invoke.MethodType.methodType;

/**
 * Utility for accessing {@link ClassDefiner class definers} capable od defining garbage-collected classes.
 */
@UtilityClass
@ExtensionMethod(LegacyClassExtensions.class)
public class GcClassDefiners {

    private static final @NotNull ClassDefiner CLASS_DEFINER;

    static {
        ClassDefiner classDefiner;
        try {
            // by default, use stable API
            classDefiner = new HiddenClassDefiner();
        } catch (final Throwable x1) {
            try {
                // try falling back to Unsafe
                classDefiner = new UnsafeClassDefiner();
            } catch (final Throwable x2) {
                // finally use the worst but available approach
                classDefiner = new TmpClassLoaderClassDefiner();
            }
        }

        CLASS_DEFINER = classDefiner;
    }

    /**
     * Gets the default {@link ClassDefiner class definer}.
     *
     * @return the default optional {@link ClassDefiner class definer} wrapped
     */
    public @NotNull ClassDefiner getDefault() {
        return CLASS_DEFINER;
    }

    private static final class HiddenClassDefiner implements ClassDefiner {

        /**
         * Method handle referring to
         * {@link Lookup}{@code .defineHiddenClass(byte[], boolean, Lookup.ClassOption)} method
         */
        private static final MethodHandle LOOKUP__DEFINE_HIDDEN_CLASS__METHOD_HANDLE;

        static {
            final Class<?> lookupClassOptionClass;
            try {
                lookupClassOptionClass = Class.forName("java.lang.invoke.MethodHandles$Lookup$ClassOption");
            } catch (final ClassNotFoundException e) {
                throw new Error("HiddenClassDefiner is unavailable: JRE is older than 15", e);
            }

            val lookup = MethodHandles.publicLookup();

            final MethodHandle methodHandle;
            {
                val methodType = methodType(
                        Lookup.class, byte[].class, boolean.class, lookupClassOptionClass.arrayType()
                );
                try {
                    methodHandle = lookup.findVirtual(Lookup.class, "defineHiddenClass", methodType);
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    throw new Error("HiddenClassDefiner is unavailable: JRE is older than 15", e);
                }
            }

            LOOKUP__DEFINE_HIDDEN_CLASS__METHOD_HANDLE = MethodHandles.insertArguments(methodHandle,
                    1 /* virtual method thus `0` is for `this` */ + 2 /* 3rd argument */,
                    uncheckedEnumValueOf(lookupClassOptionClass, "NESTMATE")
            );
        }

        @SuppressWarnings("unchecked")
        private static <E extends Enum<E>> @NotNull E uncheckedEnumValueOf(final @NotNull Class<?> type,
                                                                           final @NotNull String constantName) {
            return Enum.valueOf((Class<E>) type, constantName);
        }

        @SneakyThrows // call to `MethodHandle#invokeExact(...)`
        private static Class<?> defineHiddenClass(final @NotNull Lookup owner,
                                                  final byte @NotNull [] bytecode) {
            return (Class<?>) LOOKUP__DEFINE_HIDDEN_CLASS__METHOD_HANDLE.invokeExact(owner, bytecode);
        }

        @Override
        public Class<?> defineClass(
                final @NonNull Lookup owner,
                final @Nullable String name,
                final byte @NonNull [] bytecode
        ) {
            return defineHiddenClass(owner, bytecode);
        }

        @Override
        public Class<?>[] defineClasses(
                final @NonNull Lookup owner,
                final byte @NotNull [] @NonNull ... bytecodes
        ) {
            val length = bytecodes.length;
            val classes = new Class<?>[length];

            for (var i = 0; i < length; i++) classes[i] = defineHiddenClass(owner, bytecodes[i]);

            return classes;
        }

        @Override
        public List<Class<?>> defineClasses(
                final @NonNull Lookup owner,
                final @NonNull List<byte @NotNull []> bytecodes
        ) {
            val classes = new ArrayList<Class<?>>(bytecodes.size());

            for (val bytecode : bytecodes) classes.add(defineHiddenClass(owner, bytecode));

            return classes;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<?>[] defineClasses(
                final @NonNull Lookup owner,
                final @NonNull Pair<@Nullable String, byte @NotNull []>... bytecodes
        ) {
            val length = bytecodes.length;
            val classes = new Class<?>[length];

            for (var i = 0; i < length; i++) classes[i] = defineHiddenClass(owner, bytecodes[i].getSecond());

            return classes;
        }

        @Override
        public Map<String, Class<?>> defineClasses(
                final @NonNull Lookup owner,
                final @NonNull Map<String, byte @NotNull []> namedBytecode
        ) {
            val classes = new HashMap<String, Class<?>>(namedBytecode.size());

            for (val entry : namedBytecode.entrySet()) classes.put(
                    entry.getKey(),
                    defineHiddenClass(owner, entry.getValue())
            );

            return classes;
        }
    }

    /**
     * {@link ClassDefiner class definer}
     * based on {@code sun.misc.Unsafe#defineAnonymousClass(Class, byte[], Object[])}.
     */
    private static final class UnsafeClassDefiner implements ClassDefiner {

        /**
         * Method handle referencing {@code sun.misc.Unsafe#defineAnonymousClass(Class, byte[], Object[])}.
         */
        private static final @NotNull MethodHandle UNSAFE__DEFINE_ANONYMOUS_CLASS__METHOD_HANDLE;

        static {
            final Class<?> unsafeClass;
            if ((unsafeClass = UnsafeInternals.UNSAFE_CLASS) == null) throw new Error("No Unsafe is available");
            final Object unsafe;

            {
                final MethodHandle theUnsafeGetter;
                {
                    val lookup = FullAccessLookupFactories.getDefault()
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

            val methodType = methodType(Class.class, Class.class, byte[].class, Object[].class);

            // LookupFactory can't be used as it's associated class-loader doesn't know about `AnonymousClassDefiner`

            val lookup = MethodHandles.lookup();
            final MethodHandle methodHandle;
            try {
                methodHandle = lookup
                        .findVirtual(unsafeClass, "defineAnonymousClass", methodType);
            } catch (final NoSuchMethodException | IllegalAccessException e) {
                throw new Error(
                        "Method " + unsafeClass.getName() + ".defineAnonymousClass(Class, byte[], Object[])` "
                                + "cannot be found", e
                );
            }
            UNSAFE__DEFINE_ANONYMOUS_CLASS__METHOD_HANDLE = MethodHandles.insertArguments(
                    methodHandle.bindTo(unsafe),
                    1 /* virtual method thus `0` is for `this` */ + 2 /* 3rd argument */,
                    (Object) null // no const-pool patches
            );
        }

        @SneakyThrows // call to `MethodHandle#invokeExact(...)`
        private static Class<?> defineAnonymousClass(final @NotNull Class<?> owner,
                                                     final byte @NotNull [] bytecode) {
            return (Class<?>) UNSAFE__DEFINE_ANONYMOUS_CLASS__METHOD_HANDLE.invokeExact(
                    owner, bytecode
            );
        }

        @Override
        public Class<?> defineClass(
                final @NonNull Lookup owner,
                final @Nullable String name,
                final byte @NonNull [] bytecode
        ) {
            return defineAnonymousClass(owner.lookupClass(), bytecode);
        }

        @Override
        public Class<?>[] defineClasses(
                final @NonNull Lookup owner,
                final byte @NotNull [] @NonNull ... bytecodes
        ) {
            val length = bytecodes.length;
            val classes = new Class<?>[length];

            val lookupClass = owner.lookupClass();
            for (var i = 0; i < length; i++) classes[i] = defineAnonymousClass(lookupClass, bytecodes[i]);

            return classes;
        }

        @Override
        public List<Class<?>> defineClasses(
                final @NonNull Lookup owner,
                final @NonNull List<byte @NotNull []> bytecodes
        ) {
            val classes = new ArrayList<Class<?>>(bytecodes.size());

            val lookupClass = owner.lookupClass();
            for (val bytecode : bytecodes) classes.add(defineAnonymousClass(lookupClass, bytecode));

            return classes;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<?>[] defineClasses(
                final @NonNull Lookup owner,
                final @NonNull Pair<@Nullable String, byte @NotNull []>... bytecodes
        ) {
            val length = bytecodes.length;
            val classes = new Class<?>[length];

            val lookupClass = owner.lookupClass();
            for (var i = 0; i < length; i++) classes[i] = defineAnonymousClass(lookupClass, bytecodes[i].getSecond());

            return classes;
        }

        @Override
        public Map<String, Class<?>> defineClasses(
                final @NonNull Lookup owner,
                final @NonNull Map<String, byte @NotNull []> namedBytecode
        ) {
            val classes = new HashMap<String, Class<?>>(namedBytecode.size());

            val lookupClass = owner.lookupClass();
            for (val entry : namedBytecode.entrySet()) classes.put(
                    entry.getKey(),
                    defineAnonymousClass(lookupClass, entry.getValue())
            );

            return classes;
        }
    }

    /**
     * {@link ClassDefiner Class definer} creating temporary {@link ClassLoader class-loaders} for defined classes.
     */
    private static final class TmpClassLoaderClassDefiner implements ClassDefiner {

        @Override
        public Class<?> defineClass(
                final @NonNull Lookup owner,
                final @Nullable String name,
                final byte @NonNull [] bytecode
        ) {
            return new TmpClassLoader(owner.lookupClass().getClassLoader()).define(name, bytecode);
        }

        @Override
        public Class<?>[] defineClasses(
                final @NonNull Lookup owner,
                final byte @NotNull [] @NonNull ... bytecodes
        ) {
            val classLoader = new TmpClassLoader(owner.lookupClass().getClassLoader());

            val length = bytecodes.length;
            val classes = new Class<?>[length];
            for (var i = 0; i < length; i++) classes[i] = classLoader.define(null, bytecodes[i]);

            return classes;
        }

        @Override
        public List<Class<?>> defineClasses(
                final @NonNull Lookup owner,
                final @NonNull List<byte @NotNull []> bytecodes
        ) {
            val classLoader = new TmpClassLoader(owner.lookupClass().getClassLoader());

            val classes = new ArrayList<Class<?>>(bytecodes.size());
            for (val bytecode : bytecodes) classes.add(classLoader.define(null, bytecode));

            return classes;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<?>[] defineClasses(
                final @NonNull Lookup owner,
                final @NotNull Pair<@Nullable String, byte @NotNull []> @NonNull ... bytecodes
        ) {
            val classLoader = new TmpClassLoader(owner.lookupClass().getClassLoader());

            val length = bytecodes.length;
            val classes = new Class<?>[length];
            for (var i = 0; i < length; i++) {
                val namedClass = bytecodes[i];
                classes[i] = classLoader.define(namedClass.getFirst(), namedClass.getSecond());
            }

            return classes;
        }

        @Override
        public Map<String, Class<?>> defineClasses(
                final @NonNull Lookup owner,
                final @NonNull Map<@Nullable String, byte @NotNull []> namedBytecode
        ) {
            val classLoader = new TmpClassLoader(owner.lookupClass().getClassLoader());

            val classes = new HashMap<String, Class<?>>(namedBytecode.size());
            for (val entry : namedBytecode.entrySet()) {
                val name = entry.getKey();
                classes.put(name, classLoader.define(name, entry.getValue()));
            }

            return classes;
        }

        /**
         * Temporary class-loader which should be instantiated for groups of related classes which may be unloaded.
         */
        @Internal(
                "This class-loader is intended only for internal usage and should never be accessed outside "
                        + "as there should be no strong references to it"
        )
        private static final class TmpClassLoader extends ClassLoader {

            /**
             * Instantiates a new temporary class-loader using the given parent.
             *
             * @param parent parent class-loader
             */
            private TmpClassLoader(final ClassLoader parent) {
                super(parent);
            }

            /**
             * Defines a class by the given bytecode.
             *
             * @param name canonical name of the class
             * @param bytecode bytecode of the class
             * @return defined class
             */
            private Class<?> define(final @Nullable String name,
                                    final byte @NotNull [] bytecode) {
                return defineClass(name, bytecode, 0, bytecode.length);
            }
        }
    }
}
