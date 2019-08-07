package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class AsmTextModelFactoryTest {

    private static TextModelFactory<User> factory;

    @BeforeAll
    static void setUp() {
        factory = new AsmTextModelFactory<>();
    }

    @Test
    void testWithLongHandmadeString() {
        // this string is long so that I can check if the right bytecode operations are used
        // static part is 192 chars which is cool as I was not planning it, haha
        assertThat(
                factory.newTemplate().append(
                        "This is quite a long string which is here to test if I was right with"
                                + " passing integer to method, also it contains a dynamic part so that"
                                + " it does not get optimized to single static text model: ")
                        .append(User::getName)
                        .createAndRelease().getText(new User("Tester007", 12)),
                equalTo("This is quite a long string which is here to test if I was right with"
                        + " passing integer to method, also it contains a dynamic part so that"
                        + " it does not get optimized to single static text model: Tester007")
        );
    }

    @Test
    void testEmptyTemplate() {
        assertThat(factory.empty().getText(new User("Petro", 12)), equalTo(""));
        assertThat(factory.empty().getText(new User("Mikhail", 24)), equalTo(""));
        assertThat(factory.empty().getText(new User("Aleksey", 32)), equalTo(""));
    }

    @Test
    void testNotReusedTemplate() {
        var template = factory.newTemplate()
                .append("Hi")
                .append(" ")
                .append(User::getName)
                .append(" :)");

        var text = template.createAndRelease();
        assertThat(text.getText(new User("Alpha", 1)), equalTo("Hi Alpha :)"));
        assertThat(text.getText(new User("Beta", 2)), equalTo("Hi Beta :)"));
        assertThat(text.getText(new User("Gamma", 3)), equalTo("Hi Gamma :)"));

        template = factory.newTemplate()
                .append("qq ")
                .append(User::getName)
                .append(" \\")
                .append("o");

        text = template.createAndRelease();
        assertThat(text.getText(new User("Delta", -12)), equalTo("qq Delta \\o"));
        assertThat(text.getText(new User("Lambda", -27)), equalTo("qq Lambda \\o"));
        assertThat(text.getText(new User("Omega", -34)), equalTo("qq Omega \\o"));
    }

    @Test
    void testReusedTemplate() {
        val template = factory.newTemplate()
                .append("Hello")
                .append(" ")
                .append("World and ")
                .append(User::getName)
                .append("!");

        var text = template.create();
        assertThat(text.getText(new User("John", 8)), equalTo("Hello World and John!"));
        assertThat(text.getText(new User("Jack", 52)), equalTo("Hello World and Jack!"));
        assertThat(text.getText(new User("Daniel", 7)), equalTo("Hello World and Daniel!"));

        template.clear()
                .append("Mr. ")
                .append(User::getName)
                .append(" is")
                .append(" ")
                .append(user -> Integer.toString(user.getAge()))
                .append(" years old")
                .append(".");

        text = template.createAndRelease();
        assertThat(text.getText(new User("AbstractCoder", 18)), equalTo("Mr. AbstractCoder is 18 years old."));
        assertThat(text.getText(new User("PROgrm_JARvis", 17)), equalTo("Mr. PROgrm_JARvis is 17 years old."));
        assertThat(text.getText(new User("Tester", 17)), equalTo("Mr. Tester is 17 years old."));
    }

    @Test
    void testJustDynamicTemplate() {
        assertThat(
                factory.newTemplate()
                        .append(User::getName)
                        .createAndRelease()
                        .getText(new User("Petro", 12)),
                equalTo("Petro")
        );
        assertThat(
                factory.newTemplate()
                        .append(User::getName)
                        .append(user -> Integer.toString(user.getAge()))
                        .createAndRelease()
                        .getText(new User("Mikhail", 24)),
                equalTo("Mikhail24")
        );
    }

    @Test
    void testDynamicWithSingleCharTemplate() {
        assertThat(
                factory.newTemplate()
                        .append("Q")
                        .append(User::getName)
                        .createAndRelease()
                        .getText(new User("Petro", 12)),
                equalTo("QPetro")
        );
        assertThat(
                factory.newTemplate()
                        .append("Q")
                        .append(User::getName)
                        .append("AB")
                        .append(user -> Integer.toString(user.getAge()))
                        .append("C")
                        .createAndRelease()
                        .getText(new User("Mikhail", 24)),
                equalTo("QMikhailAB24C")
        );
    }

    @Test
    // This is an ASM specific test, because I use SIPUSH for all non-small char's while javac usese LDC dor some reason
    // I had to check if it javac who is wrong rather than me
    void testSingleCharDynamicTemplates() {
        val user = new User("John", 123);
        for (var character = Character.MIN_VALUE; true; character++) {
            val model = factory.newTemplate()
                    .append(Character.toString(character))
                    .append(User::getName)
                    .append(Character.toString(character))
                    .createAndRelease();
            assertThat(model.getText(user), equalTo(character + "John" + character));

            if (character == Character.MAX_VALUE) break;
        }
    }

    @Value
    @FieldDefaults(level = AccessLevel.PRIVATE)
    private static class User {

        @NonNull String name;
        int age;
    }
}