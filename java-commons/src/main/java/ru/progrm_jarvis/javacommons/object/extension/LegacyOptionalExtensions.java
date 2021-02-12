package ru.progrm_jarvis.javacommons.object.extension;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.invoke.MethodType.methodType;

/**
 * Extensions to provide new {@link Optional} methods on older Java versions.
 */
@UtilityClass
@SuppressWarnings("OptionalUsedAsFieldOrParameterType") // this is the whole concept of this class
public class LegacyOptionalExtensions {

    /**
     * Method handle of
     * {@link Optional}{@code .isEmpty()} method
     * being {@code null} if this method is unavailable.
     */
    private final @Nullable MethodHandle IS_EMPTY_METHOD_HANDLE;

    /**
     * Method handle of
     * {@link Optional}{@code .ifPresentOrElse(}{@link Consumer}{@code , }{@link Runnable}{@code )} method
     * being {@code null} if this method is unavailable.
     */
    private final @Nullable MethodHandle IF_PRESENT_OR_ELSE_METHOD_HANDLE;

    /**
     * Method handle of
     * {@link Optional}{@code .or(}{@link Supplier}{@code <? extends }{@link Object}{@code >>)} method
     * being {@code null} if this method is unavailable.
     */
    private final @Nullable MethodHandle OR_METHOD_HANDLE;

    /**
     * Method handle of
     * {@link Optional}{@code .stream()} method
     * being {@code null} if this method is unavailable.
     */
    private final @Nullable MethodHandle STREAM_METHOD_HANDLE;

    /**
     * Method handle of
     * {@link Optional}{@code .orElseThrow()} method
     * being {@code null} if this method is unavailable.
     */
    private final @Nullable MethodHandle OR_ELSE_THROW_METHOD_HANDLE;

    static {
        val lookup = MethodHandles.lookup();
        {
            MethodHandle handle;
            try {
                handle = lookup.findVirtual(
                        Optional.class, "isEmpty", methodType(boolean.class)
                );
            } catch (final NoSuchMethodException | IllegalAccessException e) {
                handle = null;
            }
            IS_EMPTY_METHOD_HANDLE = handle;
        }
        {
            MethodHandle handle;
            try {
                handle = lookup.findVirtual(
                        Optional.class, "ifPresentOrElse", methodType(void.class, Consumer.class, Runnable.class)
                );
            } catch (final NoSuchMethodException | IllegalAccessException e) {
                handle = null;
            }
            IF_PRESENT_OR_ELSE_METHOD_HANDLE = handle;
        }
        {
            MethodHandle handle;
            try {
                handle = lookup.findVirtual(
                        Optional.class, "or", methodType(Optional.class, Supplier.class)
                );
            } catch (final NoSuchMethodException | IllegalAccessException e) {
                handle = null;
            }
            OR_METHOD_HANDLE = handle;
        }
        {
            MethodHandle handle;
            try {
                handle = lookup.findVirtual(
                        Optional.class, "stream", methodType(Stream.class)
                );
            } catch (final NoSuchMethodException | IllegalAccessException e) {
                handle = null;
            }
            STREAM_METHOD_HANDLE = handle;
        }
        {
            MethodHandle handle;
            try {
                handle = lookup.findVirtual(
                        Optional.class, "orElseThrow", methodType(Object.class)
                );
            } catch (final NoSuchMethodException | IllegalAccessException e) {
                handle = null;
            }
            OR_ELSE_THROW_METHOD_HANDLE = handle;
        }
    }

    /**
     * Checks if the given optional is {@link Optional#empty() empty}.
     *
     * @param optional optional on which the operation happens
     * @param <T> type of the value
     * @return {@code true} if the optional is {@link Optional#empty()} and {@code false} otherwise
     * @apiNote this method is available on {@link Optional} itself since Java 11
     */
    @SneakyThrows // call to `MethodHandle#invokeExact(...)`
    public <T> boolean isEmpty(final @NotNull Optional<T> optional) {
        return IS_EMPTY_METHOD_HANDLE == null
                ? !optional.isPresent() : (boolean) IS_EMPTY_METHOD_HANDLE.invokeExact(optional);
    }

    /**
     * Runs the corresponding action depending on whether the optional is {@link Optional#isPresent() present}.
     *
     * @param optional optional on which the operation happens
     * @param action action to be run on non-{@link Optional#empty() empty} optional's value
     * @param emptyAction action to be run if the optional is empty
     * @param <T> type of the value
     * @throws NullPointerException if the optional is {@link Optional#isPresent() present} but {@code action} is {@code
     * null}
     * or if the optional is {@link Optional#empty() empty} but {@code emptyAction} is {@code null}
     * @apiNote this method is available on {@link Optional} itself since Java 9
     */
    @SneakyThrows // call to `MethodHandle#invokeExact(...)`
    public <T> void ifPresentOrElse(final @NotNull Optional<T> optional,
                                    // note: nullness checking of lambdas is done lazily according to the contract
                                    final @NotNull Consumer<? super T> action,
                                    final @NotNull Runnable emptyAction) {
        if (IF_PRESENT_OR_ELSE_METHOD_HANDLE == null) if (optional.isPresent()) action.accept(optional.get());
        else emptyAction.run();
        else IF_PRESENT_OR_ELSE_METHOD_HANDLE.invokeExact(optional, action, emptyAction);
    }

    /**
     * Returns the given optional if it is {@link Optional#isPresent() present}
     * or an optional provided by the given supplier otherwise.
     *
     * @param optional optional on which the operation happens
     * @param supplier supplier of the value in case of optional being {@link Optional#empty()}
     * @param <T> type of the value
     * @return given optional if it is {@link Optional#isPresent() present}
     * or an optional provided by the given supplier otherwise
     *
     * @throws NullPointerException if {@code supplier} is {@code null}
     * @apiNote this method is available on {@link Optional} itself since Java 9
     */
    @Contract("_, null -> fail")
    @SneakyThrows // call to `MethodHandle#invokeExact(...)`
    @SuppressWarnings({"unchecked" /* return casts */, "Contract" /* // Lombok's annotation is treated incorrectly */})
    public <T> Optional<T> or(final @NotNull Optional<T> optional,
                              final @NonNull Supplier<? extends Optional<? extends T>> supplier) {
        return OR_METHOD_HANDLE == null
                ? optional.isPresent() ? optional : (Optional<T>) supplier.get()
                : (Optional<T>) OR_METHOD_HANDLE.invokeExact(optional, supplier);
    }

    /**
     * Returns the {@link Stream stream} created from the optional's value if it is {@link Optional#isPresent() present}
     * or an {@link Stream#empty() empty stream} otherwise.
     *
     * @param optional optional on which the operation happens
     * @param <T> type of the value
     * @return given optional if it is {@link Optional#isPresent() present}
     * or an optional provided by the given supplier otherwise
     */
    @SuppressWarnings("unchecked") // return cast
    @SneakyThrows // call to `MethodHandle#invokeExact(...)`
    public <T> Stream<T> stream(final @NotNull Optional<T> optional) {
        return STREAM_METHOD_HANDLE == null
                ? optional.map(Stream::of).orElseGet(Stream::empty)
                : (Stream<T>) STREAM_METHOD_HANDLE.invokeExact(optional);
    }

    /**
     * Returns the value of the optional if it is {@link Optional#isPresent() present}
     * or throws {@link NoSuchElementException} otherwise.
     *
     * @param optional optional on which the operation happens
     * @param <T> type of the value
     * @return the value of the optional if it is {@link Optional#isPresent() present}
     */
    @SuppressWarnings("unchecked") // return cast
    @SneakyThrows // call to `MethodHandle#invokeExact(...)`
    public <T> T orElseThrow(final @NotNull Optional<T> optional) {
        return OR_ELSE_THROW_METHOD_HANDLE == null
                ? optional.orElseThrow(() -> new NoSuchElementException("No value present"))
                : (T) OR_ELSE_THROW_METHOD_HANDLE.invokeExact(optional);
    }
}
