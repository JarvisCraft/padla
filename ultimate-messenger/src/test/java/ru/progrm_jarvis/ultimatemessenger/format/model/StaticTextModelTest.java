package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

class StaticTextModelTest {

    static List<Arguments> provideTestSubjects() {
        return Arrays.asList(
                Arguments.of(StaticTextModel.of("foo"), "foo"),
                Arguments.of(StaticTextModel.of("bar"), "bar"),
                Arguments.of(StaticTextModel.of("baz"), "baz"),
                Arguments.of(StaticTextModel.of("mr. user"), "mr. user"),
                Arguments.of(StaticTextModel.of("Hello world!"), "Hello world!"),
                Arguments.of(
                        StaticTextModel.of("Japris Pogrammer seems to be a coder"),
                        "Japris Pogrammer seems to be a coder"
                ),
                Arguments.of(StaticTextModel.of(""), "") // empty text is also text
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestSubjects")
    void testGetTextForNull(@NotNull final TextModel<User> textModel, @NotNull final String text) {
        assertThat(textModel.getText(null), equalTo(text));
    }

    @ParameterizedTest
    @MethodSource("provideTestSubjects")
    void testGetTextForNotNull(@NotNull final TextModel<User> textModel, @NotNull final String text) {
        assertThat(textModel.getText(new User("Jarvis", 5)), equalTo(text));
        assertThat(textModel.getText(new User("P(r)ogrammer", 255)), equalTo(text));
    }

    @ParameterizedTest
    @MethodSource("provideTestSubjects")
    void testMinLength(@NotNull final TextModel<User> textModel, @NotNull final String text) {
        assertThat(textModel.getMinLength().orElseGet(() -> {
            fail("Min length is undefined");
            return 0;
        }), is(text.length()));
    }

    @ParameterizedTest
    @MethodSource("provideTestSubjects")
    void testMaxLength(@NotNull final TextModel<User> textModel, @NotNull final String text) {
        assertThat(textModel.getMaxLength().orElseGet(() -> {
            fail("Max length is undefined");
            return 0;
        }), is(text.length()));
    }

    @Value
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static class User {
        @NonNull String name;
        int age;
    }
}