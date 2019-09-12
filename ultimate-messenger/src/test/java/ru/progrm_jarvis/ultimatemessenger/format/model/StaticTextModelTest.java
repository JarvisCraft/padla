package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.progrm_jarvis.javacommons.pair.Pair;
import ru.progrm_jarvis.javacommons.pair.SimplePair;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

class StaticTextModelTest {

    private static List<Pair<TextModel<User>, String>> provideTextModels() {
        return Arrays.asList(
                SimplePair.of(StaticTextModel.of("foo"), "foo"),
                SimplePair.of(StaticTextModel.of("bar"), "bar"),
                SimplePair.of(StaticTextModel.of("baz"), "baz"),
                SimplePair.of(StaticTextModel.of("mr. user"), "mr. user"),
                SimplePair.of(StaticTextModel.of("Hello world!"), "Hello world!"),
                SimplePair.of(
                        StaticTextModel.of("Japris Pogrammer seems to be a coder"),
                        "Japris Pogrammer seems to be a coder"
                ),
                SimplePair.of(StaticTextModel.of(""), "") // empty text is also text
        );
    }

    @ParameterizedTest
    @MethodSource("provideTextModels")
    void testGetTextForNull(@NotNull final Pair<TextModel<User>, String> testTarget) {
        assertThat(testTarget.getFirst().getText(null), equalTo(testTarget.getSecond()));
    }

    @ParameterizedTest
    @MethodSource("provideTextModels")
    void testGetTextForNotNull(@NotNull final Pair<TextModel<User>, String> testTarget) {
        assertThat(testTarget.getFirst().getText(new User("Jarvis", 5)), equalTo(testTarget.getSecond()));
        assertThat(testTarget.getFirst().getText(new User("P(r)ogrammer", 255)), equalTo(testTarget.getSecond()));
    }

    @ParameterizedTest
    @MethodSource("provideTextModels")
    void testMinLength(@NotNull final Pair<TextModel<User>, String> testTarget) {
        assertThat(testTarget.getFirst().getMinLength().orElseGet(() -> {
            fail("Min length is undefined");
            return 0;
        }), is(testTarget.getSecond().length()));
    }

    @ParameterizedTest
    @MethodSource("provideTextModels")
    void testMaxLength(@NotNull final Pair<TextModel<User>, String> testTarget) {
        assertThat(testTarget.getFirst().getMaxLength().orElseGet(() -> {
            fail("Max length is undefined");
            return 0;
        }), is(testTarget.getSecond().length()));
    }

    @Value
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static class User {
        @NonNull String name;
        int age;
    }
}