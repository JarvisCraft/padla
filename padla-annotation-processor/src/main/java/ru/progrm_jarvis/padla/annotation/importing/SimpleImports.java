package ru.progrm_jarvis.padla.annotation.importing;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import ru.progrm_jarvis.padla.annotation.util.ClassNames;
import ru.progrm_jarvis.padla.annotation.util.JavaSourceParts;

import java.util.*;

/**
 * Simple implementation of {@link Imports}.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleImports implements Imports {

    /**
     * Full name of the class to which the imports belong
     */
    private final @NotNull String fullName;

    /**
     * Package name of the class to which the imports belong
     */
    private final @NotNull String packageName;

    /**
     * Simple name of the class to which the imports belong
     */
    private final @NotNull String simpleName;

    /**
     * Simple names of imported classes including the name of the class itself.
     */
    private final @NotNull Set<@NotNull String> simpleNames;

    /**
     * Imported classes.
     */
    private final @NotNull Set<@NotNull String> fullNames;

    /**
     * Unmodifiable view of {@link #fullNames}
     */
    private final @NotNull @UnmodifiableView Set<@NotNull String> fullNamesView;

    /**
     * Creates simple {@link Imports} for the provided class.
     *
     * @param packageName package name of the class for which the imports are computed
     * @param simpleName simple name of the class for which the imports are computed
     *
     * @return imports for the provided class
     */
    public static @NotNull Imports create(final @NonNull String packageName,
                                          final @NotNull String simpleName) {
        return create(packageName, simpleName, new TreeSet<>());
    }

    /**
     * Creates simple {@link Imports} for the provided class.
     *
     * @param packageName package name of the class for which the imports are computed
     * @param simpleName simple name of the class for which the imports are computed
     * @param comparator comparator used to order imports
     *
     * @return imports for the provided class
     */
    public static @NotNull Imports create(final @NonNull String packageName,
                                          final @NotNull String simpleName,
                                          final @NotNull Comparator<? super String> comparator) {
        return create(packageName, simpleName, new TreeSet<>(comparator));
    }

    /**
     * Creates simple {@link Imports} for the provided class.
     *
     * @param packageName package name of the class for which the imports are computed
     * @param simpleName simple name of the class for which the imports are computed
     * @param fullNames set to be used for storing full names
     *
     * @return imports for the provided class
     */
    private static @NotNull Imports create(final @NonNull String packageName,
                                           final @NotNull String simpleName,
                                           final @NotNull Set<String> fullNames){
        final Set<String> simpleNames;
        (simpleNames = new HashSet<>()).add(simpleName);

        return new SimpleImports(
                ClassNames.toFullClassName(packageName, simpleName), packageName, simpleName,
                simpleNames,
                fullNames, Collections.unmodifiableSet(fullNames)
        );
    }

    @Override
    public @NotNull @Unmodifiable Set<@NotNull String> imports() {
        return fullNamesView;
    }

    @Override
    public @NotNull String importClass(final @NonNull String packageName,
                                       final @NonNull String simpleName) {
        return importClassUnchecked(ClassNames.toFullClassName(packageName, simpleName), packageName, simpleName);
    }

    @Override
    public @NotNull String importClass(final @NonNull String fullName) {
        final int delimiterIndex;
        if ((delimiterIndex = fullName.lastIndexOf('.')) == 0) throw new IllegalArgumentException(
                "Class name cannot start with `.`"
        );

        return importClassUnchecked(
                fullName,
                delimiterIndex > 0 ? fullName.substring(0, delimiterIndex) : "",
                fullName.substring(delimiterIndex + 1)
        );
    }

    private @NotNull String importClassUnchecked(final @NotNull String fullName,
                                                 final @NotNull String packageName,
                                                 final @NotNull String simpleName) {
        assert fullName.endsWith(simpleName)
                : "`fullName` (" + fullName + ") should end with `simpleName` (" + simpleName + ')';

        // no need to import itself
        if (fullName.equals(this.fullName)) return simpleName;

        // the class may already be imported
        // note: can't add to `fullNames` yet because the import may be impossible due to simple names' collision
        final Set<String> fullNames;
        if ((fullNames = this.fullNames).contains(fullName)) return simpleName;

        if (simpleNames.add(simpleName)) {
            // only import the class if there is no existing import for the same simple name

            // don't import imported-by-default
            if (!packageName.equals(this.packageName) && !packageName.equals(JavaSourceParts.PackageNames.JAVA_LANG)) {
                val newlyImported = fullNames.add(fullName);
                assert newlyImported : "value should be newly imported";
            }

            return simpleName;
        }

        return fullName;
    }
}
