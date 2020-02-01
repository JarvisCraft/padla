package ru.progrm_jarvis.ultimatemessenger.format.placeholder;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.progrm_jarvis.ultimatemessenger.format.model.SimpleTextModelFactory;
import ru.progrm_jarvis.ultimatemessenger.format.model.TextModelFactory;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimplePlaceholdersTest {

    private static final String
            UNKNOWN_VALUE_PLACEHOLDER = "?",
            ESCAPED_ID_PLACEHOLDER_VALUE_PLACEHOLDER = "Here could have been your ID";

    private Placeholders<Target> placeholders;

    private TextModelFactory<Target> modelFactory;

    protected static Stream<Arguments> provideWithoutRegisteredPlaceholders() {
        return Arrays.stream(Target.values())
                .flatMap(target -> Stream.of(
                        Arguments.of(target, "Hello world", "Hello world"),
                        Arguments.of(target, "Hello my dear world", "Hello my dear world"),
                        Arguments.of(target, "Hello, dear {", "Hello, dear {"),
                        Arguments.of(target, "Hello, dear {.", "Hello, dear {."),
                        Arguments.of(target, "Hello, dear {123.", "Hello, dear {123."),
                        Arguments.of(target, "Hello, dear }", "Hello, dear }"),
                        Arguments.of(target, "Hello, dear }.", "Hello, dear }."),
                        Arguments.of(target, "Hello, dear }...", "Hello, dear }..."),
                        Arguments.of(target, "Hello, dear 12}...", "Hello, dear 12}..."),
                        Arguments.of(target, "Hello, dear {}", "Hello, dear {}"),
                        Arguments.of(target, "Hello, dear {}...", "Hello, dear {}..."),
                        Arguments.of(target, "{}", "{}"),
                        Arguments.of(target, "{} hello", "{} hello"),
                        Arguments.of(target, "{:} hello", "{:} hello"),
                        Arguments.of(target, "hello {:}", "hello {:}"),
                        Arguments.of(target, "{:} hello {:}", "{:} hello {:}"),
                        Arguments.of(target, "{rand} hello {:}", "??? hello {:}"),
                        Arguments.of(target, "{rand} hello {rand}", "??? hello ???"),
                        Arguments.of(target, "{rand} hello {:}", "??? hello {:}"),
                        Arguments.of(target, "{unknown:} hello {:}", "??? hello {:}")
                ));
    }

    protected static Stream<Arguments> provideWithSinglePlaceholder() {
        return Arrays.stream(Target.values())
                .flatMap(target -> Stream.of(
                        Arguments.of(target, "Hello world {test:name}", "Hello world " + target.name),
                        Arguments.of(target, "Hello world {test:id}", "Hello world " + target.ordinal()),
                        Arguments.of(target, "Hello world {test:hash}", "Hello world " + target.hashCode()),
                        Arguments.of(target, "Hello world {test:wut}", "Hello world " + UNKNOWN_VALUE_PLACEHOLDER)
                ));
    }

    protected static Stream<Arguments> provideWithMultiplePlaceholders() {
        return Arrays.stream(Target.values())
                .flatMap(target -> Stream.of(
                        Arguments.of(
                                target, "Hello {test:name} at {test:id}",
                                "Hello " + target.name + " at " + target.ordinal()),
                        Arguments.of(
                                target, "Hello world {test:id}@{test:hash}",
                                "Hello world " + target.ordinal() + '@' + target.hashCode()
                        ),
                        Arguments.of(
                                target, "Hello world {test:id}@{test:hash} (I know you, {test:name})",
                                "Hello world " + target.ordinal() + '@' + target.hashCode()
                                        + " (I know you, " + target.name + ')'
                        )
                ));
    }

    protected static Stream<Arguments> provideWithoutRegisteredPlaceholdersAndEscaping() {
        return Arrays.stream(Target.values())
                .flatMap(target -> Stream.of(
                        Arguments.of(target, "Hello \\\\world", "Hello \\world"),
                        Arguments.of(target, "Hello my dear world\\\\", "Hello my dear world\\"),
                        Arguments.of(target, "\\\\Hello, dear {", "\\Hello, dear {"),
                        Arguments.of(target, "Hello, dear \\\\{.", "Hello, dear \\{."),
                        Arguments.of(target, "He\\llo, dear {123.", "Hello, dear {123."),
                        Arguments.of(target, "Hel\\lo, de\\ar \\}\\", "Hello, dear }\\"),
                        Arguments.of(target, "Hello, dea\\r }.\\", "Hello, dea\r }.\\"),
                        Arguments.of(target, "Hell\\o, dear }...", "Hello, dear }..."),
                        Arguments.of(target, "Hello,\ndear 12}...", "Hello,\ndear 12}..."),
                        Arguments.of(target, "Hello,\\\\ndear {}", "Hello,\\ndear {}"),
                        Arguments.of(target, "Hello, dear \\{}...", "Hello, dear {}..."),
                        Arguments.of(target, "\\{\\}", "{}"),
                        Arguments.of(target, "\\{\\}\\ \\h\\e\\l\\l\\o", "{} hello"),
                        Arguments.of(target, "{\\:} hello", "{\\:} hello"),
                        Arguments.of(target, "hello \\{:}\\", "hello {:}\\"),
                        Arguments.of(target, "\\{\\:\\}\\ hello \\{\\:\\}", "{:} hello {:}"),
                        Arguments.of(target, "\\{\\:\\}\\ hello \\{\\:\\}\\", "{:} hello {:}\\"),
                        Arguments.of(target, "{\\ran\\d} hello {:}", "??? hello {:}"),
                        Arguments.of(target, "{rand\\} \\hello {rand}", "???"),
                        Arguments.of(target, "{rand\\} hello \\{:}", "???"),
                        Arguments.of(target, "{unknown:\\} hi\\\\ {:}", "???"),
                        Arguments.of(target, "{unknown:\\\\} <magic", "??? <magic")
                ));
    }


    protected static Stream<Arguments> provideWithMultiplePlaceholdersAndEscaping() {
        return Arrays.stream(Target.values())
                .flatMap(target -> Stream.of(
                        Arguments.of(target, "Hello \\\\world", "Hello \\world"),
                        Arguments.of(target, "Hello my dear world\\\\", "Hello my dear world\\"),
                        Arguments.of(target, "\\\\Hello, dear {", "\\Hello, dear {"),
                        Arguments.of(target, "Hello, dear \\\\{.", "Hello, dear \\{."),
                        Arguments.of(target, "He\\llo, dear {123.", "Hello, dear {123."),
                        Arguments.of(target, "Hel\\lo, de\\ar \\}\\", "Hello, dear }\\"),
                        Arguments.of(target, "Hello, dea\\r }.\\", "Hello, dea\r }.\\"),
                        Arguments.of(target, "Hell\\o, dear }...", "Hello, dear }..."),
                        Arguments.of(target, "Hello,\ndear 12}...", "Hello,\ndear 12}..."),
                        Arguments.of(target, "Hello,\\\\ndear {}", "Hello,\\ndear {}"),
                        Arguments.of(target, "Hello, dear \\{}...", "Hello, dear {}..."),
                        Arguments.of(target, "\\{\\}", "{}"),
                        Arguments.of(target, "\\{\\}\\ \\h\\e\\l\\l\\o", "{} hello"),
                        Arguments.of(target, "{\\:} hello", "{\\:} hello"),
                        Arguments.of(target, "hello \\{:}\\", "hello {:}\\"),
                        Arguments.of(target, "\\{\\:\\}\\ hello \\{\\:\\}", "{:} hello {:}"),
                        Arguments.of(target, "\\{\\:\\}\\ hello \\{\\:\\}\\", "{:} hello {:}\\"),
                        Arguments.of(target, "{\\ran\\d} hello {:}", "??? hello {:}"),
                        Arguments.of(target, "{rand\\} \\hello {rand}", "???"),
                        Arguments.of(target, "{rand\\} hello \\{:}", "???"),
                        Arguments.of(target, "{unknown:\\} hi\\\\ {:}", "???"),
                        Arguments.of(target, "{unknown:\\\\} <magic", "??? <magic")
                ));
    }

    protected static Stream<Arguments> provideWithSinglePlaceholderAndEscaping() {
        return Arrays.stream(Target.values())
                .flatMap(target -> Stream.of(
                        Arguments.of(
                                target, "Hello {test:name} at {test:id}",
                                "Hello " + target.name + " at " + target.ordinal()),
                        Arguments.of(
                                target, "Hello world {test:id}@{test:hash}",
                                "Hello world " + target.ordinal() + '@' + target.hashCode()
                        ),
                        Arguments.of(
                                target, "Hello world {test:id}@{test:hash} (I know you, {test:name})",
                                "Hello world " + target.ordinal() + '@'
                                        + target.hashCode() + " (I know you, " + target.name + ')'
                        )
                ));
    }

    protected static Stream<Arguments> provideWithEscapedPlaceholders() {
        return Arrays.stream(Target.values())
                .flatMap(target -> Stream.of(
                        Arguments.of(
                                target, "Hello world {test:name} but not \\{test:name}",
                                "Hello world " + target.name + " but not {test:name}"
                        ),
                        Arguments.of(
                                target, "Raw value in raw value: \\{test:\\{test:id}}",
                                "Raw value in raw value: {test:{test:id}}"
                        ),
                        Arguments.of(
                                target, "Non-raw value in raw value: \\{test:{test:id}}",
                                "Non-raw value in raw value: {test:" + target.ordinal() + "}"
                        ),
                        Arguments.of(
                                target, "Raw value in non-raw value: {test:\\{test:id\\}}",
                                "Raw value in non-raw value: " + ESCAPED_ID_PLACEHOLDER_VALUE_PLACEHOLDER
                        ),
                        Arguments.of(
                                target, "Raw value in non-raw value: {test:\\{test:ordinal\\}}",
                                "Raw value in non-raw value: " + UNKNOWN_VALUE_PLACEHOLDER
                        )
                ));
    }

    protected static Stream<Arguments> provideWithSingleCharacterPlaceholderName() {
        return Arrays.stream(Target.values())
                .flatMap(target -> Stream.of(
                        Arguments.of(target, "Foo{*}Bar", "Foo#Bar"),
                        Arguments.of(target, "Foo{*}Bar{*}", "Foo#Bar#"),
                        Arguments.of(target, "{*}Foo{*}Bar", "#Foo#Bar"),
                        Arguments.of(target, "{*}Foo{*}Bar{*}", "#Foo#Bar#"),
                        Arguments.of(target, "{*}{*}Foo{*}{*}Bar{*}{*}", "##Foo##Bar##")
                ));
    }

    protected static Stream<Arguments> provideWithEscapedChars() {
        return Arrays.stream(Target.values())
                .flatMap(target -> Stream.of(
                        Arguments.of(target, "Foo\\nBar", "Foo\nBar"),
                        Arguments.of(target, "Foo\\nBar\\n", "Foo\nBar\n"),
                        Arguments.of(target, "\\nFoo\\nBar", "\nFoo\nBar"),
                        Arguments.of(target, "\\nFoo\\nBar\\n", "\nFoo\nBar\n"),
                        Arguments.of(target, "\\n\\nFoo\\n\\nBar\\n\\n", "\n\nFoo\n\nBar\n\n")
                ));
    }

    @BeforeEach
    void setUp() {
        placeholders = SimplePlaceholders.<Target>builder().build();
        placeholders.add("*", ((value, target) -> "#"));
        placeholders.add("test", (value, target) -> {
            switch (value) {
                case "name": return target.name;
                case "id": return Integer.toString(target.ordinal());
                case "hash": return Integer.toString(target.hashCode());
                case "{test:id}": return ESCAPED_ID_PLACEHOLDER_VALUE_PLACEHOLDER;
                default: return UNKNOWN_VALUE_PLACEHOLDER;
            }
        });
        modelFactory = new SimpleTextModelFactory<>();
    }

    @ParameterizedTest
    @MethodSource("provideWithoutRegisteredPlaceholders")
    void testFormatWithoutRegisteredPlaceholders(@NotNull final Target target,
                                                 @NotNull final String raw,
                                                 @NotNull final String formatted) {
        assertEquals(formatted, placeholders.format(raw, target));
    }

    @ParameterizedTest
    @MethodSource("provideWithSinglePlaceholder")
    void testFormatWithSinglePlaceholder(@NotNull final Target target,
                                         @NotNull final String raw,
                                         @NotNull final String formatted) {
        assertEquals(formatted, placeholders.format(raw, target));
    }

    @ParameterizedTest
    @MethodSource("provideWithMultiplePlaceholders")
    void testFormatWithMultiplePlaceholders(@NotNull final Target target,
                                            @NotNull final String raw,
                                            @NotNull final String formatted) {
        assertEquals(formatted, placeholders.format(raw, target));
    }

    @ParameterizedTest
    @MethodSource("provideWithoutRegisteredPlaceholdersAndEscaping")
    void testFormatWithoutRegisteredPlaceholdersAndEscaping(@NotNull final Target target,
                                                            @NotNull final String raw,
                                                            @NotNull final String formatted) {
        assertEquals(formatted, placeholders.format(raw, target));
    }

    @ParameterizedTest
    @MethodSource("provideWithSinglePlaceholderAndEscaping")
    void testFormatWithSinglePlaceholderAndEscaping(@NotNull final Target target,
                                                    @NotNull final String raw,
                                                    @NotNull final String formatted) {
        assertEquals(formatted, placeholders.format(raw, target));
    }

    @ParameterizedTest
    @MethodSource("provideWithMultiplePlaceholdersAndEscaping")
    void testFormatWithMultiplePlaceholdersAndEscaping(@NotNull final Target target,
                                                       @NotNull final String raw,
                                                       @NotNull final String formatted) {
        assertEquals(formatted, placeholders.format(raw, target));
    }

    @ParameterizedTest
    @MethodSource("provideWithEscapedPlaceholders")
    void testFormatWithEscapedPlaceholders(@NotNull final Target target,
                                           @NotNull final String raw,
                                           @NotNull final String formatted) {
        assertEquals(formatted, placeholders.format(raw, target));
    }

    @ParameterizedTest
    @MethodSource("provideWithSingleCharacterPlaceholderName")
    void testFormatWithSingleCharacterPlaceholderName(@NotNull final Target target,
                                                      @NotNull final String raw,
                                                      @NotNull final String formatted) {
        assertEquals(formatted, placeholders.format(raw, target));
    }

    @ParameterizedTest
    @MethodSource("provideWithEscapedChars")
    void testFormatWithEscapedChars(@NotNull final Target target,
                                    @NotNull final String raw,
                                    @NotNull final String formatted) {
        assertEquals(formatted, placeholders.format(raw, target));
    }

    /*  Factory  */

    @ParameterizedTest
    @MethodSource("provideWithoutRegisteredPlaceholders")
    void testFactoryParseWithoutRegisteredPlaceholders(@NotNull final Target target,
                                                       @NotNull final String raw,
                                                       @NotNull final String formatted) {
        assertEquals(formatted, placeholders.parse(modelFactory, raw).getText(target));
    }

    @ParameterizedTest
    @MethodSource("provideWithSinglePlaceholder")
    void testFactoryParseWithSinglePlaceholder(@NotNull final Target target,
                                               @NotNull final String raw,
                                               @NotNull final String formatted) {
        assertEquals(formatted, placeholders.parse(modelFactory, raw).getText(target));
    }

    @ParameterizedTest
    @MethodSource("provideWithMultiplePlaceholders")
    void testFactoryParseWithMultiplePlaceholders(@NotNull final Target target,
                                                  @NotNull final String raw,
                                                  @NotNull final String formatted) {
        assertEquals(formatted, placeholders.parse(modelFactory, raw).getText(target));
    }

    @ParameterizedTest
    @MethodSource("provideWithoutRegisteredPlaceholdersAndEscaping")
    void testFactoryParseWithoutRegisteredPlaceholdersAndEscaping(@NotNull final Target target,
                                                                  @NotNull final String raw,
                                                                  @NotNull final String formatted) {
        assertEquals(formatted, placeholders.parse(modelFactory, raw).getText(target));
    }

    @ParameterizedTest
    @MethodSource("provideWithSinglePlaceholderAndEscaping")
    void testFactoryParseWithSinglePlaceholderAndEscaping(@NotNull final Target target,
                                                          @NotNull final String raw,
                                                          @NotNull final String formatted) {
        assertEquals(formatted, placeholders.parse(modelFactory, raw).getText(target));
    }

    @ParameterizedTest
    @MethodSource("provideWithMultiplePlaceholdersAndEscaping")
    void testFactoryParseWithMultiplePlaceholdersAndEscaping(@NotNull final Target target,
                                                             @NotNull final String raw,
                                                             @NotNull final String formatted) {
        assertEquals(formatted, placeholders.parse(modelFactory, raw).getText(target));
    }

    @ParameterizedTest
    @MethodSource("provideWithEscapedPlaceholders")
    void testFactoryParseWithEscapedPlaceholders(@NotNull final Target target,
                                                 @NotNull final String raw,
                                                 @NotNull final String formatted) {
        assertEquals(formatted, placeholders.parse(modelFactory, raw).getText(target));
    }

    @ParameterizedTest
    @MethodSource("provideWithSingleCharacterPlaceholderName")
    void testFactoryParseWithSingleCharacterPlaceholderName(@NotNull final Target target,
                                                            @NotNull final String raw,
                                                            @NotNull final String formatted) {
        assertEquals(formatted, placeholders.parse(modelFactory, raw).getText(target));
    }

    @ParameterizedTest
    @MethodSource("provideWithEscapedChars")
    void testFactoryParseWithEscapedChars(@NotNull final Target target,
                                          @NotNull final String raw,
                                          @NotNull final String formatted) {
        assertEquals(formatted, placeholders.parse(modelFactory, raw).getText(target));
    }

    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    enum Target {
        FOO("Foo"),
        BAR("Bar"),
        BAZ("Baz");

        @NonNull String name;
    }
}