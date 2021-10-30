package ru.progrm_jarvis.javacommons.bytecode.asm.signature;

import org.jetbrains.annotations.NotNull;

/**
 * Either {@link BaseTypeSignature}, {@link TypeVariableSignature},
 * {@link ArrayTypeSignature} or {@link  GenericTypeSignature}.
 */
public interface TypeSignature extends Signature {

    @NotNull TypeSignature @NotNull [] EMPTY_TYPE_SIGNATURE_ARRAY = new TypeSignature[0];

    @NotNull TypeSignatureKind typeSignatureKind();

    //<editor-fold desc="Signature sealing" defaultstate="collapsed">

    @Override
    default Signature.@NotNull SignatureKind signatureKind() {
        return SignatureKind.TYPE;
    }

    @Override
    default @NotNull ClassSignature asClass() {
        throw new UnsupportedOperationException("This is a TypeSignature");
    }

    @Override
    default @NotNull MethodSignature asMethod() {
        throw new UnsupportedOperationException("This is a TypeSignature");
    }

    @Override
    default @NotNull TypeSignature asType() {
        return this;
    }

    //</editor-fold>

    enum TypeSignatureKind {
        BASE_TYPE,
        TYPE_VARIABLE,
        ARRAY,
        GENERIC,
    }
}
