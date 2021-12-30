package ru.progrm_jarvis.padla.annotation.importing;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import ru.progrm_jarvis.padla.annotation.util.ClassNames;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Simple implementation of {@link Imports}.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleImports implements Imports {

    /**
     * Full name of the class to which the imports belong.
     */
    private final String fullClassName;

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
     * @return imports for the provided class
     */
    public static @NotNull Imports create(final @NonNull String packageName,
                                          final @NotNull String simpleName) {
        final Set<String> simpleNames;
        (simpleNames = new HashSet<>()).add(simpleName);

        final Set<String> fullNames;
        return new SimpleImports(
                ClassNames.toFullClassName(packageName, simpleName),
                simpleNames,
                fullNames = new TreeSet<>(),
                Collections.unmodifiableSet(fullNames)
        );
    }

    @Override
    public @NotNull @Unmodifiable Set<@NotNull String> imports() {
        return fullNamesView;
    }

    @Override
    public @NotNull String importClass(final @NonNull String packageName,
                                       final @NonNull String simpleName) {
        return importClassUnchecked(ClassNames.toFullClassName(packageName, simpleName), simpleName);
    }

    @Override
    public @NotNull String importClass(final @NonNull String fullName) {
        final int delimiterIndex;
        if ((delimiterIndex = fullName.lastIndexOf('.')) == 0) throw new IllegalArgumentException(
                "Class name cannot start with `.`"
        );

        return importClassUnchecked(fullName, fullName.substring(delimiterIndex + 1));
    }

    private @NotNull String importClassUnchecked(final @NotNull String fullName,
                                                 final @NotNull String simpleName) {
        assert fullName.endsWith(simpleName)
                : "`fullName` (" + fullName + ") should end with `simpleName` (" + simpleName + ')';

        // no need to import itself
        if (fullName.equals(fullClassName)) return simpleName;

        // the class may already be imported
        // note: can't add to `fullNames` yet because the import may be impossible due to simple names' collision
        final Set<String> fullNames;
        if ((fullNames = this.fullNames).contains(fullName)) return simpleName;

        if (simpleNames.add(simpleName)) {
            // only import the class if there is no existing import for the same simple name
            val newlyImported = fullNames.add(fullName);
            assert newlyImported : "value should be newly imported";

            return simpleName;
        }

        return fullName;
    }
}
