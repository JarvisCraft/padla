package ru.progrm_jarvis.javacommons.bytecode.asm.signature;

import org.jetbrains.annotations.NotNull;

/**
 * Either {@link ClassSignature}, {@link  MethodSignature} or {@link TypeSignature}.
 */
public interface Signature {

    @NotNull SignatureKind signatureKind();

    @NotNull ClassSignature asClass();

    @NotNull MethodSignature asMethod();

    @NotNull TypeSignature asType();

    enum SignatureKind {
        CLASS,
        METHOD,
        TYPE,
    }
}
