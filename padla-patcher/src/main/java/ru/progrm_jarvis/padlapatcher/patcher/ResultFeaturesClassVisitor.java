package ru.progrm_jarvis.padlapatcher.patcher;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;
import ru.progrm_jarvis.javacommons.lazy.Lazy;
import ru.progrm_jarvis.javacommons.object.Result;
import ru.progrm_jarvis.javacommons.object.ResultBootstraps;
import ru.progrm_jarvis.javacommons.object.ResultOperators;
import ru.progrm_jarvis.padlapatcher.report.SourcedErrorReporter;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static ru.progrm_jarvis.javacommons.bytecode.asm.AsmUtil.OBJECT_TYPE;
import static ru.progrm_jarvis.javacommons.bytecode.asm.AsmUtil.getHandle;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class ResultFeaturesClassVisitor extends ClassVisitor {

    private static final int ASM_API_VERSION = ASM9;

    /**
     * Type of {@link Result} class
     */
    private static final @NotNull Type RESULT__TYPE = Type.getType(Result.class);

    private static final @NotNull String RESULT__INTERNAL_NAME = RESULT__TYPE.getInternalName();

    /**
     * Type of {@link ResultOperators} class
     */
    private static final @NotNull Type RESULT_OPERATORS__TYPE = Type.getType(ResultOperators.class);

    private static final @NotNull String RESULT_OPERATORS__INTERNAL_NAME = RESULT_OPERATORS__TYPE.getInternalName();

    @NotNull SourcedErrorReporter<@NotNull String> errorReporter;

    private ResultFeaturesClassVisitor(final ClassVisitor classVisitor,
                                       final @NotNull SourcedErrorReporter<@NotNull String> errorReporter) {
        super(ASM_API_VERSION, classVisitor);

        this.errorReporter = errorReporter;
    }

    public static @NotNull ClassVisitor create(final @NonNull ClassVisitor classVisitor,
                                               final @NonNull SourcedErrorReporter<@NotNull String> errorReporter) {
        return new ResultFeaturesClassVisitor(classVisitor, errorReporter);
    }

    @Override
    public @NotNull MethodVisitor visitMethod(final int access, final String name, final String descriptor,
                                              final String signature,
                                              final String[] exceptions) {
        return new ResultFeaturesMethodVisitor(
                super.visitMethod(access, name, descriptor, signature, exceptions),
                errorReporter,
                Lazy.create(() -> Type.getReturnType(descriptor)),
                Lazy.create(() -> {
                    // FIXME
                    throw new UnsupportedOperationException("Oh no");
                })
        );
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class ResultFeaturesMethodVisitor extends MethodVisitor {

        private static final @NotNull String TRY__METHOD_NAME = "_try";
        private static final @NotNull String TRY__METHOD_DESCRIPTOR;

        private static final @NotNull String IS_SUCCESS__METHOD_NAME = "isSuccess";
        private static final @NotNull String IS_SUCCESS__METHOD_DESCRIPTOR;

        private static final @NotNull String UNWRAP__METHOD_NAME = "unwrap";
        private static final @NotNull String UNWRAP__METHOD_DESCRIPTOR;

        private static final @NotNull String TRY_CONVERT_ERROR_TYPE__METHOD_NAME = "tryConvertErrorType";
        private static final @NotNull Handle TRY_CONVERT_ERROR_TYPE__HANDLE;

        private static final @NotNull String RESULT_RESULT__METHOD_DESCRIPTOR
                = getMethodDescriptor(RESULT__TYPE, RESULT__TYPE);

        @NotNull SourcedErrorReporter<@NotNull String> errorReporter;

        @NotNull Lazy<@NotNull Type> returnType;
        @Nullable Lazy<@Nullable String> resultErrorTypeAsDescriptor;

        static {
            Method method;

            try {
                method = ResultOperators.class.getDeclaredMethod(TRY__METHOD_NAME, Result.class);
            } catch (final NoSuchMethodException e) {
                throw new AssertionError(
                        "Failed to find `ResultOperators#" + TRY__METHOD_NAME + "(Result)` method", e
                );
            }
            TRY__METHOD_DESCRIPTOR = getMethodDescriptor(method);

            try {
                method = Result.class.getDeclaredMethod("isSuccess");
            } catch (final NoSuchMethodException e) {
                throw new AssertionError(
                        "Failed to find `Result#" + IS_SUCCESS__METHOD_NAME + "()` method", e
                );
            }
            IS_SUCCESS__METHOD_DESCRIPTOR = getMethodDescriptor(method);

            try {
                method = Result.class.getDeclaredMethod(UNWRAP__METHOD_NAME);
            } catch (final NoSuchMethodException e) {
                throw new AssertionError(
                        "Failed to find `Result#" + UNWRAP__METHOD_NAME + "()` method", e
                );
            }
            UNWRAP__METHOD_DESCRIPTOR = getMethodDescriptor(method);

            {
                @SuppressWarnings("TooBroadScope" /* try-block minimization*/) val type = new Class<?>[]{
                        MethodHandles.Lookup.class, String.class, MethodType.class, Class.class
                };
                try {
                    method = ResultBootstraps.class.getDeclaredMethod(TRY_CONVERT_ERROR_TYPE__METHOD_NAME, type);
                } catch (final NoSuchMethodException e) {
                    throw new AssertionError(
                            "Failed to find `ResultBootstraps#" + TRY_CONVERT_ERROR_TYPE__METHOD_NAME
                                    + "(..)` bootstrap method", e
                    );
                }
            }
            TRY_CONVERT_ERROR_TYPE__HANDLE = getHandle(method);
        }

        private ResultFeaturesMethodVisitor(final MethodVisitor methodVisitor,
                                            final @NotNull SourcedErrorReporter<@NotNull String> errorReporter,
                                            final @NotNull Lazy<@NotNull Type> returnType,
                                            final @Nullable Lazy<@Nullable String> resultErrorTypeAsDescriptor) {
            super(ASM_API_VERSION, methodVisitor);

            this.errorReporter = errorReporter;
            this.returnType = returnType;
            this.resultErrorTypeAsDescriptor = resultErrorTypeAsDescriptor;
        }

        private boolean verifyResultConvertible() { // TODO generic checks
            if (returnType.get().equals(RESULT__TYPE)) return true;

            errorReporter.reportError("`Result#_try()` cannot be used on methods whose return type is not Result");

            return false;
        }

        @Override
        public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor,
                                    final boolean isInterface) {
            if (!isInterface && opcode == INVOKESTATIC
                    && owner.equals(RESULT_OPERATORS__INTERNAL_NAME)
                    && name.equals(TRY__METHOD_NAME)
                    && descriptor.equals(TRY__METHOD_DESCRIPTOR)
                    && verifyResultConvertible()) {
                // <$unwrapped> = <$result>.try_(); :: [..., <$result>] -> [..., <$unwrapped>]

                visitInsn(DUP); // :: -> [..., result, result]
                visitMethodInsn(
                        INVOKEINTERFACE, RESULT__INTERNAL_NAME,
                        IS_SUCCESS__METHOD_NAME, IS_SUCCESS__METHOD_DESCRIPTOR,
                        true
                ); // <$isSuccess> = <$result>.isSuccess(); :: -> [..., <$result>, <$isSuccess>]

                final Label continuationLabel; // point of successful (non-error) control flow continuation
                visitJumpInsn(IFNE, continuationLabel = new Label()); // if (!<isSuccess>) <1>: { :: -> [..., <$result>]
                // <1>
                visitInvokeDynamicInsn(
                        "try-operator", // does not matter but is required and should be non-empty
                        RESULT_RESULT__METHOD_DESCRIPTOR,
                        TRY_CONVERT_ERROR_TYPE__HANDLE,
                        OBJECT_TYPE // FIXME: this requires complicated signature processing
                ); // <$converted> = <error_converter>(<$result>);
                visitInsn(ARETURN); // return <$converted>;
                // </1>

                // `var foo = result.unwrap();`
                visitLabel(continuationLabel);
                visitMethodInsn(
                        INVOKEINTERFACE, RESULT__INTERNAL_NAME,
                        UNWRAP__METHOD_NAME, UNWRAP__METHOD_DESCRIPTOR,
                        true
                ); // <$unwrapped> = <pop>(); :: [...]
            } else super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }
}
