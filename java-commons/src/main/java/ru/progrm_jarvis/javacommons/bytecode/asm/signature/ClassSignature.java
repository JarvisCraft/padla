package ru.progrm_jarvis.javacommons.bytecode.asm.signature;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import ru.progrm_jarvis.javacommons.ownership.annotation.Own;
import ru.progrm_jarvis.javacommons.ownership.annotation.Ref;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static ru.progrm_jarvis.javacommons.bytecode.asm.signature.FormalTypeParameter.EMPTY_FORMAL_TYPE_PARAMETER_ARRAY;
import static ru.progrm_jarvis.javacommons.bytecode.asm.signature.TypeSignature.EMPTY_TYPE_SIGNATURE_ARRAY;

public interface ClassSignature extends Signature {

    @NotNull @Unmodifiable List<? extends @NotNull FormalTypeParameter> formalTypeParameters();

    @NotNull TypeSignature superClass();

    @NotNull @Unmodifiable List<? extends @NotNull TypeSignature> interfaceBounds();

    static @NotNull ClassSignature of(
            final @NonNull @Unmodifiable @Own List<? extends @NotNull FormalTypeParameter> formalTypeParameters,
            final @NonNull TypeSignature superClass,
            final @NonNull @Unmodifiable @Own List<? extends @NotNull TypeSignature> interfaces
    ) {
        return new SimpleClassSignature(formalTypeParameters, superClass, interfaces);
    }

    static @NotNull ClassSignature ofCopy(
            final @NonNull @Unmodifiable @Ref List<? extends @NotNull FormalTypeParameter> formalTypeParameters,
            final @NonNull TypeSignature superClass,
            final @NonNull @Unmodifiable @Ref List<? extends @NotNull TypeSignature> interfaces
    ) {
        return new SimpleClassSignature(
                unmodifiableList(Arrays.asList(formalTypeParameters.toArray(EMPTY_FORMAL_TYPE_PARAMETER_ARRAY))),
                superClass,
                unmodifiableList(Arrays.asList(interfaces.toArray(EMPTY_TYPE_SIGNATURE_ARRAY)))
        );
    }

    //<editor-fold desc="Signature sealing" defaultstate="collapsed">

    @Override
    default Signature.@NotNull SignatureKind signatureKind() {
        return SignatureKind.TYPE;
    }

    @Override
    default @NotNull ClassSignature asClass() {
        return this;
    }

    @Override
    default @NotNull MethodSignature asMethod() {
        throw new UnsupportedOperationException("This is a ClassSignature");
    }

    @Override
    default @NotNull TypeSignature asType() {
        throw new UnsupportedOperationException("This is a ClassSignature");
    }

    //</editor-fold>

    @Value
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class SimpleClassSignature implements ClassSignature {
        @NotNull @Unmodifiable List<? extends @NotNull FormalTypeParameter> formalTypeParameters;
        @NotNull TypeSignature superClass;
        @NotNull @Unmodifiable List<? extends @NotNull TypeSignature> interfaceBounds;
    }
}
