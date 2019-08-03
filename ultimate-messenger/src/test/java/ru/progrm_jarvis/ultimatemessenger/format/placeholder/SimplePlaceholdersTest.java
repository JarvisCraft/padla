package ru.progrm_jarvis.ultimatemessenger.format.placeholder;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class SimplePlaceholdersTest {

    private static final String UNKNOWN_VALUE_PLACEHOLDER = "?",
            ESCAPED_ID_PLACEHOLDER_VALUE_PLACEHOLDER = "Here could have been your ID";
    private Placeholders<Target> placeholders = new SimplePlaceholders<>(new HashMap<>(), '{', '}', ':', '\\');

    @BeforeEach
    void setUp() {
        placeholders = new SimplePlaceholders<>(new HashMap<>(), '{', '}', ':', '\\');
        placeholders.add("test", (value, target) -> {
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
        });
    }

    @Test
    void testWithoutPlaceholders() {
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
            assertThat(placeholders.format("{rand} hello {:}", target), equalTo("{rand} hello {:}"));
            assertThat(placeholders.format("{rand} hello {rand}", target), equalTo("{rand} hello {rand}"));
            assertThat(placeholders.format("{rand} hello {:}", target), equalTo("{rand} hello {:}"));
            assertThat(placeholders.format("{unknown:} hello {:}", target), equalTo("{unknown:} hello {:}"));
        }
    }

    @Test
    void testWithSinglePlaceholder() {
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
    void testWithMultiplePlaceholders() {
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
    void testWithoutPlaceholdersAndEscaping() {
        // test against each value
        for (val target : Target.values()) {
            assertThat(placeholders.format("Hello \\\\world", target), equalTo("Hello \\world"));
            assertThat(placeholders.format("Hello my dear world\\\\", target), equalTo("Hello my dear world\\"));
            assertThat(placeholders.format("\\\\Hello, dear {", target), equalTo("\\Hello, dear {"));
            assertThat(placeholders.format("Hello, dear \\\\{.", target), equalTo("Hello, dear \\{."));
            assertThat(placeholders.format("He\\llo, dear {123.", target), equalTo("Hello, dear {123."));
            assertThat(placeholders.format("Hel\\lo, de\\ar \\}\\", target), equalTo("Hello, dear }\\"));
            assertThat(placeholders.format("Hello, dea\\r }.\\", target), equalTo("Hello, dear }.\\"));
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
            assertThat(placeholders.format("{\\ran\\d} hello {:}", target), equalTo("{\\ran\\d} hello {:}"));
            assertThat(placeholders.format("{rand\\} \\hello {rand}", target), equalTo("{rand\\} \\hello {rand}"));
            assertThat(placeholders.format("{rand\\} hello \\{:}", target), equalTo("{rand\\} hello \\{:}"));
            assertThat(placeholders.format("{unknown:\\} hi\\\\ {:}", target), equalTo("{unknown:\\} hi\\\\ {:}"));
            assertThat(placeholders.format("{unknown:\\\\} <magic", target), equalTo("{unknown:\\\\} <magic"));
        }
    }

    @Test
    void testWithSinglePlaceholderAndEscaping() {
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
    void testWithMultiplePlaceholdersAndEscaping() {
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
    void testWithEscapedPlaceholders() {
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