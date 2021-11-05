package ru.progrm_jarvis.javacommons.data;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.io.*;
import java.util.Optional;

/**
 * Serializer of arbitary data.
 *
 * @param <T> type of serialized data
 */
public interface DataSerializer<T> {

    /**
     * Writes the object into the given output stream.
     *
     * @param output output in which the output should be written
     * @param object object to be written into the output stream
     * @throws IOException if an error happens while writing
     */
    void write(@NotNull DataOutputStream output, T object) throws IOException;

    /**
     * Writes the object into the given output stream.
     *
     * @param output output in which the output should be written
     * @param object object to be written into the output stream
     * @implNote delegates to {@link #write(DataOutputStream, Object)}
     * @implSpec should not be overridden
     */
    @SneakyThrows(IOException.class)
    default void writeUnchecked(final @NotNull DataOutputStream output, final T object) {
        write(output, object);
    }

    /**
     * Reads the object from the given input stream.
     *
     * @param input input from which the object should be read
     * @return read object
     *
     * @throws IOException if an error happens while reading
     */
    T read(@NotNull DataInputStream input) throws IOException;

    /**
     * Reads the object from the given input stream.
     *
     * @param input input from which the object should be read
     * @return read object
     *
     * @implNote delegates to {@link #read(DataInputStream)}
     * @implSpec should not be overridden
     */
    @SneakyThrows(IOException.class)
    default T readUnchecked(final @NotNull DataInputStream input) {
        return read(input);
    }

    /**
     * Reads the object from the given byte-array.
     *
     * @param byteArray byte-array from which the object should be read
     * @return read object
     *
     * @throws IOException if an error happens while reading
     * @implNote default implementation simply creates a new {@link ByteArrayInputStream}
     * wrapped in {@link DataInputStream} and passes it to {@link #read(DataInputStream)}
     * @see #read(DataInputStream) stream equivalent of this method
     */
    default T fromByteArray(final byte @NotNull [] byteArray) throws IOException {
        // note: no need for buffering
        try (val input = new DataInputStream(new ByteArrayInputStream(byteArray))) {
            return read(input);
        }
    }

    /**
     * Reads the object from the given byte-array.
     *
     * @param byteArray byte-array from which the object should be read
     * @return read object
     *
     * @implNote default implementation simply creates a new {@link ByteArrayInputStream}
     * wrapped in {@link DataInputStream} and passes it to {@link #read(DataInputStream)}
     * @implNote delegates to {@link #fromByteArray(byte[])}
     * @implSpec should not be overridden
     * @see #read(DataInputStream) stream equivalent of this method
     */
    @SneakyThrows(IOException.class)
    default T fromByteArrayUnchecked(final byte @NotNull [] byteArray) {
        return fromByteArray(byteArray);
    }

    /**
     * Converts the given object into a byte-array.
     *
     * @param object object to be converted into a byte-array
     * @param expectedSize expected size of the created byte-array
     * @return byte-array representation of the object
     *
     * @throws IOException if an error happens while writing
     * @apiNote {@code expectedSize} is not required to actually be exact, it is just used for possible optimizations
     * @implNote default implementation simply creates a new {@link ByteArrayOutputStream}
     * wrapped in {@link DataOutputStream} and passes it to {@link #write(DataOutputStream, Object)}
     * @see #write(DataOutputStream, Object) stream equivalent of this method
     */
    default byte @NotNull [] toByteArray(
            final T object,
            final @Range(from = 1, to = Integer.MAX_VALUE) int expectedSize
    ) throws IOException {
        // note: no need for buffering
        try (val result = new ByteArrayOutputStream(expectedSize);
             val output = new DataOutputStream(result)) {
            write(output, object);
            output.flush();

            return result.toByteArray();
        }
    }

    /**
     * Converts the given object into a byte-array.
     *
     * @param object object to be converted into a byte-array
     * @return byte-array representation of the object
     *
     * @throws IOException if an error happens while writing
     * @apiNote alternative implementations may simply call to {@link #toByteArray(Object, int)} with specific size
     * @implNote default implementation simply creates a new {@link ByteArrayOutputStream}
     * wrapped in {@link DataOutputStream} and passes it to {@link #write(DataOutputStream, Object)}
     * @see #toByteArray(Object, int) variant accepting expected byte-array size
     * @see #write(DataOutputStream, Object) stream equivalent of this method
     */
    default byte @NotNull [] toByteArray(final T object) throws IOException {
        // note: no need for buffering
        try (val result = new ByteArrayOutputStream();
             val output = new DataOutputStream(result)) {
            write(output, object);
            output.flush();

            return result.toByteArray();
        }
    }

    /**
     * Converts the given object into a byte-array.
     *
     * @param object object to be converted into a byte-array
     * @param expectedSize expected size of the created byte-array
     * @return byte-array representation of the object
     *
     * @apiNote {@code expectedSize} is not required to actually be exact, it is just used for possible optimizations
     * @implNote default implementation simply creates a new {@link ByteArrayOutputStream}
     * wrapped in {@link DataOutputStream} and passes it to {@link #write(DataOutputStream, Object)}
     * @implNote delegates to {@link #toByteArray(Object, int)}
     * @implSpec should not be overridden
     * @see #write(DataOutputStream, Object) stream equivalent of this method
     */
    @SneakyThrows(IOException.class)
    default byte @NotNull [] toByteArrayUnchecked(
            final T object,
            final @Range(from = 1, to = Integer.MAX_VALUE) int expectedSize
    ) {
        return toByteArray(object, expectedSize);
    }

    /**
     * Converts the given object into a byte-array.
     *
     * @param object object to be converted into a byte-array
     * @return byte-array representation of the object
     *
     * @apiNote alternative implementations may simply call to {@link #toByteArray(Object, int)} with specific size
     * @implNote default implementation simply creates a new {@link ByteArrayOutputStream}
     * wrapped in {@link DataOutputStream} and passes it to {@link #write(DataOutputStream, Object)}
     * @implNote delegates to {@link #toByteArray(Object)}
     * @implSpec should not be overridden
     * @see #toByteArray(Object, int) variant accepting expected byte-array size
     * @see #write(DataOutputStream, Object) stream equivalent of this method
     */
    @SneakyThrows(IOException.class)
    default byte @NotNull [] toByteArrayUnchecked(final T object) {
        return toByteArray(object);
    }

    /**
     * Crates a data serializer based on this one which allows null values.
     *
     * @return null-friendly equivalent of this data serializer
     *
     * @see #optional() equivalent using {@link Optional} instead
     */
    default @NotNull DataSerializer<@Nullable T> nullable() {
        return new NullableDataSerializer<>(this);
    }

    /**
     * Crates a data serializer based on this one which allows {@link Optional optional} values.
     *
     * @return {@link Optional}-friendly equivalent of this data serializer
     *
     * @see #nullable() equivalent using nullable values instead
     */
    default @NotNull DataSerializer<@NotNull Optional<T>> optional() {
        return new OptionalDataSerializer<>(this);
    }

    /**
     * Data serializer for nullable types.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    final class NullableDataSerializer<T> implements DataSerializer<@Nullable T> {

        @NotNull DataSerializer<T> wrapped;

        @Override
        public void write(final @NotNull DataOutputStream output, final @Nullable T object) throws IOException {
            final boolean present;
            output.writeBoolean(present = object != null);
            if (present) wrapped.write(output, object);
        }

        @Override
        public @Nullable T read(final @NotNull DataInputStream input) throws IOException {
            return input.readBoolean() ? wrapped.read(input) : null;
        }
    }

    /**
     * Data serializer for {@link Optional optional} types.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    final class OptionalDataSerializer<T> implements DataSerializer<@NotNull Optional<T>> {

        @NotNull DataSerializer<T> wrapped;

        @Override
        public void write(final @NotNull DataOutputStream output,
                          final @NotNull Optional<T> object) throws IOException {
            final boolean present;
            output.writeBoolean(present = object.isPresent());
            if (present) wrapped.write(output, object.get());
        }

        @Override
        public @NotNull Optional<T> read(final @NotNull DataInputStream input) throws IOException {
            return input.readBoolean() ? Optional.of(wrapped.read(input)) : Optional.empty();
        }
    }
}
