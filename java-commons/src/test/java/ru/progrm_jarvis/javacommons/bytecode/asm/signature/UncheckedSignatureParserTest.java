package ru.progrm_jarvis.javacommons.bytecode.asm.signature;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.signature.SignatureReader;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class UncheckedSignatureParserTest {

    private static @NotNull Stream<@NotNull Arguments> provideSignatures() {
        return Stream.of(
                arguments(
                        "<A:Ljava/lang/Object;B:TA;T:Ljava/lang/Exception;:Ljava/io/Serializable;"
                                + ":Ljava/util/function/Consumer<-[[[Ljava/util/function/Function<TA;"
                                + "Ljava/lang/Object;>;>;>Ljava/lang/Object;"
                                + "Ljava/util/function/Consumer<Ljava/lang/String;>;",
                        BaseTypeSignature.BOOLEAN /* FIXME */
                )
        );
    }

    @ParameterizedTest
    @MethodSource("provideSignatures")
    void testSignatureCorrectParsing(final @NotNull String rawSignature, final @NotNull Signature signature) {
        val parser = UncheckedBadlyDesignedSignatureParser.create();
        new SignatureReader(rawSignature).accept(parser);
        assertEquals(signature, parser.result().orElseGet(fail("parser did not produce any result")));
    }

}