package ru.progrm_jarvis.javacommons.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Utility for {@link UUID}-related functionality.
 */
@UtilityClass
public class UuidUtil {

    /**
     * Amount of bytes from which the {@link UUID} consists.
     */
    private static final int UUID_BYTES = 16;

    /**
     * Converts {@link UUID} into an array of 16 {@code byte}s.
     *
     * @param uuid UUID to convert
     * @return result of conversion as array of 16 bytes
     *
     * @see #uuidFromBytes(byte[]) for backward conversion
     */
    public byte[] uuidToBytes(final @NonNull UUID uuid) {
        val buffer = ByteBuffer.wrap(new byte[UUID_BYTES]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());

        return buffer.array();
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

        val buffer = ByteBuffer.wrap(bytes);
        return new UUID(buffer.getLong(), buffer.getLong());
    }
}
