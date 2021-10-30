package ru.progrm_jarvis.javacommons.object;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.annotation.Any;

import java.lang.invoke.*;

import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodType.methodType;

/**
 * <p>Bootstrap methods related to {@link Result}.</p>
 * <p>This class is for internal usage and regular users don't usually need to directly use it</p>
 */
@UtilityClass
public class ResultBootstraps {

    /**
     * Method handle of {@link  #tryConvertErrorTypeInternal(Result, Class)} method
     */
    private final @NotNull MethodHandle TRY_CONVERT_ERROR_TYPE_INTERNAL__METHOD_HANDLE;

    static {
        val lookup = MethodHandles.lookup();
        val methodType = methodType(Result.class, Result.class, Class.class);
        try {
            TRY_CONVERT_ERROR_TYPE_INTERNAL__METHOD_HANDLE = lookup
                    .findStatic(ResultBootstraps.class, "tryConvertErrorTypeInternal", methodType);
        } catch (final NoSuchMethodException | IllegalAccessException e) {
            throw new AssertionError("Failed to find own method", e);
        }
    }

    /**
     * <p>Creates a {@link CallSite} whose target has the signature {@code (}{@link Result}{@code ) }{@link Result}
     * and performs the dynamically validated conversion of the error types.</p>
     * <p>The consumed {@link Result} has to be {@link Result#isError() an error},
     * otherwise the behaviour is not specified.</p>
     * <p>The conversion rules are the following:</p>
     * <dl>
     *     <dt>If the error type is {@link Void}</dt>
     *     <dd>Then no conversions happen and {@link Result#nullError() null errror} gets produced</dd>
     *     <dt>If the error type {@link Class#isAssignableFrom(Class) is assignable from} the value</dt>
     *     <dd>Then the error value is kept as-is</dd>
     *     <dt>If the error type {@link Class#isAssignableFrom(Class) is assignable from} {@link String}</dt>
     *     <dd>Then the error value gets converted to a {@link String} using its {@link Object#toString()}</dd>
     * </dl>
     *
     * @param lookup representing the caller
     * @param name any non-{@code null} string (this is a JVM-required parameter)
     * @param invokedType type of the produced {@link MethodHandle},
     * should be {@code (}{@link Result}{@code ) }{@link Result}
     * @param errorType type of the resulting error
     * @return call site whose target converts the result's error type to the required one
     *
     * @throws NullPointerException if {@code lookup} is {@code null}
     * @throws NullPointerException if {@code name} is {@code null}
     * @throws NullPointerException if {@code invokedType} is {@code null}
     * @throws NullPointerException if {@code errorType} is {@code null}
     * @throws IllegalArgumentException if {@code invokedType} has signature
     * other than {@code (}{@link Result}{@code ) }{@link Result}
     */
    // produces `(Lru.progrm_jarvis.object.Result;)Lru.progrm_jarvis.object.Result;` call site
    public @NotNull CallSite tryConvertErrorType(
            @SuppressWarnings("unused" /*JVM-required */) final MethodHandles.@NonNull Lookup lookup,
            @SuppressWarnings("unused" /*JVM-required */) final @NonNull String name,
            @SuppressWarnings("TypeMayBeWeakened" /*JVM-required */) final @NonNull MethodType invokedType,
            final @NonNull Class<?> errorType
    ) {
        if (invokedType.parameterCount() != 1
                || invokedType.parameterType(1) != Result.class
                || invokedType.returnType() != Result.class) throw new IllegalArgumentException(
                "invokedType should have `(Result)Result` signature"
        );

        if (errorType == void.class) return NullErrorResultCallSiteHolder.INSTANCE;

        return new ConstantCallSite(
                insertArguments(TRY_CONVERT_ERROR_TYPE_INTERNAL__METHOD_HANDLE, 1, errorType)
        );
    }

    /**
     * Attempts to convert the result's error type to the provided one.
     *
     * @param result error result whose error value should be converted
     * @param targetErrorType type of the target error
     * @param <T> current formal result successful type
     * @param <E> the original error type
     * @param <R> the resulting error type
     * @return result with the converted error type
     */
    private <@Any T, E, R> @NotNull Result<T, R> tryConvertErrorTypeInternal(
            final @NotNull Result<T, E> result,
            final @NotNull Class<? super R> targetErrorType
    ) {
        assert result.isError() : "Only error Results' error types can be converted";

        final E error;
        return (error = result.unwrapError()) == null
                ? Result.nullError() // explicit call to validate generic types without unchecked casts
                : Result.error(tryConvert(error, targetErrorType));
    }

    /**
     * Attempts to convert the given error object into the provided one.
     *
     * @param error error object which should be converted
     * @param targetErrorType type of the target error
     * @param <E> the original error type
     * @param <R> the resulting error type
     * @return error converted to the specified type
     */
    private <E, R> R tryConvert(final @NotNull E error, final @NotNull Class<? super R> targetErrorType) {
        final Class<?> errorClass;
        // upcast if it is possible
        if (targetErrorType.isAssignableFrom(errorClass = error.getClass())) return uncheckedCast(error);

        // convert via `toString()` if it is possible
        if (targetErrorType.isAssignableFrom(String.class)) return uncheckedCast(error.toString());

        // TODO: records and Throwables explicit support

        throw new IllegalArgumentException(
                "Error value \"" + error + "\" of type \"" + errorClass + "\" " +
                        "cannot be converted to the error value of type \"" + targetErrorType + "\""
        );
    }

    /**
     * Performs a generic cast of the specified value.
     *
     * @param value the value to be cast to the specified type
     * @param <T>   the required type of the value
     * @return the provided value cast to the specific type
     * @apiNote this is effectively no-op
     */
    @SuppressWarnings("unchecked")
    @Contract("null -> null; _ -> param1")
    private <T> T uncheckedCast(final Object value) {
        return (T) value;
    }

    /**
     * Holder of static {@link  CallSite} whose target is {@link MethodHandle}
     * with signature {@code (}{@link Result}{@code ) }{@link Result}
     * which ignores the first argument and delegates to {@link Result#nullError()}.
     */
    private final class NullErrorResultCallSiteHolder {

        /**
         * Static {@link CallSite} producing {@code (}{@link Result}{@code ) }{@link Result} {@link MethodHandle}
         * which always returns {@link Result#nullError()}.
         */
        private static final @NotNull CallSite INSTANCE;

        static {
            final MethodHandle methodHandle;
            {
                val lookup = MethodHandles.lookup();
                val methodType = methodType(Result.class);
                try {
                    methodHandle = lookup.findStatic(Result.class, "nullError", methodType);
                } catch (final NoSuchMethodException | IllegalAccessException e) {
                    throw new IllegalStateException("Failed to find method", e);
                }
            }

            INSTANCE = new ConstantCallSite(MethodHandles.dropArguments(methodHandle, 0, Result.class));
        }
    }
}
