package ru.progrm_jarvis.javacommons.bytecode.asm.signature;

import lombok.AccessLevel;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureVisitor;
import ru.progrm_jarvis.javacommons.object.Pair;
import ru.progrm_jarvis.javacommons.object.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// this is TraceSignatureVisitor but generates an object representation rather than a String
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class UncheckedBadlyDesignedSignatureParser extends BadlyDesignedSignatureParser<Signature> {

    /**
     * Used version of ASM API.
     */
    private static final int ASM_API = Opcodes.ASM9;

    @Nullable SignatureVisitor activeVisitor;
    @Nullable Signature result;

    private UncheckedBadlyDesignedSignatureParser() {
        super(ASM_API);
    }

    public static @NotNull UncheckedBadlyDesignedSignatureParser create() {
        return new UncheckedBadlyDesignedSignatureParser();
    }

    @Override
    public @NotNull Optional<Signature> result() {
        return Optional.ofNullable(result);
    }

    private static @NotNull Type parseBaseType(final char baseType) {
        switch (baseType) {
            case 'Z':
                return Type.BOOLEAN_TYPE;
            case 'B':
                return Type.BYTE_TYPE;
            case 'C':
                return Type.CHAR_TYPE;
            case 'S':
                return Type.SHORT_TYPE;
            case 'I':
                return Type.INT_TYPE;
            case 'J':
                return Type.LONG_TYPE;
            case 'F':
                return Type.FLOAT_TYPE;
            case 'D':
                return Type.DOUBLE_TYPE;
            case 'V':
                return Type.VOID_TYPE;
            default:
                throw new AssertionError("Invalid base type '" + baseType + '\'');
        }
    }

    private static @NotNull GenericTypeSignature.Bound parseGenericBound(final char generic) {
        switch (generic) {
            case SignatureVisitor.EXTENDS:
                return GenericTypeSignature.Bound.EXTENDS;
            case SignatureVisitor.SUPER:
                return GenericTypeSignature.Bound.SUPER;
            case SignatureVisitor.INSTANCEOF:
                return GenericTypeSignature.Bound.NONE;
            default:
                throw new AssertionError("Invalid generic '" + generic + '\'');
        }
    }

    private @NotNull SignatureVisitor activeVisitor(final @NotNull SignatureVisitor activeVisitor) {
        assert this.activeVisitor == null : "activeVisitor is already set";

        this.activeVisitor = activeVisitor;

        return activeVisitor;
    }

    private @NotNull SignatureVisitor activeVisitor() {
        val activeVisitor = this.activeVisitor;
        assert activeVisitor != null : "activeVisitor should have been set";

        return activeVisitor;
    }

    @Override
    public void visitFormalTypeParameter(final String name) {
        // lazy initialization is required to delegate to the signature-kind specific implementation
        SignatureVisitor currentActiveVisitor;
        if ((currentActiveVisitor = activeVisitor) == null) activeVisitor = currentActiveVisitor
                = new ClassSignatureVisitor(this, new ArrayList<>(), new ArrayList<>()) {
            @Override
            protected void onVisitEnd(final @NotNull ClassSignature typeSignature) {
                result = typeSignature;
                activeVisitor = null;
            }
        };

        currentActiveVisitor.visitFormalTypeParameter(name);
    }

    @Override
    public SignatureVisitor visitClassBound() {
        return activeVisitor().visitClassBound();
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        return activeVisitor().visitInterfaceBound();
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        return activeVisitor().visitSuperclass();
    }

    @Override
    public SignatureVisitor visitInterface() {
        return activeVisitor().visitInterface();
    }

    @Override
    public SignatureVisitor visitParameterType() {
        return activeVisitor().visitParameterType();
    }

    @Override
    public SignatureVisitor visitReturnType() {
        return activeVisitor().visitReturnType();
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        return activeVisitor().visitExceptionType();
    }

    @Override
    public void visitBaseType(final char descriptor) {
        activeVisitor().visitBaseType(descriptor);
    }

    @Override
    public void visitTypeVariable(final String name) {
        activeVisitor().visitTypeVariable(name);
    }

    @Override
    public SignatureVisitor visitArrayType() {
        return activeVisitor().visitArrayType();
    }

    @Override
    public void visitClassType(final String name) {
        activeVisitor().visitClassType(name);
    }

    @Override
    public void visitInnerClassType(final String name) {
        activeVisitor().visitInnerClassType(name);
    }

    @Override
    public void visitTypeArgument() {
        activeVisitor().visitTypeArgument();
    }

    @Override
    public SignatureVisitor visitTypeArgument(final char wildcard) {
        return activeVisitor().visitTypeArgument(wildcard);
    }

    @Override
    public void visitEnd() {
        activeVisitor().visitEnd();
    }

    // This should either produce a type or a reference to formal parameter
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private abstract static class TypeSignatureVisitor extends DelegatingSignatureVisitor {

        @NotNull GenericTypeSignature.Bound bound;
        @NonFinal @Nullable Result<
                @NotNull Pair<@NotNull Type, @NotNull List<@NotNull TypeSignature>>,
                @NotNull String
                > typeOrGeneric;
        @NonFinal int arrayDepth;

        protected TypeSignatureVisitor(final @NotNull SignatureVisitor parent,
                                       final @NotNull GenericTypeSignature.Bound bound) {
            super(ASM_API, parent);

            this.bound = bound;
        }

        private void trySetTypeOrGeneric(final @NotNull Result<
                @NotNull Pair<@NotNull Type, @NotNull List<@NotNull TypeSignature>>,
                @NotNull String
                > typeOrGeneric) {
            assert this.typeOrGeneric == null : "typeOrGeneric has already been set";

            this.typeOrGeneric = typeOrGeneric;
        }

        private @NotNull Result<
                @NotNull Pair<@NotNull Type, @NotNull List<@NotNull TypeSignature>>,
                @NotNull String
                > tryGetTypeOrGeneric() {
            val typeOrGeneric = this.typeOrGeneric;
            assert typeOrGeneric != null : "typeOrGeneric should have been set";

            return typeOrGeneric;
        }

        @Override
        public void visitTypeVariable(final String name) {
            trySetTypeOrGeneric(Result.error(name));

            super.visitTypeVariable(name);
        }

        @Override
        public SignatureVisitor visitArrayType() {
            arrayDepth++;

            return this;
        }

        @Override
        public void visitInnerClassType(final String name) {
            super.visitInnerClassType(name);
        }

        @Override
        public void visitClassType(final String name) {
            trySetTypeOrGeneric(Result.success(Pair.of(Type.getType('L' + name + ';'), new ArrayList<>())));

            super.visitClassType(name);
        }

        @Override
        public SignatureVisitor visitTypeArgument(final char wildcard) {
            final List<TypeSignature> parameters;
            {
                val rootTypeOrGeneric = tryGetTypeOrGeneric();
                assert rootTypeOrGeneric.isSuccess() : "generic parameters cannot be present on generic types";
                parameters = rootTypeOrGeneric.unwrap().getSecond();
            }

            // recursive visitor to add generic parameters
            return new TypeSignatureVisitor(this, parseGenericBound(wildcard)) {
                @Override
                protected void onVisitEnd(final @NotNull TypeSignature genericParameter) {
                    parameters.add(genericParameter);
                    System.out.println("~~~~~~~~~ on " + genericParameter);
                }
            };
        }

        @Override
        public void visitEnd() {
            System.out.println("End: " + tryGetTypeOrGeneric());
            onVisitEnd(ArrayTypeSignature.wrap(tryGetTypeOrGeneric()
                            .<TypeSignature>map(typeAndGenerics -> GenericTypeSignature.of(
                                    bound, typeAndGenerics.getFirst(), typeAndGenerics.getSecond()
                            ))
                            .orComputeDefault(TypeVariableSignature::of),
                    arrayDepth
            ));
            System.out.println("Called onVisitEnd()");

            super.visitEnd();
        }

        protected abstract void onVisitEnd(final @NotNull TypeSignature typeSignature);
    }


    /**
     * {@link SignatureVisitor} responsible for building {@link ClassSignature}.
     */
    @ToString // TODO remove this
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private abstract class ClassSignatureVisitor extends DelegatingSignatureVisitor {

        @NotNull List<@NotNull FormalTypeParameter> formalTypeParameters;
        @NonFinal @Nullable TypeSignature superClass;
        @NotNull List<@NotNull TypeSignature> interfaces;

        @NonFinal transient @Nullable GenericParameterSignatureVisitor pendingGenericParameterVisitor;

        protected ClassSignatureVisitor(final @NotNull SignatureVisitor parent,
                                        final @NotNull List<@NotNull FormalTypeParameter> formalTypeParameters,
                                        final @NotNull List<@NotNull TypeSignature> interfaces) {
            super(ASM_API, parent);

            this.formalTypeParameters = formalTypeParameters;
            this.interfaces = interfaces;
        }

        @Override
        public void visitFormalTypeParameter(final String name) {
            // note: this visit-method does not return a new visitor which is done later by
            // `visitClassBound()` thus the visitor has to be saved "for later use"
            pendingGenericParameterVisitor = new GenericParameterSignatureVisitor(this, name, new ArrayList<>()) {
                @Override
                protected void onVisitEnd(final @NotNull FormalTypeParameter typeSignature) {
                    formalTypeParameters.add(typeSignature);
                }
            };

            super.visitFormalTypeParameter(name);
        }

        @Override
        public SignatureVisitor visitClassBound() {
            val pendingGenericParameterVisitor = this.pendingGenericParameterVisitor;
            assert pendingGenericParameterVisitor != null : "pendingGenericParameterVisitor should have been set";
            this.pendingGenericParameterVisitor = null;

            return pendingGenericParameterVisitor.visitClassBound();
        }

        @Override
        public SignatureVisitor visitSuperclass() {
            return new TypeSignatureVisitor(this, GenericTypeSignature.Bound.NONE) {
                @Override
                protected void onVisitEnd(final @NotNull TypeSignature typeSignature) {
                    System.out.println("~~ onVisitEnd of visitSuperclass");
                    superClass = typeSignature;
                }
            };
        }

        @Override
        public SignatureVisitor visitInterface() {
            return new TypeSignatureVisitor(this, GenericTypeSignature.Bound.NONE) {
                @Override
                protected void onVisitEnd(final @NotNull TypeSignature typeSignature) {
                    System.out.println("~~ onVisitEnd of visitInterface");
                    interfaces.add(typeSignature);
                }
            };
        }

        @Override
        public void visitEnd() {
            val superClass = this.superClass;
            System.out.println(this);
            assert superClass != null : "superClass should have been set";

            onVisitEnd(ClassSignature.of(formalTypeParameters, superClass, interfaces));

            super.visitEnd();
        }

        protected abstract void onVisitEnd(final @NotNull ClassSignature typeSignature);
    }

    /**
     * {@link SignatureVisitor} responsible for building {@link FormalTypeParameter}.
     */
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private abstract class GenericParameterSignatureVisitor extends DelegatingSignatureVisitor {

        @NotNull String formalName;
        @NonFinal @Nullable TypeSignature classBound;
        @NotNull List<@NotNull TypeSignature> interfaceBounds;

        private GenericParameterSignatureVisitor(final @NotNull SignatureVisitor parent,
                                                 final @NotNull String formalName,
                                                 final @NotNull List<@NotNull TypeSignature> interfaceBounds) {
            super(ASM_API, parent);

            this.formalName = formalName;
            this.interfaceBounds = interfaceBounds;
        }

        @Override
        public SignatureVisitor visitClassBound() {
            return new TypeSignatureVisitor(this, GenericTypeSignature.Bound.NONE) {
                @Override
                protected void onVisitEnd(final @NotNull TypeSignature typeSignature) {
                    classBound = typeSignature;
                    System.out.println("Called onVisitEnd() ~~~visitClassBound");
                }
            };
        }

        @Override
        public SignatureVisitor visitInterfaceBound() {
            return new TypeSignatureVisitor(this, GenericTypeSignature.Bound.NONE) {
                @Override
                protected void onVisitEnd(final @NotNull TypeSignature typeSignature) {
                    interfaceBounds.add(typeSignature);
                    System.out.println("Called onVisitEnd() ~~~visitInterfaceBound");
                }
            };
        }

        @Override
        public void visitEnd() { // FIXME this never gets called because the end of <...> is determined by start of
            // visitSuperclass()
            val classBound = this.classBound;
            assert classBound != null : "classBound should have been set for generic parameter \"" + formalName + '"';

            onVisitEnd(FormalTypeParameter.of(formalName, classBound, interfaceBounds));

            super.visitEnd();
        }

        protected abstract void onVisitEnd(final @NotNull FormalTypeParameter typeSignature);
    }
}
