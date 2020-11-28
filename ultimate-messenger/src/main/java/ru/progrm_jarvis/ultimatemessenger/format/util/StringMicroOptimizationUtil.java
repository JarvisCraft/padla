package ru.progrm_jarvis.ultimatemessenger.format.util;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.javacommons.invoke.InvokeUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;

/**
 * This is a mostly internal utility (although useful externally) for specific {@link String string} operations.
 */
@UtilityClass
public class StringMicroOptimizationUtil {

    /**
     * Name of the field of class {@link String} which holds its chars (according to OpenJDK sources)
     */
    private final String STRING_VALUE_FIELD_NAME = "value";
    /**
     * Method handle for accessing {@link String}{@code .}{@value #STRING_VALUE_FIELD_NAME} if it is possible
     */
    private final @NotNull MethodHandle STRING_VALUE_FIELD_GETTER_METHOD_HANDLE;

    static {
        STRING_VALUE_FIELD_GETTER_METHOD_HANDLE = Arrays.stream(String.class.getDeclaredFields())
                .filter(field -> field.getName().equals(STRING_VALUE_FIELD_NAME) && field.getType() == char[].class)
                .findAny()
                .map(InvokeUtil::toGetterMethodHandle)
                .orElseGet(() -> {
                    val lookup = MethodHandles.publicLookup();
                    try {
                        return lookup.findVirtual(String.class, "toCharArray", MethodType.methodType(char[].class));
                    } catch (final NoSuchMethodException | IllegalAccessException e) {
                        throw new Error("java.lang.String.toCharArray() is unavailable", e);
                    }
                });
    }

    /**
     * Gets array of {@link String string} characters possibly returning
     * its internal {@value #STRING_VALUE_FIELD_NAME} field's value.
     *
     * @param string string whose chars should be got
     * @return array of {@link String string} characters possibly being the one used by {@code string} itself
     *
     * @apiNote modifications to the array may reflect on {@code string}
     */
    @SneakyThrows
    // null check goes by field STRING_VALUE_FIELD_AVAILABLE
    public char @NotNull [] getStringChars(final @NonNull String string) {
        return (char[]) STRING_VALUE_FIELD_GETTER_METHOD_HANDLE.invokeExact(string);
    }

    /**
     * Escaped the given {@link String} so that it can be inserted between {@code "}s
     * in Java code making this {@link String literal}'s value be equal to the source one.
     *
     * @param source source {@link String string}
     * @return valid value for copying into {@link String string literal} {@code "}s
     */
    public String escapeJavaStringLiteral(final @NonNull String source) {
        @Nullable StringBuilder result = null;
        val characters = getStringChars(source);
        int lastWriteIndex = -1;
        val length = characters.length;
        for (var index = 0; index < length; index++) {
            val character = characters[index];
            switch (character) {
                case '\t': {
                    (result == null
                            ? result = new StringBuilder(length + 1).append(source, 0, lastWriteIndex = index)
                            : result.append(source, lastWriteIndex + 1, lastWriteIndex = index))
                            .append('\\').append('t');
                    break;
                }
                case '\b': {
                    (result == null
                            ? result = new StringBuilder(length + 1).append(source, 0, lastWriteIndex = index)
                            : result.append(source, lastWriteIndex + 1, lastWriteIndex = index))
                            .append('\\').append('b');
                    break;
                }
                case '\n': {
                    (result == null
                            ? result = new StringBuilder(length + 1).append(source, 0, lastWriteIndex = index)
                            : result.append(source, lastWriteIndex + 1, lastWriteIndex = index))
                            .append('\\').append('n');
                    break;
                }
                case '\r': {
                    (result == null
                            ? result = new StringBuilder(length + 1).append(source, 0, lastWriteIndex = index)
                            : result.append(source, lastWriteIndex + 1, lastWriteIndex = index))
                            .append('\\').append('r');
                    break;
                }
                case '\f': {
                    (result == null
                            ? result = new StringBuilder(length + 1).append(source, 0, lastWriteIndex = index)
                            : result.append(source, lastWriteIndex + 1, lastWriteIndex = index))
                            .append('\\').append('f');
                    break;
                }
                case '"': {
                    (result == null
                            ? result = new StringBuilder(length + 1).append(source, 0, lastWriteIndex = index)
                            : result.append(source, lastWriteIndex + 1, lastWriteIndex = index))
                            .append('\\').append('"');
                    break;
                }
                case '\\': {
                    (result == null
                            ? result = new StringBuilder(length + 1).append(source, 0, lastWriteIndex = index)
                            : result.append(source, lastWriteIndex + 1, lastWriteIndex = index))
                            .append('\\').append('\\');
                    break;
                }
                default: {/* This character will be appended later */}
            }
        }

        return result == null ? source : result.append(source, lastWriteIndex + 1, length).toString();
    }

    /**
     * Escaped the given {@code char} so that it can be inserted between {@code '}s
     * in Java code making this {@code character} literal's value be equal to the source one.
     *
     * @param source source {@code char}
     * @return valid value for copying into {@code character} literal {@code '}s
     */
    public String escapeJavaCharacterLiteral(final char source) {
        switch (source) {
            case '\n': return "\\n";
            case '\r': return "\\r";
            case '\'': return "\\'";
            case '\\': return "\\\\";
            default: return Character.toString(source);
        }
    }
}
