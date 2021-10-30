package ru.progrm_jarvis.javacommons.bytecode.asm.signature;

import org.jetbrains.annotations.NotNull;

public enum BaseTypeSignature implements TypeSignature {
    BOOLEAN,
    BYTE,
    CHAR,
    SHORT,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    VOID;

    @Override
    public @NotNull TypeSignatureKind typeSignatureKind() {
        return TypeSignatureKind.BASE_TYPE;
    }
}
