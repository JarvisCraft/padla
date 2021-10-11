package ru.progrm_jarvis.javacommons.data;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.util.UuidUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.*;
import java.util.*;

/**
 * Common {@link DataSerializer data serializers}.
 */
@UtilityClass
public class DataSerializers {

    /* ********************************************** Primitive types ********************************************** */

    public @NotNull DataSerializer<@NotNull Boolean> booleanDataSerializer() {
        return BooleanDataSerializer.INSTANCE;
    }

    public @NotNull DataSerializer<@NotNull Byte> byteDataSerializer() {
        return ByteDataSerializer.INSTANCE;
    }

    public @NotNull DataSerializer<@NotNull Short> shortDataSerializer() {
        return ShortDataSerializer.INSTANCE;
    }

    public @NotNull DataSerializer<@NotNull Character> charDataSerializer() {
        return CharDataSerializer.INSTANCE;
    }

    public @NotNull DataSerializer<@NotNull Integer> intDataSerializer() {
        return IntDataSerializer.INSTANCE;
    }

    public @NotNull DataSerializer<@NotNull Long> longDataSerializer() {
        return LongDataSerializer.INSTANCE;
    }

    public @NotNull DataSerializer<@NotNull Float> floatDataSerializer() {
        return FloatDataSerializer.INSTANCE;
    }

    public @NotNull DataSerializer<@NotNull Double> doubleDataSerializer() {
        return DoubleDataSerializer.INSTANCE;
    }

    public @NotNull DataSerializer<@NotNull String> stringDataSerializer() {
        return StringDataSerializer.INSTANCE;
    }

    public @NotNull DataSerializer<@NotNull UUID> uuidDataSerializer() {
        return UuidDataSerializer.INSTANCE;
    }

    /* ************************************************ Collections ************************************************ */

    public <C extends Collection<T>, T> @NotNull DataSerializer<@NotNull C> collectionDataSerializer(
            final DataSerializers.@NonNull SizeAwareFactory<C> collectionFactory,
            final @NonNull DataSerializer<T> elementSerializer
    ) {
        return new CollectionDataSerializer<>(collectionFactory, elementSerializer);
    }

    public <T> @NotNull DataSerializer<@NotNull Collection<T>> collectionDataSerializer(
            final @NonNull DataSerializer<T> elementSerializer) {
        return collectionDataSerializer(ArrayList::new, elementSerializer);
    }

    public <T> @NotNull DataSerializer<@NotNull List<T>> listDataSerializer(
            final @NonNull DataSerializer<T> elementSerializer
    ) {
        return collectionDataSerializer(ArrayList::new, elementSerializer);
    }

    public <T> @NotNull DataSerializer<@NotNull Set<T>> setDataSerializer(
            final @NonNull DataSerializer<T> elementSerializer
    ) {
        return collectionDataSerializer(HashSet::new, elementSerializer);
    }

    public <M extends Map<K, V>, K, V> @NotNull DataSerializer<@NotNull M> mapDataSerializer(
            final DataSerializers.@NonNull SizeAwareFactory<M> mapFactory,
            final @NonNull DataSerializer<K> keySerializer,
            final @NonNull DataSerializer<V> valueSerializer
    ) {
        return new MapDataSerializer<>(mapFactory, keySerializer, valueSerializer);
    }

    /* *************************************************** Enums *************************************************** */

    public <E extends Enum<E>> DataSerializer<@NotNull E> namedEnumDataSerializer(final @NonNull Class<E> enumType) {
        final E[] enumValues;
        final int length;
        val map = new HashMap<String, E>(length = (enumValues = enumType.getEnumConstants()).length);
        for (var index = 0; index < length; index++) {
            final E value;
            map.put((value = enumValues[index]).name(), value);
        }

        return new NamedEnumDataSerializer<>(map);
    }

    public <E extends Enum<E>> DataSerializer<@NotNull E> ordinalEnumDataSerializer(final @NonNull Class<E> enumType) {
        final E[] enumConstants;
        final int enumConstantsLength;
        if ((enumConstantsLength = (enumConstants = enumType.getEnumConstants()).length)
                < 1 << 8) return new ByteOrdinalEnumDataSerializer<>(enumConstants);

        if (enumConstantsLength
                < 1 << 16) return new ShortOrdinalEnumDataSerializer<>(enumConstants);

        return new IntOrdinalEnumDataSerializer<>(enumConstants);
    }

    /* ************************************************ Date & Time ************************************************ */

    public @NotNull DataSerializer<@NotNull LocalDateTime> localDateTimeDataSerializer(
            final @NotNull ZoneOffset zoneOffset
    ) {
        return new LocalDateTimeDataSerializer(zoneOffset);
    }

    public @NotNull DataSerializer<@NotNull LocalDate> localDateDataSerializer() {
        return LocalDateDataSerializer.INSTANCE;
    }

    public @NotNull DataSerializer<@NotNull LocalTime> localTimeDataSerializer() {
        return LocalTimeDataSerializer.INSTANCE;
    }

    public @NotNull DataSerializer<@NotNull Instant> instantDataSerializer() {
        return InstantDataSerializer.INSTANCE;
    }

    /* ******************************************* Functional interfaces ******************************************* */

    @FunctionalInterface
    public interface SizeAwareFactory<C> {
        @NotNull C create(int initialSize);
    }

    /* ********************************************** Implementations ********************************************** */

    /* ********************************************** Primitive types ********************************************** */

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class BooleanDataSerializer implements DataSerializer<@NotNull Boolean> {

        private static final @NotNull DataSerializer<@NotNull Boolean> INSTANCE = new BooleanDataSerializer();

        @Override
        public void write(final @NotNull DataOutputStream out, final @NotNull Boolean value) throws IOException {
            out.writeBoolean(value);
        }

        @Override
        public @NotNull Boolean read(final @NotNull DataInputStream in) throws IOException {
            return in.readBoolean();
        }

        @Override
        public @NotNull Boolean fromByteArray(final byte @NotNull [] byteArray) throws IOException {
            if (byteArray.length != 1) throw new IOException("Byte array should be of length 1");

            return byteArray[0] != 0;
        }

        @Override
        public byte @NotNull [] toByteArray(final @NotNull Boolean value, final int expectedSize) {
            return toByteArray(value);
        }

        @Override
        public byte @NotNull [] toByteArray(final @NotNull Boolean value) {
            return new byte[]{value ? (byte) 1 : (byte) 0};
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ByteDataSerializer implements DataSerializer<@NotNull Byte> {

        private static final @NotNull DataSerializer<@NotNull Byte> INSTANCE = new ByteDataSerializer();

        @Override
        public void write(final @NotNull DataOutputStream out, final @NotNull Byte value) throws IOException {
            out.writeByte(value);
        }

        @Override
        public @NotNull Byte read(final @NotNull DataInputStream in) throws IOException {
            return in.readByte();
        }

        @Override
        public @NotNull Byte fromByteArray(final byte @NotNull [] byteArray) throws IOException {
            if (byteArray.length != 1) throw new IOException("Byte array should be of length 1");

            return byteArray[0];
        }

        @Override
        public byte @NotNull [] toByteArray(final @NotNull Byte value, final int expectedSize) {
            return toByteArray(value);
        }

        @Override
        public byte @NotNull [] toByteArray(final @NotNull Byte value) {
            return new byte[]{value};
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ShortDataSerializer implements DataSerializer<@NotNull Short> {

        private static final @NotNull DataSerializer<@NotNull Short> INSTANCE = new ShortDataSerializer();

        @Override
        public void write(final @NotNull DataOutputStream out, final @NotNull Short value) throws IOException {
            out.writeShort(value);
        }

        @Override
        public @NotNull Short read(final @NotNull DataInputStream in) throws IOException {
            return in.readShort();
        }

        @Override
        public @NotNull Short fromByteArray(final byte @NotNull [] byteArray) throws IOException {
            if (byteArray.length != 2) throw new IOException("Byte array should be of length 2");

            return (short) (byteArray[0] << 8 | byteArray[1] & 0xFF);
        }

        @Override
        public byte @NotNull [] toByteArray(final @NotNull Short value, final int expectedSize) {
            return toByteArray(value);
        }

        @Override
        public byte @NotNull [] toByteArray(final @NotNull Short value) {
            return new byte[]{(byte) (value >>> 8 & 0xFF), (byte) (value & 0xFF)};
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class CharDataSerializer implements DataSerializer<@NotNull Character> {

        private static final @NotNull DataSerializer<@NotNull Character> INSTANCE = new CharDataSerializer();

        @Override
        public void write(final @NotNull DataOutputStream out, final @NotNull Character value) throws IOException {
            out.writeChar(value);
        }

        @Override
        public @NotNull Character read(final @NotNull DataInputStream in) throws IOException {
            return in.readChar();
        }

        @Override
        public @NotNull Character fromByteArray(final byte @NotNull [] byteArray) throws IOException {
            if (byteArray.length != 2) throw new IOException("Byte array should be of length 2");

            return (char) (byteArray[0] << 8 | byteArray[1] & 0xFF);
        }

        @Override
        public byte @NotNull [] toByteArray(final @NotNull Character value, final int expectedSize) {
            return toByteArray(value);
        }

        @Override
        public byte @NotNull [] toByteArray(final @NotNull Character value) {
            return new byte[]{(byte) (value >>> 8 & 0xFF), (byte) (value & 0xFF)};
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class IntDataSerializer implements DataSerializer<@NotNull Integer> {

        private static final @NotNull DataSerializer<@NotNull Integer> INSTANCE = new IntDataSerializer();

        @Override
        public void write(final @NotNull DataOutputStream out, final @NotNull Integer value) throws IOException {
            out.writeInt(value);
        }

        @Override
        public @NotNull Integer read(final @NotNull DataInputStream in) throws IOException {
            return in.readInt();
        }

        @Override
        public @NotNull Integer fromByteArray(final byte @NotNull [] byteArray) throws IOException {
            if (byteArray.length != 4) throw new IOException("Byte array should be of length 4");

            return byteArray[0] << 24
                    | (byteArray[1] & 0xFF) << 16
                    | (byteArray[2] & 0xFF) << 8
                    | byteArray[3] & 0xFF;
        }

        @Override
        public byte @NotNull [] toByteArray(final @NotNull Integer value, final int expectedSize) {
            return toByteArray(value);
        }

        @Override
        public byte @NotNull [] toByteArray(final @NotNull Integer value) {
            return new byte[]{
                    (byte) (value >>> 24 & 0xFF),
                    (byte) (value >>> 16 & 0xFF),
                    (byte) (value >>> 8 & 0xFF),
                    (byte) (value & 0xFF)
            };
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class LongDataSerializer implements DataSerializer<@NotNull Long> {

        private static final @NotNull DataSerializer<@NotNull Long> INSTANCE = new LongDataSerializer();

        @Override
        public void write(final @NotNull DataOutputStream out, final @NotNull Long value) throws IOException {
            out.writeLong(value);
        }

        @Override
        public @NotNull Long read(final @NotNull DataInputStream in) throws IOException {
            return in.readLong();
        }

        @Override
        public @NotNull Long fromByteArray(final byte @NotNull [] byteArray) throws IOException {
            if (byteArray.length != 8) throw new IOException("Byte array should be of length 8");

            return (long) byteArray[0] << 56
                    | (long) (byteArray[1] & 0xFF) << 48
                    | (long) (byteArray[2] & 0xFF) << 40
                    | (long) (byteArray[3] & 0xFF) << 32
                    | (byteArray[4] & 0xFF) << 24
                    | (byteArray[5] & 0xFF) << 16
                    | (byteArray[6] & 0xFF) << 8
                    | byteArray[7] & 0xFF;
        }

        @Override
        public byte @NotNull [] toByteArray(final @NotNull Long value, final int expectedSize) {
            return toByteArray(value);
        }

        @Override
        public byte @NotNull [] toByteArray(final @NotNull Long value) {
            return new byte[]{
                    (byte) (value >>> 56 & 0xFF),
                    (byte) (value >>> 48 & 0xFF),
                    (byte) (value >>> 40 & 0xFF),
                    (byte) (value >>> 32 & 0xFF),
                    (byte) (value >>> 24 & 0xFF),
                    (byte) (value >>> 16 & 0xFF),
                    (byte) (value >>> 8 & 0xFF),
                    (byte) (value & 0xFF)
            };
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class FloatDataSerializer implements DataSerializer<@NotNull Float> {

        private static final @NotNull DataSerializer<@NotNull Float> INSTANCE = new FloatDataSerializer();

        private static final @NotNull DataSerializer<@NotNull Integer> INT_DATA_SERIALIZER = IntDataSerializer.INSTANCE;

        @Override
        public void write(final @NotNull DataOutputStream out, final @NotNull Float value) throws IOException {
            out.writeFloat(value);
        }

        @Override
        public @NotNull Float read(final @NotNull DataInputStream in) throws IOException {
            return in.readFloat();
        }

        @Override
        public @NotNull Float fromByteArray(final byte @NotNull [] byteArray) throws IOException {
            return Float.intBitsToFloat(INT_DATA_SERIALIZER.fromByteArray(byteArray));
        }

        @Override
        public byte @NotNull [] toByteArray(final @NotNull Float value, final int expectedSize) throws IOException {
            return toByteArray(value);
        }

        @Override
        public byte @NotNull [] toByteArray(final @NotNull Float value) throws IOException {
            return INT_DATA_SERIALIZER.toByteArray(Float.floatToIntBits(value));
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class DoubleDataSerializer implements DataSerializer<@NotNull Double> {

        private static final @NotNull DataSerializer<@NotNull Double> INSTANCE = new DoubleDataSerializer();

        private static final @NotNull DataSerializer<@NotNull Long> LONG_DATA_SERIALIZER = LongDataSerializer.INSTANCE;

        @Override
        public void write(final @NotNull DataOutputStream out, final @NotNull Double value) throws IOException {
            out.writeDouble(value);
        }

        @Override
        public @NotNull Double read(final @NotNull DataInputStream in) throws IOException {
            return in.readDouble();
        }

        @Override
        public @NotNull Double fromByteArray(final byte @NotNull [] byteArray) throws IOException {
            return Double.longBitsToDouble(LONG_DATA_SERIALIZER.fromByteArray(byteArray));
        }

        @Override
        public byte @NotNull [] toByteArray(final @NotNull Double value, final int expectedSize) throws IOException {
            return toByteArray(value);
        }

        @Override
        public byte @NotNull [] toByteArray(final @NotNull Double value) throws IOException {
            return LONG_DATA_SERIALIZER.toByteArray(Double.doubleToLongBits(value));
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class StringDataSerializer implements DataSerializer<@NotNull String> {

        private static final @NotNull DataSerializer<@NotNull String> INSTANCE = new StringDataSerializer();

        @Override
        public void write(final @NotNull DataOutputStream out, final @NotNull String string) throws IOException {
            out.writeUTF(string);
        }

        @Override
        public @NotNull String read(final @NotNull DataInputStream in) throws IOException {
            return in.readUTF();
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class UuidDataSerializer implements DataSerializer<@NotNull UUID> {

        private static final @NotNull DataSerializer<@NotNull UUID> INSTANCE = new UuidDataSerializer();

        @Override
        public void write(final @NotNull DataOutputStream out, final @NotNull UUID uuid) throws IOException {
            out.writeLong(uuid.getMostSignificantBits());
            out.writeLong(uuid.getLeastSignificantBits());
        }

        @Override
        public @NotNull UUID read(final @NotNull DataInputStream in) throws IOException {
            return new UUID(in.readLong(), in.readLong());
        }

        @Override
        public @NotNull UUID fromByteArray(final byte @NotNull [] byteArray) throws IOException {
            if (byteArray.length != UuidUtil.UUID_BYTES) throw new IOException("Byte array should be of length 16");

            return UuidUtil.uuidFromBytes(byteArray);
        }

        @Override
        public byte @NotNull [] toByteArray(final @NotNull UUID uuid, final int expectedSize) {
            return toByteArray(uuid);
        }

        @Override
        public byte @NotNull [] toByteArray(final @NotNull UUID uuid) {
            return UuidUtil.uuidToBytes(uuid);
        }
    }

    /* ************************************************ Collections ************************************************ */

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class CollectionDataSerializer<C extends Collection<T>, T>
            implements DataSerializer<@NotNull C> {

        DataSerializers.@NotNull SizeAwareFactory<C> collectionFactory;
        @NotNull DataSerializer<T> elementSerializer;

        @Override
        public void write(final @NotNull DataOutputStream out, final @NotNull C collection) throws IOException {
            out.writeInt(collection.size());
            for (val element : collection) elementSerializer.write(out, element);
        }

        @Override
        public @NotNull C read(final @NotNull DataInputStream in) throws IOException {
            final int size;
            val collection = collectionFactory.create(size = in.readInt());
            for (var i = 0; i < size; i++) collection.add(elementSerializer.read(in));

            return collection;
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class MapDataSerializer<M extends Map<K, V>, K, V> implements DataSerializer<@NotNull M> {

        DataSerializers.@NotNull SizeAwareFactory<M> mapFactory;
        @NotNull DataSerializer<K> keySerializer;
        @NotNull DataSerializer<V> valueSerializer;

        @Override
        public void write(final @NotNull DataOutputStream out, final @NotNull M map) throws IOException {
            final Set<Map.Entry<K, V>> entries;
            out.writeInt((entries = map.entrySet()).size());
            for (val entry : entries) {
                keySerializer.write(out, entry.getKey());
                valueSerializer.write(out, entry.getValue());
            }
        }

        @Override
        public @NotNull M read(final @NotNull DataInputStream in) throws IOException {
            final int size;
            val map = mapFactory.create(size = in.readInt());
            for (var i = 0; i < size; i++) map.put(keySerializer.read(in), valueSerializer.read(in));

            return map;
        }
    }

    /* *************************************************** Enums *************************************************** */

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class NamedEnumDataSerializer<E extends Enum<E>> implements DataSerializer<@NotNull E> {

        @NotNull Map<String, E> enumsByNames;

        @Override
        public void write(final @NotNull DataOutputStream out, final E element) throws IOException {
            out.writeUTF(element.name());
        }

        @Override
        public E read(final @NotNull DataInputStream in) throws IOException {
            final E value;
            {
                final String name;
                if ((value = enumsByNames.get(name = in.readUTF()))
                        == null) throw new IOException("Invalid enum constant name: " + name);
            }

            return value;
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class ByteOrdinalEnumDataSerializer<E extends Enum<E>> implements DataSerializer<@NotNull E> {

        @NotNull E @NotNull [] enumConstants;

        @Override
        public void write(final @NotNull DataOutputStream out, final @NotNull E element) throws IOException {
            out.writeByte(element.ordinal());
        }

        @Override
        public E read(final @NotNull DataInputStream in) throws IOException {
            final int enumConstantOrdinal;
            final E[] thisEnumConstants;
            if ((enumConstantOrdinal = in.readUnsignedByte())
                    >= (thisEnumConstants = enumConstants).length) throw new IOException(
                    "Enum constant ordinal (" + enumConstantOrdinal
                            + ") exceeds its limit (" + thisEnumConstants.length + ')'
            );

            return enumConstants[enumConstantOrdinal];
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class ShortOrdinalEnumDataSerializer<E extends Enum<E>> implements DataSerializer<@NotNull E> {

        @NotNull E @NotNull [] enumConstants;

        @Override
        public void write(final @NotNull DataOutputStream out, final E element) throws IOException {
            out.writeShort(element.ordinal());
        }

        @Override
        public E read(final @NotNull DataInputStream in) throws IOException {
            final int enumConstantOrdinal;
            final E[] thisEnumConstants;
            if ((enumConstantOrdinal = in.readUnsignedShort())
                    >= (thisEnumConstants = enumConstants).length) throw new IOException(
                    "Enum constant ordinal (" + enumConstantOrdinal
                            + ") exceeds its limit (" + thisEnumConstants.length + ')'
            );

            return enumConstants[enumConstantOrdinal];
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class IntOrdinalEnumDataSerializer<E extends Enum<E>> implements DataSerializer<@NotNull E> {

        @NotNull E @NotNull [] enumConstants;

        @Override
        public void write(final @NotNull DataOutputStream out, final E element) throws IOException {
            out.writeInt(element.ordinal());
        }

        @Override
        public E read(final @NotNull DataInputStream in) throws IOException {
            final int enumConstantOrdinal;
            final E[] thisEnumConstants;
            if ((enumConstantOrdinal = in.readInt()) // note: no need for un-signing as arrays cannot be as big
                    >= (thisEnumConstants = enumConstants).length) throw new IOException(
                    "Enum constant ordinal (" + enumConstantOrdinal
                            + ") exceeds its limit (" + thisEnumConstants.length + ')'
            );

            return enumConstants[enumConstantOrdinal];
        }
    }

    /* ************************************************ Date & Time ************************************************ */

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class InstantDataSerializer implements DataSerializer<@NotNull Instant> {

        private static final @NotNull DataSerializer<@NotNull Instant> INSTANCE = new InstantDataSerializer();

        @Override
        public void write(final @NotNull DataOutputStream out, final @NotNull Instant object) throws IOException {
            out.writeLong(object.toEpochMilli());
        }

        @Override
        public @NotNull Instant read(final @NotNull DataInputStream in) throws IOException {
            return Instant.ofEpochMilli(in.readLong());
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class LocalDateTimeDataSerializer implements DataSerializer<@NotNull LocalDateTime> {

        @NotNull ZoneOffset zoneOffset;

        @Override
        public void write(final @NotNull DataOutputStream out, final @NotNull LocalDateTime object) throws IOException {
            out.writeLong(object.toEpochSecond(zoneOffset));
            out.writeInt(object.getNano());
        }

        @Override
        public @NotNull LocalDateTime read(final @NotNull DataInputStream in) throws IOException {
            return LocalDateTime.ofEpochSecond(in.readLong(), in.readInt(), zoneOffset);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class LocalTimeDataSerializer implements DataSerializer<@NotNull LocalTime> {

        private static final @NotNull DataSerializer<@NotNull LocalTime> INSTANCE = new LocalTimeDataSerializer();

        @Override
        public void write(final @NotNull DataOutputStream out, final @NotNull LocalTime object) throws IOException {
            out.writeLong(object.toNanoOfDay());
        }

        @Override
        public @NotNull LocalTime read(final @NotNull DataInputStream in) throws IOException {
            return LocalTime.ofNanoOfDay(in.readLong());
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class LocalDateDataSerializer implements DataSerializer<@NotNull LocalDate> {

        private static final @NotNull DataSerializer<@NotNull LocalDate> INSTANCE = new LocalDateDataSerializer();

        @Override
        public void write(final @NotNull DataOutputStream out, final @NotNull LocalDate object) throws IOException {
            out.writeLong(object.toEpochDay());
        }

        @Override
        public @NotNull LocalDate read(final @NotNull DataInputStream in) throws IOException {
            return LocalDate.ofEpochDay(in.readLong());
        }
    }
}
