package ru.progrm_jarvis.padla.tools.unsafemethodsaccessgenerator;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UnsafeMethodData {

    private static final @NotNull Locale LOCALE = Locale.ENGLISH;
    private static final @NotNull Pattern CAMEL_CASE_SPLIT_PATTERN = Pattern.compile("(?=\\p{Upper})");

    @NonNull Method method;
    @NonNull String upperCamelCaseName;
    @NonNull String upperCamelCaseNameWithTypeInfo;
    @NonNull String[] signature;
    @NonNull String returnType;
    boolean returnValue;

    public static UnsafeMethodData from(final @NonNull Method method) {
        val upperCamelCaseName = camelCaseToUpperSnakeCase(method.getName());
        return new UnsafeMethodData(
                method, upperCamelCaseName,
                appendParameterNames(upperCamelCaseName, method), getSignature(method.getParameterTypes()),
                getTypeName(method.getReturnType()),
                method.getReturnType() != void.class
        );
    }

    private static String camelCaseToUpperSnakeCase(final @NotNull CharSequence methodName) {
        val words =  CAMEL_CASE_SPLIT_PATTERN.split(methodName);

        val length = words.length;
        assert length != 0;
        if (length == 1) return words[0].toUpperCase(LOCALE);

        val result = new StringBuilder(methodName.length() + length - 1)
                .append(words[0]);

        for (var i = 1; i < length; i++) result.append('_').append(words[i]);

        return result.toString().toUpperCase(LOCALE);
    }

    private static String getTypeName(@NotNull Class<?> type) {
        var depth = 0;
        while (type.isArray()) {
            type = type.getComponentType();
            depth++;
        }
        if (depth == 0) return type.getSimpleName();

        val typeName = type.getSimpleName();
        val result = new StringBuilder(typeName.length() + depth * 2).append(typeName);
        for (var i = 0; i < depth; i++) result.append('[').append(']');

        return result.toString();
    }

    private static String[] getSignature(@SuppressWarnings("rawtypes") final @NotNull Class[] parameterTypes) {
        if (true) return Arrays.stream(parameterTypes)
                .map(UnsafeMethodData::getTypeName)
                .toArray(String[]::new);
        final int length;
        val signature = new String[length = parameterTypes.length];
        for (int i = 0; i < length; i++) {
            var parameterType = parameterTypes[i];

            final StringBuilder postfix = new StringBuilder();
            while (parameterType.isArray()) {
                postfix.append('[').append(']');
                parameterType = parameterType.getComponentType();
            }
            signature[i] = parameterType.getSimpleName() + postfix;
        }

        return signature;
    }

    private static String appendParameterNames(final @NotNull String string,
                                               final @NotNull Method method) {
        final int parameterCount = method.getParameterCount();
        if (parameterCount == 0) return string;

        val result = new StringBuilder(string.length() + 1 + parameterCount * 2 /* minimal growth*/)
                .append(string).append('_');
        for (var parameterType : method.getParameterTypes()) {
            result.append('_');
            var depth = 0;
            while (parameterType.isArray()) {
                ++depth;
                parameterType = parameterType.getComponentType();
            }
            if (parameterType.isPrimitive()) result.append('$');
            result.append(parameterType.getSimpleName().toUpperCase(LOCALE));
            for (var i = 0; i < depth; i++) result.append('$');
        }

        return result.toString();
    }
}
