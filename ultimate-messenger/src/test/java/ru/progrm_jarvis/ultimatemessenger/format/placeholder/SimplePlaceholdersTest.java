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
import static org.junit.jupiter.params.provider.Arguments.arguments;

class SimplePlaceholdersTest {

    private static final String
            UNKNOWN_VALUE_PLACEHOLDER = "?",
            ESCAPED_ID_PLACEHOLDER_VALUE_PLACEHOLDER = "Here could have been your ID";

    private Placeholders<Target> placeholders;

    private TextModelFactory<Target> modelFactory;

    protected static Stream<Arguments> provideWithoutRegisteredPlaceholders() {
        return Arrays.stream(Target.values())
                .flatMap(target -> Stream.of(
                        arguments(target, "Hello world", "Hello world"),
                        arguments(target, "Hello my dear world", "Hello my dear world"),
                        arguments(target, "Hello, dear {", "Hello, dear {"),
                        arguments(target, "Hello, dear {.", "Hello, dear {."),
                        arguments(target, "Hello, dear {123.", "Hello, dear {123."),
                        arguments(target, "Hello, dear }", "Hello, dear }"),
                        arguments(target, "Hello, dear }.", "Hello, dear }."),
                        arguments(target, "Hello, dear }...", "Hello, dear }..."),
                        arguments(target, "Hello, dear 12}...", "Hello, dear 12}..."),
                        arguments(target, "Hello, dear {}", "Hello, dear {}"),
                        arguments(target, "Hello, dear {}...", "Hello, dear {}..."),
                        arguments(target, "{}", "{}"),
                        arguments(target, "{} hello", "{} hello"),
                        arguments(target, "{:} hello", "{:} hello"),
                        arguments(target, "hello {:}", "hello {:}"),
                        arguments(target, "{:} hello {:}", "{:} hello {:}"),
                        arguments(target, "{rand} hello {:}", "??? hello {:}"),
                        arguments(target, "{rand} hello {rand}", "??? hello ???"),
                        arguments(target, "{rand} hello {:}", "??? hello {:}"),
                        arguments(target, "{unknown:} hello {:}", "??? hello {:}")
                ));
    }

    protected static Stream<Arguments> provideWithSinglePlaceholder() {
        return Arrays.stream(Target.values())
                .flatMap(target -> Stream.of(
                        arguments(target, "Hello world {test:name}", "Hello world " + target.name),
                        arguments(target, "Hello world {test:id}", "Hello world " + target.ordinal()),
                        arguments(target, "Hello world {test:hash}", "Hello world " + target.hashCode()),
                        arguments(target, "Hello world {test:wut}", "Hello world " + UNKNOWN_VALUE_PLACEHOLDER)
                ));
    }

    protected static Stream<Arguments> provideWithMultiplePlaceholders() {
        return Arrays.stream(Target.values())
                .flatMap(target -> Stream.of(
                        arguments(
                                target, "Hello {test:name} at {test:id}",
                                "Hello " + target.name + " at " + target.ordinal()),
                        arguments(
                                target, "Hello world {test:id}@{test:hash}",
                                "Hello world " + target.ordinal() + '@' + target.hashCode()
                        ),
                        arguments(
                                target, "Hello world {test:id}@{test:hash} (I know you, {test:name})",
                                "Hello world " + target.ordinal() + '@' + target.hashCode()
                                        + " (I know you, " + target.name + ')'
                        )
                ));
    }

    protected static Stream<Arguments> provideWithoutRegisteredPlaceholdersAndEscaping() {
        return Arrays.stream(Target.values())
                .flatMap(target -> Stream.of(
                        arguments(target, "Hello \\\\world", "Hello \\world"),
                        arguments(target, "Hello my dear world\\\\", "Hello my dear world\\"),
                        arguments(target, "\\\\Hello, dear {", "\\Hello, dear {"),
                        arguments(target, "Hello, dear \\\\{.", "Hello, dear \\{."),
                        arguments(target, "He\\llo, dear {123.", "Hello, dear {123."),
                        arguments(target, "Hel\\lo, de\\ar \\}\\", "Hello, dear }\\"),
                        arguments(target, "Hello, dea\\r }.\\", "Hello, dea\r }.\\"),
                        arguments(target, "Hell\\o, dear }...", "Hello, dear }..."),
                        arguments(target, "Hello,\ndear 12}...", "Hello,\ndear 12}..."),
                        arguments(target, "Hello,\\\\ndear {}", "Hello,\\ndear {}"),
                        arguments(target, "Hello, dear \\{}...", "Hello, dear {}..."),
                        arguments(target, "\\{\\}", "{}"),
                        arguments(target, "\\{\\}\\ \\h\\e\\l\\l\\o", "{} hello"),
                        arguments(target, "{\\:} hello", "{\\:} hello"),
                        arguments(target, "hello \\{:}\\", "hello {:}\\"),
                        arguments(target, "\\{\\:\\}\\ hello \\{\\:\\}", "{:} hello {:}"),
                        arguments(target, "\\{\\:\\}\\ hello \\{\\:\\}\\", "{:} hello {:}\\"),
                        arguments(target, "{\\ran\\d} hello {:}", "??? hello {:}"),
                        arguments(target, "{rand\\} \\hello {rand}", "???"),
                        arguments(target, "{rand\\} hello \\{:}", "???"),
                        arguments(target, "{unknown:\\} hi\\\\ {:}", "???"),
                        arguments(target, "{unknown:\\\\} <magic", "??? <magic")
                ));
    }


    protected static Stream<Arguments> provideWithMultiplePlaceholdersAndEscaping() {
        return Arrays.stream(Target.values())
                .flatMap(target -> Stream.of(
                        arguments(
                                target, "Hello \\{test:name} at \\{test:id}",
                                "Hello {test:name} at {test:id}"
                        ),
                        arguments(
                                target, "Hello world \\{test:id}@{test:hash}",
                                "Hello world {test:id}@" + target.hashCode()
                        ),
                        arguments(
                                target, "Hello world {test:id}@\\{test:hash} (I know you, {test:name})",
                                "Hello world " + target.ordinal() + "@{test:hash} (I know you, " + target.name + ')'
                        ),
                        arguments(
                                target, "Hello \\\\{test:name} at {test:id}",
                                "Hello \\" + target.name + " at " + target.ordinal()
                        ),
                        arguments(
                                target, "Hello world {test:id}@\\\\{test:hash}",
                                "Hello world " + target.ordinal() + "@\\" + target.hashCode()
                        ),
                        arguments(
                                target, "Hello world \\\\{test:id}@\\\\{test:hash} (I know you, {test:name})",
                                "Hello world \\" + target.ordinal() + "@\\" + target.hashCode()
                                        + " (I know you, " + target.name + ')'
                        )
                ));
    }

    protected static Stream<Arguments> provideWithSinglePlaceholderAndEscaping() {
        return Arrays.stream(Target.values())
                .flatMap(target -> Stream.of(
                        arguments(target, "Hello world {test:name}", "Hello world " + target.name),
                        arguments(target, "Hello world {test:id}", "Hello world " + target.ordinal()),
                        arguments(target, "Hello world {test:hash}", "Hello world " + target.hashCode()),
                        arguments(target, "Hello world {test:wut}", "Hello world " + UNKNOWN_VALUE_PLACEHOLDER)
                ));
    }

    protected static Stream<Arguments> provideWithEscapedPlaceholders() {
        return Arrays.stream(Target.values())
                .flatMap(target -> Stream.of(
                        arguments(
                                target, "Hello world {test:name} but not \\{test:name}",
                                "Hello world " + target.name + " but not {test:name}"
                        ),
                        arguments(
                                target, "Raw value in raw value: \\{test:\\{test:id}}",
                                "Raw value in raw value: {test:{test:id}}"
                        ),
                        arguments(
                                target, "Non-raw value in raw value: \\{test:{test:id}}",
                                "Non-raw value in raw value: {test:" + target.ordinal() + "}"
                        ),
                        arguments(
                                target, "Raw value in non-raw value: {test:\\{test:id\\}}",
                                "Raw value in non-raw value: " + ESCAPED_ID_PLACEHOLDER_VALUE_PLACEHOLDER
                        ),
                        arguments(
                                target, "Raw value in non-raw value: {test:\\{test:ordinal\\}}",
                                "Raw value in non-raw value: " + UNKNOWN_VALUE_PLACEHOLDER
                        )
                ));
    }

    protected static Stream<Arguments> provideWithSingleCharacterPlaceholderName() {
        return Arrays.stream(Target.values())
                .flatMap(target -> Stream.of(
                        arguments(target, "Foo{*}Bar", "Foo#Bar"),
                        arguments(target, "Foo{*}Bar{*}", "Foo#Bar#"),
                        arguments(target, "{*}Foo{*}Bar", "#Foo#Bar"),
                        arguments(target, "{*}Foo{*}Bar{*}", "#Foo#Bar#"),
                        arguments(target, "{*}{*}Foo{*}{*}Bar{*}{*}", "##Foo##Bar##")
                ));
    }

    protected static Stream<Arguments> provideWithEscapedChars() {
        return Arrays.stream(Target.values())
                .flatMap(target -> Stream.of(
                        arguments(target, "Foo\\nBar", "Foo\nBar"),
                        arguments(target, "Foo\\nBar\\n", "Foo\nBar\n"),
                        arguments(target, "\\nFoo\\nBar", "\nFoo\nBar"),
                        arguments(target, "\\nFoo\\nBar\\n", "\nFoo\nBar\n"),
                        arguments(target, "\\n\\nFoo\\n\\nBar\\n\\n", "\n\nFoo\n\nBar\n\n")
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
        modelFactory = SimpleTextModelFactory.create();
    }

    @ParameterizedTest
    @MethodSource("provideWithoutRegisteredPlaceholders")
    void testFormatWithoutRegisteredPlaceholders(final @NotNull Target target,
                                                 final @NotNull String raw,
                                                 final @NotNull String formatted) {
        assertEquals(formatted, placeholders.format(raw, target));
    }

    @ParameterizedTest
    @MethodSource("provideWithSinglePlaceholder")
    void testFormatWithSinglePlaceholder(final @NotNull Target target,
                                         final @NotNull String raw,
                                         final @NotNull String formatted) {
        assertEquals(formatted, placeholders.format(raw, target));
    }

    @ParameterizedTest
    @MethodSource("provideWithMultiplePlaceholders")
    void testFormatWithMultiplePlaceholders(final @NotNull Target target,
                                            final @NotNull String raw,
                                            final @NotNull String formatted) {
        assertEquals(formatted, placeholders.format(raw, target));
    }

    @ParameterizedTest
    @MethodSource("provideWithoutRegisteredPlaceholdersAndEscaping")
    void testFormatWithoutRegisteredPlaceholdersAndEscaping(final @NotNull Target target,
                                                            final @NotNull String raw,
                                                            final @NotNull String formatted) {
        assertEquals(formatted, placeholders.format(raw, target));
    }

    @ParameterizedTest
    @MethodSource("provideWithSinglePlaceholderAndEscaping")
    void testFormatWithSinglePlaceholderAndEscaping(final @NotNull Target target,
                                                    final @NotNull String raw,
                                                    final @NotNull String formatted) {
        assertEquals(formatted, placeholders.format(raw, target));
    }

    @ParameterizedTest
    @MethodSource("provideWithMultiplePlaceholdersAndEscaping")
    void testFormatWithMultiplePlaceholdersAndEscaping(final @NotNull Target target,
                                                       final @NotNull String raw,
                                                       final @NotNull String formatted) {
        assertEquals(formatted, placeholders.format(raw, target));
    }

    @ParameterizedTest
    @MethodSource("provideWithEscapedPlaceholders")
    void testFormatWithEscapedPlaceholders(final @NotNull Target target,
                                           final @NotNull String raw,
                                           final @NotNull String formatted) {
        assertEquals(formatted, placeholders.format(raw, target));
    }

    @ParameterizedTest
    @MethodSource("provideWithSingleCharacterPlaceholderName")
    void testFormatWithSingleCharacterPlaceholderName(final @NotNull Target target,
                                                      final @NotNull String raw,
                                                      final @NotNull String formatted) {
        assertEquals(formatted, placeholders.format(raw, target));
    }

    @ParameterizedTest
    @MethodSource("provideWithEscapedChars")
    void testFormatWithEscapedChars(final @NotNull Target target,
                                    final @NotNull String raw,
                                    final @NotNull String formatted) {
        assertEquals(formatted, placeholders.format(raw, target));
    }

    /*  Factory  */

    @ParameterizedTest
    @MethodSource("provideWithoutRegisteredPlaceholders")
    void testFactoryParseWithoutRegisteredPlaceholders(final @NotNull Target target,
                                                       final @NotNull String raw,
                                                       final @NotNull String formatted) {
        assertEquals(formatted, placeholders.parse(modelFactory, raw).getText(target));
    }

    @ParameterizedTest
    @MethodSource("provideWithSinglePlaceholder")
    void testFactoryParseWithSinglePlaceholder(final @NotNull Target target,
                                               final @NotNull String raw,
                                               final @NotNull String formatted) {
        assertEquals(formatted, placeholders.parse(modelFactory, raw).getText(target));
    }

    @ParameterizedTest
    @MethodSource("provideWithMultiplePlaceholders")
    void testFactoryParseWithMultiplePlaceholders(final @NotNull Target target,
                                                  final @NotNull String raw,
                                                  final @NotNull String formatted) {
        assertEquals(formatted, placeholders.parse(modelFactory, raw).getText(target));
    }

    @ParameterizedTest
    @MethodSource("provideWithoutRegisteredPlaceholdersAndEscaping")
    void testFactoryParseWithoutRegisteredPlaceholdersAndEscaping(final @NotNull Target target,
                                                                  final @NotNull String raw,
                                                                  final @NotNull String formatted) {
        assertEquals(formatted, placeholders.parse(modelFactory, raw).getText(target));
    }

    @ParameterizedTest
    @MethodSource("provideWithSinglePlaceholderAndEscaping")
    void testFactoryParseWithSinglePlaceholderAndEscaping(final @NotNull Target target,
                                                          final @NotNull String raw,
                                                          final @NotNull String formatted) {
        assertEquals(formatted, placeholders.parse(modelFactory, raw).getText(target));
    }

    @ParameterizedTest
    @MethodSource("provideWithMultiplePlaceholdersAndEscaping")
    void testFactoryParseWithMultiplePlaceholdersAndEscaping(final @NotNull Target target,
                                                             final @NotNull String raw,
                                                             final @NotNull String formatted) {
        assertEquals(formatted, placeholders.parse(modelFactory, raw).getText(target));
    }

    @ParameterizedTest
    @MethodSource("provideWithEscapedPlaceholders")
    void testFactoryParseWithEscapedPlaceholders(final @NotNull Target target,
                                                 final @NotNull String raw,
                                                 final @NotNull String formatted) {
        assertEquals(formatted, placeholders.parse(modelFactory, raw).getText(target));
    }

    @ParameterizedTest
    @MethodSource("provideWithSingleCharacterPlaceholderName")
    void testFactoryParseWithSingleCharacterPlaceholderName(final @NotNull Target target,
                                                            final @NotNull String raw,
                                                            final @NotNull String formatted) {
        assertEquals(formatted, placeholders.parse(modelFactory, raw).getText(target));
    }

    @ParameterizedTest
    @MethodSource("provideWithEscapedChars")
    void testFactoryParseWithEscapedChars(final @NotNull Target target,
                                          final @NotNull String raw,
                                          final @NotNull String formatted) {
        assertEquals(formatted, placeholders.parse(modelFactory, raw).getText(target));
    }

    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private enum Target {
        FOO("Foo"),
        BAR("Bar"),
        BAZ("Baz");

        @NonNull String name;
    }
}