package ru.progrm_jarvis.javacommons.bytecode.asm.signature;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import ru.progrm_jarvis.javacommons.object.Result;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SimpleSignatureParser implements SignatureParser {

    /**
     * Used version of ASM API.
     */
    private static final int ASM_API = Opcodes.ASM9;

    @Override
    public @NotNull Result<@NotNull TypeSignature, ?> parseTypeSignature(final @NotNull String signature) {
        final TypeSignatureVisitor visitor;
        new SignatureReader(signature).accept(visitor = new TypeSignatureVisitor(
                ASM_API, new TypeSignaturePrefixVisitor(ASM_API, new Context<>(Result.error()))
        ));

        return visitor.result(); // FIXME
    }

    // Parsers of low-level types

    private static @NotNull Result<@NotNull BaseTypeSignature, ?> parseBaseTypeSignature(final char baseType) {
        switch (baseType) {
            case 'Z': return Result.success(BaseTypeSignature.BOOLEAN);
            case 'B': return Result.success(BaseTypeSignature.BYTE);
            case 'C': return Result.success(BaseTypeSignature.CHAR);
            case 'S': return Result.success(BaseTypeSignature.SHORT);
            case 'I': return Result.success(BaseTypeSignature.INT);
            case 'J': return Result.success(BaseTypeSignature.LONG);
            case 'F': return Result.success(BaseTypeSignature.FLOAT);
            case 'D': return Result.success(BaseTypeSignature.DOUBLE);
            case 'V': return Result.success(BaseTypeSignature.VOID);
            default: return Result.error("Invalid base type '" + baseType + '\'');
        }
    }

    private static @NotNull Result<@NotNull Type, ?> parseBaseType(final char baseType) {
        switch (baseType) {
            case 'Z': return Result.success(Type.BOOLEAN_TYPE);
            case 'B': return Result.success(Type.BYTE_TYPE);
            case 'C': return Result.success(Type.CHAR_TYPE);
            case 'S': return Result.success(Type.SHORT_TYPE);
            case 'I': return Result.success(Type.INT_TYPE);
            case 'J': return Result.success(Type.LONG_TYPE);
            case 'F': return Result.success(Type.FLOAT_TYPE);
            case 'D': return Result.success(Type.DOUBLE_TYPE);
            case 'V': return Result.success(Type.VOID_TYPE);
            default: return Result.error("Invalid base type '" + baseType + '\'');
        }
    }

    private static @NotNull Result<GenericTypeSignature.@NotNull Bound, ?> parseGenericBound(final char generic) {
        switch (generic) {
            case SignatureVisitor.EXTENDS: return Result.success(GenericTypeSignature.Bound.EXTENDS);
            case SignatureVisitor.SUPER: return Result.success(GenericTypeSignature.Bound.SUPER);
            case SignatureVisitor.INSTANCEOF: return Result.success(GenericTypeSignature.Bound.NONE);
            default: return Result.error("Invalid generic '" + generic + '\'');
        }
    }
    
    // Context

    @Data
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private class Context<S extends Signature> {
        @NotNull Result<? extends @NotNull S, ?> result;
        @NotNull SignatureVisitor state;

        public void update(final Result<? extends @NotNull S, ?> result, final @NotNull SignatureVisitor state) {
            this.result = result;
            this.state = state;
        }
    }

    // Visitors

    /**
     * {@link SignatureVisitor} which delegates its calls to inner state {@link SignatureVisitor}s producing the result.
     */
    @Accessors(fluent = true)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    private static final class TypeSignatureVisitor extends SignatureVisitor {

        @NotNull Context<TypeSignature> context;

        private TypeSignatureVisitor(final int api, final @NotNull Context<TypeSignature> context) {
            super(api);

            this.context = context;
        }

        @Delegate(types = SignatureVisitor.class)
        private @NotNull SignatureVisitor signature() {
            return context.getState();
        }
    }

    @SuppressWarnings("AbstractClassWithoutAbstractMethods") // should not be directly instantiable
    @FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
    private abstract class ContextualSignatureVisitor<S extends Signature> extends SignatureVisitor {

        @NotNull Context<S> context;

        private ContextualSignatureVisitor(final int api, final @NotNull Context<S> context) {
            super(api);

            this.context = context;
        }

        // FIXME: all methods should fail
    }

    private final class TerminalSignatureVisitor<S extends Signature> extends ContextualSignatureVisitor<S> {

        private TerminalSignatureVisitor(final int api, final @NotNull Context<S> context) {
            super(api, context);
        }
    }

    private final class TypeSignaturePrefixVisitor extends ContextualSignatureVisitor<TypeSignature> {

        private TypeSignaturePrefixVisitor(final int api, final @NotNull Context<TypeSignature> context) {
            super(api, context);
        }

        @Override
        public void visitBaseType(final char descriptor) {
            final Context<TypeSignature> context;
            (context = this.context).update(
                    parseBaseTypeSignature(descriptor),
                    new TerminalSignatureVisitor<>(api, context)
            );
        }

        @Override
        public void visitTypeVariable(final @NotNull String name) {
            final Context<TypeSignature> context;
            (context = this.context).update(
                    Result.success(TypeVariableSignature.of(name)),
                    new TerminalSignatureVisitor<>(api, context)
            );
        }

        @Override
        public @NotNull SignatureVisitor visitArrayType() {
            final Context<TypeSignature> context;
            (context = this.context).update(
                    Result.success(TypeVariableSignature.of(name)),
                    new TerminalSignatureVisitor<>(api, context)
            );
            final SignatureVisitor arrayTypeVisitor;
            state = arrayTypeVisitor = new ArrayTypeVisitor(api, 1); // start array visit
            return arrayTypeVisitor;
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private final class ArrayTypeVisitor extends ContextualSignatureVisitor {

        @Range(from = 1, to = Integer.MAX_VALUE) int depth;

        private ArrayTypeVisitor(final int api, @Range(from = 1, to = Integer.MAX_VALUE) final int depth) {
            super(api, context);

            this.depth = depth;
        }
    }

    @Accessors(fluent = true)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    private static final class TypeSignatureVisitor extends SignatureVisitor {

        @NotNull Result<? extends @NotNull Signature, ?> result;
        @Delegate(types = SignatureVisitor.class)
        @NotNull SignatureVisitor state;

        private TypeSignatureVisitor(final int api,
                                     final @NotNull Result<? extends @NotNull Signature, ?> result) {
            super(api);

            state = new PrefixVisitor();
        }

        @Accessors(fluent = true)
        @FieldDefaults(level = AccessLevel.PRIVATE)
        private static final class PrefixVisitor extends SignatureVisitor {

        }

        @Override
        public void visitBaseType(final char descriptor) {
            final Result<? extends Signature, ?> result;
            this.result = (result = this.result).isSuccess()
                    ? Result.error("Type signature is already set to " + result.unwrap())
                    : parseBaseTypeSignature(descriptor);
        }

        @Override
        public void visitTypeVariable(final @NotNull String name) {
            final Result<? extends Signature, ?> result;
            this.result = (result = this.result).isSuccess()
                    ? Result.error("Type signature is already set to " + result.unwrap())
                    : Result.success(TypeVariableSignature.of(name));
        }

        @Override
        public @NotNull SignatureVisitor visitArrayType() {
            return super.visitArrayType();
        }

        @FieldDefaults(level = AccessLevel.PRIVATE)
        private final class GenericTypeSignatureVisitor extends SignatureVisitor {

            @Nullable String classType;

            public GenericTypeSignatureVisitor(final int api) {
                super(api);
            }

            private <T> void error(final @NotNull T error) {
                result = Result.error(error);
                genericTypeSignatureVisitor = null;
            }

            @Override
            public void visitClassType(final @NotNull String name) {
                if (classType != null) error("Duplicate class type in signature, initial was \"");
                else classType = GenericTypeSignature.of()
                super.visitClassType(name);
            }
        }
    }

    // visits "prefix" of signatues of "ClassSignature" and "MethodSignature"
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private final class FormalTypeParametersVisitor extends SignatureVisitor {

        @NotNull List<? extends FormalTypeParameter> formalTypeParameters;

        private FormalTypeParametersVisitor(final int api) {
            super(api);
        }

        @Override
        public void visitFormalTypeParameter(final @NonNull String name) {
            super.visitFormalTypeParameter(name);
        }
    }

    // visits "prefix" of signatures of "ClassSignature" and "MethodSignature"
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private final class FormalTypeParameterVisitor extends SignatureVisitor {

        private final String bc;

        private BaseVisitor(final int api) {
            super(api);
        }
    }

    enum State {
        NONE,
        CLASS_SIGNATURE_PREFIX
    }
}
