package ru.progrm_jarvis.javacommons.bytecode.asm.signature;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Type;
import ru.progrm_jarvis.javacommons.ownership.annotation.Own;
import ru.progrm_jarvis.javacommons.ownership.annotation.Ref;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface GenericTypeSignature extends TypeSignature {

    @NotNull GenericTypeSignature @NotNull [] EMPTY_GENERIC_TYPE_SIGNATURE_ARRAY = new GenericTypeSignature[0];

    @NotNull Bound bound();

    @NotNull Type type();

    @NotNull @Unmodifiable List<? extends @NotNull TypeSignature> typeArguments();

    @Override
    default @NotNull TypeSignatureKind typeSignatureKind() {
        return TypeSignatureKind.GENERIC;
    }

    static @NotNull GenericTypeSignature of(
            final @NonNull Bound bound,
            final @NonNull Type type,
            final @NonNull @Own List<? extends @NotNull TypeSignature> typeArguments
    ) {
        return new SimpleGenericTypeSignature(bound, type, Collections.unmodifiableList(typeArguments));
    }

    static @NotNull GenericTypeSignature ofCopy(
            final @NonNull Bound bound,
            final @NonNull Type type,
            final @NonNull @Ref List<? extends @NotNull TypeSignature> typeArguments
    ) {
        return of(bound, type, Arrays.asList(typeArguments.toArray(EMPTY_TYPE_SIGNATURE_ARRAY)));
    }

    enum Bound {
        NONE,
        SUPER,
        EXTENDS
    }

    @Value
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class SimpleGenericTypeSignature implements GenericTypeSignature {
        @NotNull Bound bound;
        @NotNull Type type;
        @NotNull @Unmodifiable List<? extends @NotNull TypeSignature> typeArguments;
    }
}
