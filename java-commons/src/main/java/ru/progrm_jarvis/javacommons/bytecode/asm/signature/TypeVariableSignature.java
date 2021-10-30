package ru.progrm_jarvis.javacommons.bytecode.asm.signature;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

public interface TypeVariableSignature extends TypeSignature {

    @NotNull String formalName();

    static @NotNull TypeVariableSignature of(final @NonNull String formalName) {
        return new SimpleTypeVariableSignature(formalName);
    }

    @Override
    default @NotNull TypeSignatureKind typeSignatureKind() {
        return TypeSignatureKind.TYPE_VARIABLE;
    }

    @Value
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class SimpleTypeVariableSignature implements TypeVariableSignature {
        @NotNull String formalName;
    }
}
