package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TextModelTest {

    static Stream<Arguments> provideTestSubjects() {
        return Stream.of(
                arguments(TextModel.of("foo"), "foo"),
                arguments(TextModel.of("bar"), "bar"),
                arguments(TextModel.of("baz"), "baz"),
                arguments(TextModel.of("mr. user"), "mr. user"),
                arguments(TextModel.of("Hello world!"), "Hello world!"),
                arguments(
                        TextModel.of("Japris Pogrammer seems to be a coder"),
                        "Japris Pogrammer seems to be a coder"
                ),
                arguments(TextModel.of(""), "") // empty text is also text
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestSubjects")
    void testGetTextForNull(final @NotNull TextModel<User> textModel, final @NotNull String text) {
        assertThat(textModel.getText(null), equalTo(text));
    }

    @ParameterizedTest
    @MethodSource("provideTestSubjects")
    void testGetTextForNotNull(final @NotNull TextModel<User> textModel, final @NotNull String text) {
        assertThat(textModel.getText(new User("Jarvis", 5)), equalTo(text));
        assertThat(textModel.getText(new User("P(r)ogrammer", 255)), equalTo(text));
    }

    @ParameterizedTest
    @MethodSource("provideTestSubjects")
    void testMinLength(final @NotNull TextModel<User> textModel, final @NotNull String text) {
        assertThat(textModel.getMinLength(), is(text.length()));
    }

    @ParameterizedTest
    @MethodSource("provideTestSubjects")
    void testMaxLength(final @NotNull TextModel<User> textModel, final @NotNull String text) {
        assertThat(textModel.getMaxLength(), is(text.length()));
    }

    @Value
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static class User {
        @NonNull String name;
        int age;
    }
}