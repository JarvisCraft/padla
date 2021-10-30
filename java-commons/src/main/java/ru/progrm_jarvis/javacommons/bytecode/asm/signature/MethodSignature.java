package ru.progrm_jarvis.javacommons.bytecode.asm.signature;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import ru.progrm_jarvis.javacommons.ownership.annotation.Own;

import java.util.List;

public interface MethodSignature extends Signature {

    static @NotNull MethodSignature of(final @NonNull @Own List<@NotNull TypeSignature> typeArguments,
                                       final @NonNull Type baseType) {
        return new SimpleMethodSignature(Collections.unmodifiableList(typeArguments), baseType);
    }

    static @NotNull MethodSignature ofCopy(final @NonNull @Ref List<@NotNull TypeSignature> typeArguments,
                                           final @NonNull Type baseType) {
        return new SimpleMethodSignature(
                Collections.unmodifiableList(Arrays.asList(typeArguments.toArray(EMPTY_TYPE_SIGNATURE_ARRAY))),
                baseType
        );
    }

    @Override
    default Signature.@NotNull SignatureKind signatureKind() {
        return SignatureKind.METHOD;
    }

    @NotNull @Unmodifiable List<@NotNull Type> parameterTypes();

    @NotNull Type returnType();

    //<editor-fold desc="Signature sealing" defaultstate="collapsed">

    @Override
    default @NotNull ClassSignature asClass() {
        throw new UnsupportedOperationException("This is a MethodSignature");
    }

    @Override
    default @NotNull MethodSignature asMethod() {
        return this;
    }

    @Override
    default @NotNull TypeSignature asType() {
        throw new UnsupportedOperationException("This is a MethodSignature");
    }

    //</editor-fold>

    @Value
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class SimpleMethodSignature implements MethodSignature {
        @NotNull
        @Unmodifiable
        List<@NotNull TypeSignature> parameterTypes;
        @NotNull Type returnType;
    }
}
