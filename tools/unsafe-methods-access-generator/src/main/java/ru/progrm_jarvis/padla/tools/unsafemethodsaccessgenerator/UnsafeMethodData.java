package ru.progrm_jarvis.padla.tools.unsafemethodsaccessgenerator;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Pattern;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UnsafeMethodData {

    private static final Pattern CAMEL_CASE_SPLIT_PATTERN = Pattern.compile("(?=\\p{Upper})");

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

    private static String camelCaseToUpperSnakeCase(@NotNull final String methodName) {
        val words =  CAMEL_CASE_SPLIT_PATTERN.split(methodName);
        val length = words.length;

        assert length != 0;
        if (length == 1) return words[0].toUpperCase();

        val result = new StringBuilder(methodName.length() + length - 1)
                .append(words[0]);
        for (var i = 1; i < words.length; i++) result.append('_').append(words[i]);

        return result.toString().toUpperCase();
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

    private static String[] getSignature(@SuppressWarnings("rawtypes") @NotNull final Class[] parameterTypes) {
        if (true) return Arrays.stream(parameterTypes)
                .map(UnsafeMethodData::getTypeName)
                .toArray(String[]::new);
        val signature = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            var parameterType = parameterTypes[i];

            final StringBuilder postfix = new StringBuilder();
            while (parameterType.isArray()) {
                postfix.append('[').append(']');
                parameterType = parameterType.getComponentType();
            }
            signature[i] = parameterType.getSimpleName() + postfix.toString();
        }

        return signature;
    }

    private static String appendParameterNames(@NotNull final String string,
                                               @NotNull final Method method) {
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
            result.append(parameterType.getSimpleName().toUpperCase());
            for (var i = 0; i < depth; i++) result.append('$');
        }

        return result.toString();
    }
}