package ru.progrm_jarvis.ultimatemessenger;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import ru.progrm_jarvis.ultimatemessenger.format.StringFormatter;
import ru.progrm_jarvis.ultimatemessenger.format.model.AsmTextModelFactory;
import ru.progrm_jarvis.ultimatemessenger.format.model.SimpleTextModelFactory;
import ru.progrm_jarvis.ultimatemessenger.format.model.TextModel;
import ru.progrm_jarvis.ultimatemessenger.format.placeholder.Placeholders;
import ru.progrm_jarvis.ultimatemessenger.format.placeholder.SimplePlaceholders;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class PlaceholdersBenchmark {

    public static void main(@NonNull final String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().build()).run();
    }

    @Benchmark
    public void regex(final Blackhole blackhole,
                      final GenericConfiguration genericConfiguration,
                      final RegexConfiguration regexConfiguration) {
        for (val target : genericConfiguration.targets) {
            val matcher = regexConfiguration.placeholderPattern.matcher(regexConfiguration.rawText);

            val result = new StringBuffer(); // this is not okay, but hi Java 8 support
            while (matcher.find()) {
                matcher.appendReplacement(
                        result, regexConfiguration.formatters.get(matcher.group(1)).apply(matcher.group(2), target)
                );
            }
            matcher.appendTail(result);

            blackhole.consume(result.toString());
        }
    }

    @Benchmark
    public void simplePlaceholdersWithSimpleTmf(final Blackhole blackhole,
                                                final GenericConfiguration genericConfiguration,
                                                final SimpleTmfPlaceholdersConfiguration placeholdersConfiguration) {
        for (val target : genericConfiguration.targets) blackhole
                .consume(placeholdersConfiguration.text.getText(target));
    }

    @Benchmark
    public void simplePlaceholdersWithAsmWithScfTmf(final Blackhole blackhole,
                                                    final GenericConfiguration genericConfiguration,
                                                    final AsmTmfWithScfPlaceholdersConfiguration
                                                            placeholdersConfiguration) {
        for (val target : genericConfiguration.targets) blackhole
                .consume(placeholdersConfiguration.text.getText(target));
    }

    @Benchmark
    public void simplePlaceholdersWithAsmWithoutScfTmf(final Blackhole blackhole,
                                                       final GenericConfiguration genericConfiguration,
                                                       final AsmTmfWithoutScfPlaceholdersConfiguration
                                                                   placeholdersConfiguration) {
        for (val target : genericConfiguration.targets) blackhole
                .consume(placeholdersConfiguration.text.getText(target));
    }

    /**
     * Basic generated configuration.
     */
    @State(Scope.Benchmark)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class GenericConfiguration {

        /**
         * Mix of random {@link FormattingTarget formatting targets}
         */
        private final FormattingTarget[] targets = {
                new FormattingTarget("JARvis", "PROgrammer", 18, 0xCAFEBABE),
                new FormattingTarget("Julik", "Domino", 18, 0xCAFEBABE),
                new FormattingTarget("Magical", "Guy", 0x1D0C0, 0b1001010010),
                new FormattingTarget("Magical", "Guy", 0x1D0C0, 0b1001010010), // duplicate value put specially
                new FormattingTarget("John", "Five", 48, 10.0)
        };

        /**
         * Formatter for replacing placeholders related to {@link FormattingTarget}
         */
        private final StringFormatter<FormattingTarget> formatter = (text, target) -> {
            switch (text) {
                case "name": return target.getName();
                case "surname": return target.getSurname();
                case "age": return Integer.toString(target.getAge());
                case "score": return Double.toString(target.getScore());
                default: return "<?>";
            }
        };

        /**
         * Text which should be formatted
         */
        @Param({
                       "This is a simple string with no placeholders",
                       "This is a small string with some placeholders: {user:name} and {user:surname}",
                       "Text: {user:name}{user:surname} is known to be of age {user:age} and his score is {user:score}",
                       "Repeated: {user:name} (user:name) {user:score}{user:score} {user:age}&{user:surname}",
               })
        private String rawText;
    }

    @State(Scope.Benchmark)
    public static class RegexConfiguration {
        protected final Pattern placeholderPattern = Pattern.compile("\\{([^:}]?)(?::([^}]*))?}");
        protected String rawText;
        protected final Map<String, StringFormatter<FormattingTarget>> formatters = new HashMap<>();

        @Setup
        public void setup(final GenericConfiguration genericConfiguration) {
            rawText = genericConfiguration.rawText;
            formatters.put("user", genericConfiguration.formatter);
        }
    }

    @State(Scope.Benchmark)
    public static class PlaceholdersConfiguration {
        protected final Placeholders<FormattingTarget> placeholders
                = SimplePlaceholders.<FormattingTarget>builder().build();

        @Setup
        public void setup(final GenericConfiguration genericConfiguration) {
            placeholders.add("user", genericConfiguration.formatter);
        }
    }

    @State(Scope.Benchmark)
    public static class SimpleTmfPlaceholdersConfiguration {
        protected TextModel<FormattingTarget> text;

        @Setup
        public void setup(final GenericConfiguration genericConfiguration,
                          final PlaceholdersConfiguration placeholdersConfiguration) {
            text = placeholdersConfiguration.placeholders
                    .parse(SimpleTextModelFactory.get(), genericConfiguration.rawText);
        }
    }

    @State(Scope.Benchmark)
    public static class AsmTmfWithoutScfPlaceholdersConfiguration {
        protected TextModel<FormattingTarget> text;

        @Setup
        public void setup(final GenericConfiguration genericConfiguration,
                          final PlaceholdersConfiguration placeholdersConfiguration) {
            text = placeholdersConfiguration.placeholders.parse(AsmTextModelFactory.create(
                    AsmTextModelFactory.configuration().enableStringConcatFactory(false).build()
            ), genericConfiguration.rawText);
        }
    }

    @State(Scope.Benchmark)
    public static class AsmTmfWithScfPlaceholdersConfiguration {
        protected TextModel<FormattingTarget> text;

        @Setup
        public void setup(final GenericConfiguration genericConfiguration,
                          final PlaceholdersConfiguration placeholdersConfiguration) {
            text = placeholdersConfiguration.placeholders.parse(AsmTextModelFactory.create(
                    AsmTextModelFactory.configuration().enableStringConcatFactory(true).build()
            ), genericConfiguration.rawText);
        }
    }

    /**
     * This is a target used for formatting. It's methods should be used instead of direct field access to be more
     * realistic.
     */
    @Value
    @FieldDefaults(level = AccessLevel.PRIVATE)
    private static final class FormattingTarget {
        String name, surname;
        int age;
        // non-final field to allow more dynamic invocations
        @NonFinal double score;

        public double getScore() {
            return ++score;
        }
    }
}
