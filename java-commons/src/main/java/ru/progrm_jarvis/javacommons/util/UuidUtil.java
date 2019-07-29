package ru.progrm_jarvis.javacommons.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.nio.ByteBuffer;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Utility for {@link UUID}-related functionality.
 */
@UtilityClass
public class UuidUtil {

    /**
     * Converts {@link UUID} into an array of 16 {@link byte}s.
     *
     * @param uuid UUID to convert
     * @return result of conversion as array of 16 bytes
     *
     * @see #uuidFromBytes(byte[]) for backward conversion
     */
    public byte[] uuidToBytes(@NonNull final UUID uuid) {
        val buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());

        return buffer.array();
    }

    /**
     * Converts a 16-{@link byte}s array into a {@link UUID}.
     *
     * @param bytes array of 16 bytes to convert
     * @return result of conversion as UUID
     *
     * @see #uuidToBytes(UUID) for backward conversion
     */
    public UUID uuidFromBytes(@NonNull final byte[] bytes) {
        checkArgument(bytes.length == 16, "bytes length should be 16");

        val buffer = ByteBuffer.wrap(bytes);
        return new UUID(buffer.getLong(), buffer.getLong());
    }
}
