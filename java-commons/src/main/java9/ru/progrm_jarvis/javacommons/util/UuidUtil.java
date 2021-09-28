package ru.progrm_jarvis.javacommons.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.UUID;

/**
 * Utility for {@link UUID}-related functionality.
 */
@UtilityClass
public class UuidUtil {

    /**
     * Amount of bytes from which the {@link UUID} consists.
     */
    private final int UUID_BYTES = Long.BYTES << 1;

    /**
     * VarHandle representing a {@code long[]}-view to a {@code byte[]} using {@link ByteOrder#BIG_ENDIAN big-endian}.
     */
    private final @NotNull VarHandle BYTE_ARRAY_LONG_VIEW_BIG_ENDIAN
            = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN);

    /**
     * Converts {@link UUID} into an array of 16 {@code byte}s.
     *
     * @param uuid UUID to convert
     * @return result of conversion as array of 16 bytes
     *
     * @see #uuidFromBytes(byte[]) for backward conversion
     */
    public byte[] uuidToBytes(final @NonNull UUID uuid) {
        // note: there is an Unsafe method for allocating a non-zeroed array, but it is hard to access it
        final byte[] bytes;
        BYTE_ARRAY_LONG_VIEW_BIG_ENDIAN.set(bytes = new byte[UUID_BYTES], 0, uuid.getMostSignificantBits());
        BYTE_ARRAY_LONG_VIEW_BIG_ENDIAN.set(bytes, Long.BYTES, uuid.getLeastSignificantBits());

        return bytes;
    }

    /**
     * Converts a 16-{@code byte}s array into a {@link UUID}.
     *
     * @param bytes array of 16 bytes to convert
     * @return result of conversion as UUID
     *
     * @see #uuidToBytes(UUID) for backward conversion
     */
    public UUID uuidFromBytes(final byte @NonNull [] bytes) {
        if (bytes.length != UUID_BYTES) throw new IllegalArgumentException(
                "Length of bytes length should be " + UUID_BYTES
        );

        return new UUID(
                (long) BYTE_ARRAY_LONG_VIEW_BIG_ENDIAN.get(bytes, 0),
                (long) BYTE_ARRAY_LONG_VIEW_BIG_ENDIAN.get(bytes, Long.BYTES)
        );
    }
}
