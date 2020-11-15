package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.NonNull;
import lombok.Value;
import lombok.val;
import lombok.var;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractTextModelFactoryTest {

    protected abstract @NotNull Stream<@NotNull TextModelFactory<@NotNull User>> provideTestSubjects();

    @ParameterizedTest
    @MethodSource("provideTestSubjects")
    void testWithLongHandmadeString(@NotNull final TextModelFactory<User> factory) {
        // this string is long so that I can check if the right bytecode operations are used
        // static part is 192 chars which is cool as I was not planning it, haha
        assertThat(
                factory.newBuilder().append(
                        "This is quite a long string which is here to test if I was right with"
                                + " passing integer to method, also it contains a dynamic part so that"
                                + " it does not get optimized to single static text model: ")
                        .append(User::getName)
                        .buildAndRelease().getText(new User("Tester007", 12)),
                equalTo("This is quite a long string which is here to test if I was right with"
                        + " passing integer to method, also it contains a dynamic part so that"
                        + " it does not get optimized to single static text model: Tester007")
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestSubjects")
    void testEmptyBuilder(@NotNull final TextModelFactory<User> factory) {
        assertThat(factory.empty().getText(new User("Petro", 12)), equalTo(""));
        assertThat(factory.empty().getText(new User("Mikhail", 24)), equalTo(""));
        assertThat(factory.empty().getText(new User("Aleksey", 32)), equalTo(""));
    }

    @ParameterizedTest
    @MethodSource("provideTestSubjects")
    void testNotReusedBuilder(@NotNull final TextModelFactory<User> factory) {
        var builder = factory.newBuilder()
                .append("Hi")
                .append(" ")
                .append(User::getName)
                .append(" :)");

        var text = builder.buildAndRelease();
        assertThat(text.getText(new User("Alpha", 1)), equalTo("Hi Alpha :)"));
        assertThat(text.getText(new User("Beta", 2)), equalTo("Hi Beta :)"));
        assertThat(text.getText(new User("Gamma", 3)), equalTo("Hi Gamma :)"));

        builder = factory.newBuilder()
                .append("qq ")
                .append(User::getName)
                .append(" \\")
                .append("o");

        text = builder.buildAndRelease();
        assertThat(text.getText(new User("Delta", -12)), equalTo("qq Delta \\o"));
        assertThat(text.getText(new User("Lambda", -27)), equalTo("qq Lambda \\o"));
        assertThat(text.getText(new User("Omega", -34)), equalTo("qq Omega \\o"));
    }

    @ParameterizedTest
    @MethodSource("provideTestSubjects")
    void testReusedBuilder(@NotNull final TextModelFactory<User> factory) {
        val builder = factory.newBuilder()
                .append("Hello")
                .append(" ")
                .append("World and ")
                .append(User::getName)
                .append("!");

        var text = builder.build();
        assertThat(text.getText(new User("John", 8)), equalTo("Hello World and John!"));
        assertThat(text.getText(new User("Jack", 52)), equalTo("Hello World and Jack!"));
        assertThat(text.getText(new User("Daniel", 7)), equalTo("Hello World and Daniel!"));

        builder.clear()
                .append("Mr. ")
                .append(User::getName)
                .append(" is")
                .append(" ")
                .append(user -> Integer.toString(user.getAge()))
                .append(" years old")
                .append(".");

        text = builder.buildAndRelease();
        assertThat(text.getText(new User("AbstractCoder", 18)), equalTo("Mr. AbstractCoder is 18 years old."));
        assertThat(text.getText(new User("PROgrm_JARvis", 17)), equalTo("Mr. PROgrm_JARvis is 17 years old."));
        assertThat(text.getText(new User("Tester", 17)), equalTo("Mr. Tester is 17 years old."));
    }

    @ParameterizedTest
    @MethodSource("provideTestSubjects")
    void testJustDynamicBuilder(@NotNull final TextModelFactory<User> factory) {
        assertThat(
                factory.newBuilder()
                        .append(User::getName)
                        .buildAndRelease()
                        .getText(new User("Petro", 12)),
                equalTo("Petro")
        );
        assertThat(
                factory.newBuilder()
                        .append(User::getName)
                        .append(user -> Integer.toString(user.getAge()))
                        .buildAndRelease()
                        .getText(new User("Mikhail", 24)),
                equalTo("Mikhail24")
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestSubjects")
    void testDynamicWithSingleCharBuilder(@NotNull final TextModelFactory<User> factory) {
        assertThat(
                factory.newBuilder()
                        .append("Q")
                        .append(User::getName)
                        .buildAndRelease()
                        .getText(new User("Petro", 12)),
                equalTo("QPetro")
        );
        assertThat(
                factory.newBuilder()
                        .append("Q")
                        .append(User::getName)
                        .append("AB")
                        .append(user -> Integer.toString(user.getAge()))
                        .append("C")
                        .buildAndRelease()
                        .getText(new User("Mikhail", 24)),
                equalTo("QMikhailAB24C")
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestSubjects")
        // This is an ASM specific test, because I use SIPUSH for all non-small char's while javac usese LDC dor some reason
        // I had to check if it javac who is wrong rather than me
    void testSingleCharDynamicBuilders(@NotNull final TextModelFactory<User> factory) {
        val user = new User("John", 123);
        for (var character = Character.MIN_VALUE; true; character++) {
            val model = factory.newBuilder()
                    .append(Character.toString(character))
                    .append(User::getName)
                    .append(Character.toString(character))
                    .buildAndRelease();
            assertThat(model.getText(user), equalTo(character + "John" + character));

            if (character == Character.MAX_VALUE) break;
        }
    }

    @ParameterizedTest
    @MethodSource("provideTestSubjects")
    void testBigTextModels(@NotNull final TextModelFactory<User> factory) {
        // 200 is currently the maximal amount of `StringConcatFactory#makeConcat[..]` dynamic arguments

        val user = new User("Japris", 8);

        for (val dynamicElementsCount : new int[]{200, 201, 202, 399, 400, 401, 599, 600, 601}) {
            val textModelBuilder = factory.newBuilder();

            val expectedString = new StringBuilder();
            for (var i = 0; i < dynamicElementsCount; i++) {
                val character = Character.toString((char) (i + 128));
                expectedString.append(character).append("_");

                textModelBuilder.append(target -> {
                    assertThat(target, Matchers.is(target));

                    return character;
                }).append("_");
            }

            assertThat(textModelBuilder.buildAndRelease().getText(user), equalTo(expectedString.toString()));
        }
    }

    @Value
    protected static class User {

        @NonNull String name;
        int age;
    }
}
