package ru.progrm_jarvis.ultimatemessenger.format.placeholder;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.progrm_jarvis.ultimatemessenger.format.model.SimpleTextModelFactory;
import ru.progrm_jarvis.ultimatemessenger.format.model.TextModelFactory;

import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class SimplePlaceholdersTest {

    private static final String UNKNOWN_VALUE_PLACEHOLDER = "?",
            ESCAPED_ID_PLACEHOLDER_VALUE_PLACEHOLDER = "Here could have been your ID";
    private Placeholders<Target> placeholders;
    private TextModelFactory<Target> modelFactory;

    @BeforeEach
    void setUp() {
        placeholders = SimplePlaceholders.<Target>builder()
                .handler("*", ((value, target) -> "#"))
                .handler("test", (value, target) -> {
                    switch (value) {
                        case "name": return target.name;
                        case "id": return Integer.toString(target.ordinal());
                        case "dynamic": {
                            val dynamicValue = ThreadLocalRandom.current().nextInt();
                            target.dynamicValue.set(dynamicValue);

                            return Integer.toString(dynamicValue);
                        }
                        case "{test:id}": return ESCAPED_ID_PLACEHOLDER_VALUE_PLACEHOLDER;
                        default: return UNKNOWN_VALUE_PLACEHOLDER;
                    }
                })
                .build();
        modelFactory = new SimpleTextModelFactory<>();
    }

    @Test
    void testFormatWithoutRegisteredPlaceholders() {
        // test against each value
        for (val target : Target.values()) {
            assertThat(placeholders.format("Hello world", target), equalTo("Hello world"));
            assertThat(placeholders.format("Hello my dear world", target), equalTo("Hello my dear world"));
            assertThat(placeholders.format("Hello, dear {", target), equalTo("Hello, dear {"));
            assertThat(placeholders.format("Hello, dear {.", target), equalTo("Hello, dear {."));
            assertThat(placeholders.format("Hello, dear {123.", target), equalTo("Hello, dear {123."));
            assertThat(placeholders.format("Hello, dear }", target), equalTo("Hello, dear }"));
            assertThat(placeholders.format("Hello, dear }.", target), equalTo("Hello, dear }."));
            assertThat(placeholders.format("Hello, dear }...", target), equalTo("Hello, dear }..."));
            assertThat(placeholders.format("Hello, dear 12}...", target), equalTo("Hello, dear 12}..."));
            assertThat(placeholders.format("Hello, dear {}", target), equalTo("Hello, dear {}"));
            assertThat(placeholders.format("Hello, dear {}...", target), equalTo("Hello, dear {}..."));
            assertThat(placeholders.format("{}", target), equalTo("{}"));
            assertThat(placeholders.format("{} hello", target), equalTo("{} hello"));
            assertThat(placeholders.format("{:} hello", target), equalTo("{:} hello"));
            assertThat(placeholders.format("hello {:}", target), equalTo("hello {:}"));
            assertThat(placeholders.format("{:} hello {:}", target), equalTo("{:} hello {:}"));
            assertThat(placeholders.format("{rand} hello {:}", target), equalTo("??? hello {:}"));
            assertThat(placeholders.format("{rand} hello {rand}", target), equalTo("??? hello ???"));
            assertThat(placeholders.format("{rand} hello {:}", target), equalTo("??? hello {:}"));
            assertThat(placeholders.format("{unknown:} hello {:}", target), equalTo("??? hello {:}"));
        }
    }

    @Test
    void testFormatWithSinglePlaceholder() {
        // test against each value
        for (val target : Target.values()) {
            assertThat(
                    placeholders.format("Hello world {test:name}", target),
                    equalTo("Hello world " + target.name)
            );
            assertThat(
                    placeholders.format("Hello world {test:id}", target),
                    equalTo("Hello world " + target.ordinal())
            );
            assertThat(
                    placeholders.format("Hello world {test:dynamic}", target),
                    equalTo("Hello world " + target.dynamicValue.get())
            );
            assertThat(
                    placeholders.format("Hello world {test:wut}", target),
                    equalTo("Hello world " + UNKNOWN_VALUE_PLACEHOLDER)
            );
        }
    }

    @Test
    void testFormatWithMultiplePlaceholders() {
        // test against each value
        for (val target : Target.values()) {
            assertThat(
                    placeholders.format("Hello {test:name} at {test:id}", target),
                    equalTo("Hello " + target.name + " at " + target.ordinal())
            );
            assertThat(
                    placeholders.format("Hello world {test:id}@{test:dynamic}", target),
                    equalTo("Hello world " + target.ordinal() + '@' + target.dynamicValue.get())
            );
            assertThat(
                    placeholders.format("Hello world {test:id}@{test:dynamic} (I know you, {test:name})", target),
                    equalTo("Hello world " + target.ordinal() + '@' + target.dynamicValue.get()
                            + " (I know you, " + target.name + ')')
            );
        }
    }

    @Test
    void testFormatWithoutRegisteredPlaceholdersAndEscaping() {
        // test against each value
        for (val target : Target.values()) {
            assertThat(placeholders.format("Hello \\\\world", target), equalTo("Hello \\world"));
            assertThat(placeholders.format("Hello my dear world\\\\", target), equalTo("Hello my dear world\\"));
            assertThat(placeholders.format("\\\\Hello, dear {", target), equalTo("\\Hello, dear {"));
            assertThat(placeholders.format("Hello, dear \\\\{.", target), equalTo("Hello, dear \\{."));
            assertThat(placeholders.format("He\\llo, dear {123.", target), equalTo("Hello, dear {123."));
            assertThat(placeholders.format("Hel\\lo, de\\ar \\}\\", target), equalTo("Hello, dear }\\"));
            assertThat(placeholders.format("Hello, dea\\r }.\\", target), equalTo("Hello, dea\r }.\\"));
            assertThat(placeholders.format("Hell\\o, dear }...", target), equalTo("Hello, dear }..."));
            assertThat(placeholders.format("Hello,\ndear 12}...", target), equalTo("Hello,\ndear 12}..."));
            assertThat(placeholders.format("Hello,\\\\ndear {}", target), equalTo("Hello,\\ndear {}"));
            assertThat(placeholders.format("Hello, dear \\{}...", target), equalTo("Hello, dear {}..."));
            assertThat(placeholders.format("\\{\\}", target), equalTo("{}"));
            assertThat(placeholders.format("\\{\\}\\ \\h\\e\\l\\l\\o", target), equalTo("{} hello"));
            assertThat(placeholders.format("{\\:} hello", target), equalTo("{\\:} hello"));
            assertThat(placeholders.format("hello \\{:}\\", target), equalTo("hello {:}\\"));
            assertThat(placeholders.format("\\{\\:\\}\\ hello \\{\\:\\}", target), equalTo("{:} hello {:}"));
            assertThat(placeholders.format("\\{\\:\\}\\ hello \\{\\:\\}\\", target), equalTo("{:} hello {:}\\"));
            assertThat(placeholders.format("{\\ran\\d} hello {:}", target), equalTo("??? hello {:}"));
            assertThat(placeholders.format("{rand\\} \\hello {rand}", target), equalTo("???"));
            assertThat(placeholders.format("{rand\\} hello \\{:}", target), equalTo("???"));
            assertThat(placeholders.format("{unknown:\\} hi\\\\ {:}", target), equalTo("???"));
            assertThat(placeholders.format("{unknown:\\\\} <magic", target), equalTo("??? <magic"));
        }
    }

    @Test
    void testFormatWithSinglePlaceholderAndEscaping() {
        // test against each value
        for (val target : Target.values()) {
            assertThat(
                    placeholders.format("Hello world {test:name}", target),
                    equalTo("Hello world " + target.name)
            );
            assertThat(
                    placeholders.format("Hello world {test:id}", target),
                    equalTo("Hello world " + target.ordinal())
            );
            assertThat(
                    placeholders.format("Hello world {test:dynamic}", target),
                    equalTo("Hello world " + target.dynamicValue.get())
            );
            assertThat(
                    placeholders.format("Hello world {test:wut}", target),
                    equalTo("Hello world " + UNKNOWN_VALUE_PLACEHOLDER)
            );
        }
    }

    @Test
    void testFormatWithMultiplePlaceholdersAndEscaping() {
        // test against each value
        for (val target : Target.values()) {
            assertThat(
                    placeholders.format("Hello {test:name} at {test:id}", target),
                    equalTo("Hello " + target.name + " at " + target.ordinal())
            );
            assertThat(
                    placeholders.format("Hello world {test:id}@{test:dynamic}", target),
                    equalTo("Hello world " + target.ordinal() + '@' + target.dynamicValue.get())
            );
            assertThat(
                    placeholders.format("Hello world {test:id}@{test:dynamic} (I know you, {test:name})", target),
                    equalTo("Hello world " + target.ordinal() + '@' + target.dynamicValue.get()
                            + " (I know you, " + target.name + ')')
            );
        }
    }

    @Test
    void testFormatWithEscapedPlaceholders() {
        // test against each value
        for (val target : Target.values()) {
            assertThat(
                    placeholders.format("Hello world {test:name} but not \\{test:name}", target),
                    equalTo("Hello world " + target.name + " but not {test:name}")
            );
            assertThat(
                    placeholders.format("Raw value in raw value: \\{test:\\{test:id}}", target),
                    equalTo("Raw value in raw value: {test:{test:id}}")
            );
            assertThat(
                    placeholders.format("Non-raw value in raw value: \\{test:{test:id}}", target),
                    equalTo("Non-raw value in raw value: {test:" + target.ordinal() + "}")
            );
            assertThat(
                    placeholders.format("Raw value in non-raw value: {test:\\{test:id\\}}", target),
                    equalTo("Raw value in non-raw value: " + ESCAPED_ID_PLACEHOLDER_VALUE_PLACEHOLDER)
            );
            assertThat(
                    placeholders.format("Raw value in non-raw value: {test:\\{test:ordinal\\}}", target),
                    equalTo("Raw value in non-raw value: " + UNKNOWN_VALUE_PLACEHOLDER)
            );
        }
    }

    @Test
    void testFactoryWithSingleCharacterPlaceholderName() {
        // test against each value
        for (val target : Target.values()) {
            assertThat(placeholders.format("Foo{*}Bar", target), equalTo("Foo#Bar"));
            assertThat(placeholders.format("Foo{*}Bar{*}", target), equalTo("Foo#Bar#"));
            assertThat(placeholders.format("{*}Foo{*}Bar", target), equalTo("#Foo#Bar"));
            assertThat(placeholders.format("{*}Foo{*}Bar{*}", target), equalTo("#Foo#Bar#"));
            assertThat(placeholders.format("{*}{*}Foo{*}{*}Bar{*}{*}", target), equalTo("##Foo##Bar##"));
        }
    }

    @Test
    void testFactoryWithEscapedChars() {
        // test against each value
        for (val target : Target.values()) {
            assertThat(placeholders.format("Foo\\nBar", target), equalTo("Foo\nBar"));
            assertThat(placeholders.format("Foo\\nBar\\n", target), equalTo("Foo\nBar\n"));
            assertThat(placeholders.format("\\nFoo\\nBar", target), equalTo("\nFoo\nBar"));
            assertThat(placeholders.format("\\nFoo\\nBar\\n", target), equalTo("\nFoo\nBar\n"));
            assertThat(placeholders.format("\\n\\nFoo\\n\\nBar\\n\\n", target), equalTo("\n\nFoo\n\nBar\n\n"));
        }
    }

    @Test
    void testParseFactoryWithoutRegisteredPlaceholders() {
        // test against each value
        for (val target : Target.values()) {
            assertThat(
                    placeholders.parse(modelFactory, "Hello world").getText(target), equalTo("Hello world")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello my dear world").getText(target),
                    equalTo("Hello my dear world")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello, dear {").getText(target), equalTo("Hello, dear {")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello, dear {.").getText(target), equalTo("Hello, dear {.")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello, dear {123.").getText(target), equalTo("Hello, dear {123.")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello, dear }").getText(target), equalTo("Hello, dear }")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello, dear }.").getText(target), equalTo("Hello, dear }.")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello, dear }...").getText(target), equalTo("Hello, dear }...")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello, dear 12}...").getText(target),
                    equalTo("Hello, dear 12}...")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello, dear {}").getText(target), equalTo("Hello, dear {}")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello, dear {}...").getText(target), equalTo("Hello, dear {}...")
            );
            assertThat(
                    placeholders.parse(modelFactory, "{}").getText(target), equalTo("{}")
            );
            assertThat(
                    placeholders.parse(modelFactory, "{} hello").getText(target), equalTo("{} hello")
            );
            assertThat(
                    placeholders.parse(modelFactory, "{:} hello").getText(target), equalTo("{:} hello")
            );
            assertThat(
                    placeholders.parse(modelFactory, "hello {:}").getText(target), equalTo("hello {:}")
            );
            assertThat(
                    placeholders.parse(modelFactory, "{:} hello {:}").getText(target), equalTo("{:} hello {:}")
            );
            assertThat(
                    placeholders.parse(modelFactory, "{rand} hello {:}").getText(target), equalTo("??? hello {:}")
            );
            assertThat(
                    placeholders.parse(modelFactory, "{rand} hello {rand}").getText(target), equalTo("??? hello ???")
            );
            assertThat(
                    placeholders.parse(modelFactory, "{rand} hello {:}").getText(target), equalTo("??? hello {:}")
            );
            assertThat(
                    placeholders.parse(modelFactory, "{unknown:} hello {:}").getText(target), equalTo("??? hello {:}")
            );
        }
    }

    @Test
    void testParseFactoryWithSinglePlaceholder() {
        // test against each value
        for (val target : Target.values()) {
            assertThat(
                    placeholders.parse(modelFactory, "Hello world {test:name}").getText(target),
                    equalTo("Hello world " + target.name)
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello world {test:id}").getText(target),
                    equalTo("Hello world " + target.ordinal())
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello world {test:dynamic}").getText(target),
                    equalTo("Hello world " + target.dynamicValue.get())
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello world {test:wut}").getText(target),
                    equalTo("Hello world " + UNKNOWN_VALUE_PLACEHOLDER)
            );
        }
    }

    @Test
    void testParseFactoryWithMultiplePlaceholders() {
        // test against each value
        for (val target : Target.values()) {
            assertThat(
                    placeholders.parse(modelFactory, "Hello {test:name} at {test:id}").getText(target),
                    equalTo("Hello " + target.name + " at " + target.ordinal())
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello world {test:id}@{test:dynamic}").getText(target),
                    equalTo("Hello world " + target.ordinal() + '@' + target.dynamicValue.get())
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello world {test:id}@{test:dynamic} (I know you, {test:name})")
                            .getText(target),
                    equalTo("Hello world " + target.ordinal() + '@' + target.dynamicValue.get()
                            + " (I know you, " + target.name + ')')
            );
        }
    }

    @Test
    void testParseFactoryWithoutRegisteredPlaceholdersAndEscaping() {
        // test against each value
        for (val target : Target.values()) {
            assertThat(
                    placeholders.parse(modelFactory, "Hello \\\\world").getText(target), equalTo("Hello \\world")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello my dear world\\\\").getText(target),
                    equalTo("Hello my dear world\\")
            );
            assertThat(
                    placeholders.parse(modelFactory, "\\\\Hello, dear {").getText(target),
                    equalTo("\\Hello, dear {")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello, dear \\\\{.").getText(target),
                    equalTo("Hello, dear \\{.")
            );
            assertThat(
                    placeholders.parse(modelFactory, "He\\llo, dear {123.").getText(target),
                    equalTo("Hello, dear {123.")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hel\\lo, de\\ar \\}\\").getText(target),
                    equalTo("Hello, dear }\\")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello, dea\\r }.\\").getText(target),
                    equalTo("Hello, dea\r }.\\")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hell\\o, dear }...").getText(target),
                    equalTo("Hello, dear }...")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello,\ndear 12}...").getText(target),
                    equalTo("Hello,\ndear 12}...")
            );
            assertThat(placeholders.parse(modelFactory, "Hello,\\\\ndear {}").getText(target),
                    equalTo("Hello,\\ndear {}")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello, dear \\{}...").getText(target),
                    equalTo("Hello, dear {}...")
            );
            assertThat(
                    placeholders.parse(modelFactory, "\\{\\}").getText(target), equalTo("{}")
            );
            assertThat(
                    placeholders.parse(modelFactory, "\\{\\}\\ \\h\\e\\l\\l\\o").getText(target),
                    equalTo("{} hello")
            );
            assertThat(
                    placeholders.parse(modelFactory, "{\\:} hello").getText(target), equalTo("{\\:} hello")
            );
            assertThat(
                    placeholders.parse(modelFactory, "hello \\{:}\\").getText(target), equalTo("hello {:}\\")
            );
            assertThat(
                    placeholders.parse(modelFactory, "\\{\\:\\}\\ hello \\{\\:\\}").getText(target),
                    equalTo("{:} hello {:}")
            );
            assertThat(
                    placeholders.parse(modelFactory, "\\{\\:\\}\\ hello \\{\\:\\}\\").getText(target),
                    equalTo("{:} hello {:}\\")
            );
            assertThat(
                    placeholders.parse(modelFactory, "{\\ran\\d} hello {:}").getText(target),
                    equalTo("??? hello {:}")
            );
            assertThat(
                    placeholders.parse(modelFactory, "{rand\\} \\hello {rand}").getText(target), equalTo("???")
            );
            assertThat(
                    placeholders.parse(modelFactory, "{rand\\} hello \\{:}").getText(target), equalTo("???")
            );
            assertThat(
                    placeholders.parse(modelFactory, "{unknown:\\} hi\\\\ {:}").getText(target), equalTo("???")
            );
            assertThat(
                    placeholders.parse(modelFactory, "{unknown:\\\\} <magic").getText(target),
                    equalTo("??? <magic")
            );
        }
    }

    @Test
    void testParseFactoryWithSinglePlaceholderAndEscaping() {
        // test against each value
        for (val target : Target.values()) {
            assertThat(
                    placeholders.parse(modelFactory, "Hello world {test:name}").getText(target),
                    equalTo("Hello world " + target.name)
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello world {test:id}").getText(target),
                    equalTo("Hello world " + target.ordinal())
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello world {test:dynamic}").getText(target),
                    equalTo("Hello world " + target.dynamicValue.get())
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello world {test:wut}").getText(target),
                    equalTo("Hello world " + UNKNOWN_VALUE_PLACEHOLDER)
            );
        }
    }

    @Test
    void testParseFactoryWithMultiplePlaceholdersAndEscaping() {
        // test against each value
        for (val target : Target.values()) {
            assertThat(
                    placeholders.parse(modelFactory, "Hello {test:name} at {test:id}").getText(target),
                    equalTo("Hello " + target.name + " at " + target.ordinal())
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello world {test:id}@{test:dynamic}").getText(target),
                    equalTo("Hello world " + target.ordinal() + '@' + target.dynamicValue.get())
            );
            assertThat(
                    placeholders.parse(modelFactory, "Hello world {test:id}@{test:dynamic} (I know you, {test:name})")
                            .getText(target),
                    equalTo("Hello world " + target.ordinal() + '@' + target.dynamicValue.get()
                            + " (I know you, " + target.name + ')')
            );
        }
    }

    @Test
    void testParseFactoryWithEscapedPlaceholders() {
        // test against each value
        for (val target : Target.values()) {
            assertThat(
                    placeholders.parse(modelFactory, "Hello world {test:name} but not \\{test:name}").getText(target),
                    equalTo("Hello world " + target.name + " but not {test:name}")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Raw value in raw value: \\{test:\\{test:id}}").getText(target),
                    equalTo("Raw value in raw value: {test:{test:id}}")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Non-raw value in raw value: \\{test:{test:id}}").getText(target),
                    equalTo("Non-raw value in raw value: {test:" + target.ordinal() + "}")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Raw value in non-raw value: {test:\\{test:id\\}}")
                            .getText(target),
                    equalTo("Raw value in non-raw value: " + ESCAPED_ID_PLACEHOLDER_VALUE_PLACEHOLDER)
            );
            assertThat(
                    placeholders.parse(modelFactory, "Raw value in non-raw value: {test:\\{test:ordinal\\}}")
                            .getText(target),
                    equalTo("Raw value in non-raw value: " + UNKNOWN_VALUE_PLACEHOLDER)
            );
        }
    }

    @Test
    void testParseFactoryWithSingleCharacterPlaceholderName() {
        // test against each value
        for (val target : Target.values()) {
            assertThat(
                    placeholders.parse(modelFactory, "Foo{*}Bar").getText(target),
                    equalTo("Foo#Bar")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Foo{*}Bar{*}").getText(target),
                    equalTo("Foo#Bar#")
            );
            assertThat(
                    placeholders.parse(modelFactory, "{*}Foo{*}Bar").getText(target),
                    equalTo("#Foo#Bar")
            );
            assertThat(
                    placeholders.parse(modelFactory, "{*}Foo{*}Bar{*}").getText(target),
                    equalTo("#Foo#Bar#")
            );
            assertThat(
                    placeholders.parse(modelFactory, "{*}{*}Foo{*}{*}Bar{*}{*}").getText(target),
                    equalTo("##Foo##Bar##")
            );
        }
    }

    @Test
    void testParseFactoryWithEscapedChars() {
        // test against each value
        for (val target : Target.values()) {
            assertThat(
                    placeholders.parse(modelFactory, "Foo\\nBar").getText(target),
                    equalTo("Foo\nBar")
            );
            assertThat(
                    placeholders.parse(modelFactory, "Foo\\nBar\\n").getText(target),
                    equalTo("Foo\nBar\n")
            );
            assertThat(
                    placeholders.parse(modelFactory, "\\nFoo\\nBar").getText(target),
                    equalTo("\nFoo\nBar")
            );
            assertThat(
                    placeholders.parse(modelFactory, "\\nFoo\\nBar\\n").getText(target),
                    equalTo("\nFoo\nBar\n")
            );
            assertThat(
                    placeholders.parse(modelFactory, "\\n\\nFoo\\n\\nBar\\n\\n").getText(target),
                    equalTo("\n\nFoo\n\nBar\n\n")
            );
        }
    }

    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    enum Target {
        FOO("Foo"),
        BAR("Bar"),
        BAZ("baz");
        @NonNull String name;
        private ThreadLocal<Integer> dynamicValue = new ThreadLocal<>();
    }
}