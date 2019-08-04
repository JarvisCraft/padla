package ru.progrm_jarvis.ultimatemessenger.format.util;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.reflector.invoke.InvokeUtil;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

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
     * Method handle for accessing {@link String}{@code .}{@value STRING_VALUE_FIELD_NAME} if it is possible
     */
    @Nullable private final MethodHandle STRING_VALUE_FIELD_GETTER_METHOD_HANDLE;
    /**
     * Marker indicating whether access to {@link String}{@code .}{@value STRING_VALUE_FIELD_NAME} is available
     */
    private final boolean STRING_VALUE_FIELD_AVAILABLE;

    static {
        fastStringToCharArray: {
            final Field stringValueField;
            try {
                stringValueField = String.class.getDeclaredField(STRING_VALUE_FIELD_NAME);
            } catch (final NoSuchFieldException e) {
                STRING_VALUE_FIELD_GETTER_METHOD_HANDLE = null;
                STRING_VALUE_FIELD_AVAILABLE = false;
                break fastStringToCharArray;
            } // field can't be found and so slower #toCharArray() is used
            STRING_VALUE_FIELD_GETTER_METHOD_HANDLE = InvokeUtil.toGetterMethodHandle(stringValueField);
            STRING_VALUE_FIELD_AVAILABLE = true;
        }
    }

    /**
     * Gets array of {@link String string} characters possibly returning
     * its internal {@value #STRING_VALUE_FIELD_NAME} field's value.
     *
     * @return array of {@link String string} characters possibly being the one used by {@code string} itself
     *
     * @apiNote modifications to the array may reflect on {@code string}
     */
    @SneakyThrows
    @SuppressWarnings("ConstantConditions") // null check goes by field STRING_VALUE_FIELD_AVAILABLE
    public char[] getStringChars(@NonNull final String string) {
        if (STRING_VALUE_FIELD_AVAILABLE) return (char[]) STRING_VALUE_FIELD_GETTER_METHOD_HANDLE.invokeExact(string);
        return string.toCharArray();
    }
}
