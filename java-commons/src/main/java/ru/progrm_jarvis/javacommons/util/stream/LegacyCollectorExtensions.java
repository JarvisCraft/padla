package ru.progrm_jarvis.javacommons.util.stream;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import ru.progrm_jarvis.javacommons.ownership.annotation.Own;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.invoke.MethodType.methodType;

@UtilityClass
public class LegacyCollectorExtensions {
    /**
     * Method handle of
     * {@link Collectors}{@code .toList()} method
     * being {@code null} if this method is unavailable.
     */
    private static final @Nullable MethodHandle TO_LIST__METHOD_HANDLE;

    static {
        val lookup = MethodHandles.lookup();
        {
            MethodHandle handle;
            try {
                handle = lookup.findVirtual(Stream.class, "toList", methodType(List.class));
            } catch (final NoSuchMethodException | IllegalAccessException e) {
                handle = null;
            }
            TO_LIST__METHOD_HANDLE = handle;
        }
    }

    /**
     * <p>Returns an immutable list containing the elements of the stream.</p>
     * <p>This is a terminal operation</p>
     *
     * @param stream stream to convert into the list
     * @param <T> type of stream's elements
     * @return an unmodifiable list consisting os this stream's elements
     *
     * @apiNote this method is available on {@link Stream} itself since Java 16
     */
    @SneakyThrows // call to `MethodHandle#invokeExact(...)`
    public static <T> @NotNull @Unmodifiable List<T> toList(final @NotNull @Own Stream<?> stream) {
        if (TO_LIST__METHOD_HANDLE == null) {
            // all implementation rely on `toArray()` conversion
            final Object[] array;
            if ((array = stream.toArray()).length == 0) return Collections.emptyList();

            // Copying into the ArrayList is required as there is no guarantee that the returned array
            // will never get modified somewhere else.
            // While this may look like an overkill, this is required by the contract.
            return uncheckedListCast(Collections.unmodifiableList(new ArrayList<>(Arrays.asList(array))));
        }

        return uncheckedListCast((List<?>) TO_LIST__METHOD_HANDLE.invokeExact(stream));
    }

    /**
     * Casts the given list into a specific one.
     *
     * @param raw raw-types list
     * @param <T> exact wanted type of list elements
     * @return the provided list with its type case to the specific one
     *
     * @apiNote this is effectively no-op
     */
    // note: no nullability annotations are present on lists as cast of `null` is also safe
    @Contract("_ -> param1")
    @SuppressWarnings("unchecked")
    private static <T> List<T> uncheckedListCast(final List<?> raw) {
        return (List<T>) raw;
    }
}
