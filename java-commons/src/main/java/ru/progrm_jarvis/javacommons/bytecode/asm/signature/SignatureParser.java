package ru.progrm_jarvis.javacommons.bytecode.asm.signature;

import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.object.Result;

public interface SignatureParser {

    @NotNull Result<@NotNull ClassSignature, ?> parseClassSignature(@NotNull String signature);

    @NotNull Result<@NotNull MethodSignature, ?> parseMethodSignature(@NotNull String signature);

    @NotNull Result<@NotNull TypeSignature, ?> parseTypeSignature(@NotNull String signature);
}
