package ru.progrm_jarvis.javacommons.bytecode.asm.signature;

import lombok.AccessLevel;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * {@link SignatureVisitor} which delegates all {@link SignatureVisitor} methods to the provided signature visitor.
 * This is to be identical in behaviour with other <b>ASM</b>'s visitors.
 *
 * @implNote this is kept up-to-date by using <b>Lombok</b>'s {@link Delegate}
 * on the field holding the signature visitor, thus any changes to the <b>ASM</b> api will get reflected here
 * without a need to track these.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class DelegatingSignatureVisitor extends SignatureVisitor {

    /**
     * Signature visitor to which all methods of {@link SignatureVisitor} are delegated.
     */
    @Delegate(types = SignatureVisitor.class)
    @NotNull SignatureVisitor signatureVisitor;

    protected DelegatingSignatureVisitor(final int api, final @NotNull SignatureVisitor signatureVisitor) {
        super(api);

        this.signatureVisitor = signatureVisitor;
    }
}
