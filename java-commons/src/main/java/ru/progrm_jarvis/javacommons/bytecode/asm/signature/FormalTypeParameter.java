package ru.progrm_jarvis.javacommons.bytecode.asm.signature;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static ru.progrm_jarvis.javacommons.bytecode.asm.signature.TypeSignature.EMPTY_TYPE_SIGNATURE_ARRAY;

public interface FormalTypeParameter {

    @NotNull FormalTypeParameter @NotNull[] EMPTY_FORMAL_TYPE_PARAMETER_ARRAY = new FormalTypeParameter[0];

    static FormalTypeParameter of(final @NonNull String formalName,
                                  final @NonNull TypeSignature classBound,
                                  final @NonNull @Unmodifiable List<? extends @NotNull TypeSignature> interfaceBounds) {
        return new SimpleFormalTypeParameter(formalName, classBound, interfaceBounds);
    }

    static FormalTypeParameter ofCopy(final @NonNull String formalName,
                                      final @NonNull TypeSignature classBound,
                                      final @NonNull @Unmodifiable List<? extends @NotNull TypeSignature> interfaceBounds) {
        return new SimpleFormalTypeParameter(formalName,classBound, Collections.unmodifiableList(
                Arrays.asList(interfaceBounds.toArray(EMPTY_TYPE_SIGNATURE_ARRAY))
        ));
    }

    @NotNull String formalName();

    @NotNull TypeSignature classBound();

    @NotNull @Unmodifiable List<? extends @NotNull TypeSignature> interfaceBounds();

    @Value
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class SimpleFormalTypeParameter implements FormalTypeParameter {
        @NotNull String formalName;
        @NotNull TypeSignature classBound;
        @NotNull @Unmodifiable List<? extends @NotNull TypeSignature> interfaceBounds;
    }
}
