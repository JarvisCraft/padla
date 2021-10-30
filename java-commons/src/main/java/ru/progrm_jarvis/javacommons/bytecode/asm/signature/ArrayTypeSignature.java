package ru.progrm_jarvis.javacommons.bytecode.asm.signature;

import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

public interface ArrayTypeSignature extends TypeSignature {

    @NotNull TypeSignature elementType();

    static @NotNull ArrayTypeSignature of(final @NonNull TypeSignature elementType) {
        return new SimpleArrayTypeSignature(elementType);
    }

    static TypeSignature wrap(/* will get wrapped */ @NonNull TypeSignature typeSignature, final int depth) {
        if (depth < 0) throw new IllegalArgumentException("depth should be non-negative");

        for (var level = 0; level < depth; level++) typeSignature = new SimpleArrayTypeSignature(typeSignature);

        return typeSignature;
    }

    @Override
    default @NotNull TypeSignatureKind typeSignatureKind() {
        return TypeSignatureKind.ARRAY;
    }

    @Value
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class SimpleArrayTypeSignature implements ArrayTypeSignature {
        @NotNull TypeSignature elementType;
    }
}
