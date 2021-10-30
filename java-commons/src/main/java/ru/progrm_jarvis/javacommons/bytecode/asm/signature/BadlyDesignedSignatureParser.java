package ru.progrm_jarvis.javacommons.bytecode.asm.signature;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.Optional;

public abstract class BadlyDesignedSignatureParser<R> extends SignatureVisitor {

    /**
     * Constructs a new signature parser.
     *
     * @param api the ASM API version implemented by this visitor
     */
    protected BadlyDesignedSignatureParser(final int api) {
        super(api);
    }

    public abstract @NotNull Optional<R> result();
}
