package ru.progrm_jarvis.ultimatemessenger;

import lombok.NonNull;
import lombok.val;
import lombok.var;
import org.jetbrains.annotations.Nullable;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import ru.progrm_jarvis.javacommons.pair.Pair;
import ru.progrm_jarvis.javacommons.pair.SimplePair;
import ru.progrm_jarvis.ultimatemessenger.format.model.AsmTextModelFactory;
import ru.progrm_jarvis.ultimatemessenger.format.model.SimpleTextModelFactory;
import ru.progrm_jarvis.ultimatemessenger.format.model.TextModel;
import ru.progrm_jarvis.ultimatemessenger.format.placeholder.Placeholders;
import ru.progrm_jarvis.ultimatemessenger.format.placeholder.SimplePlaceholders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Threads(Threads.MAX)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class StaticPlaceholderBenchmark {

    public static void main(@NonNull final String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().build()).run();
    }

    @Benchmark
    public void stringReplace(final Blackhole blackhole,
                              final Configuration configuration,
                              final StringReplaceConfiguration stringReplaceConfiguration) {
        for (var text : stringReplaceConfiguration.texts) {
            for (val replacement : configuration.replacements.entrySet()) text
                    = text.replace(replacement.getKey(), replacement.getValue());

            blackhole.consume(text);
        }
    }

    @Benchmark
    public void regex(final Blackhole blackhole,
                      final Configuration configuration,
                      final RegexConfiguration regexConfiguration) {
        for (val text : regexConfiguration.texts) {
            val matcher = regexConfiguration.placeholderPattern.matcher(text);

            val result = new StringBuffer(); // this is not okay, but hi Java 8 support
            while (matcher.find()) {
                val group = matcher.group(1);
                matcher.appendReplacement(result, configuration.replacements.get(group));
            }
            matcher.appendTail(result);

            blackhole.consume(result.toString());
        }
    }

    @Benchmark
    public void simplePlaceholdersWithSimpleTmf(final Blackhole blackhole,
                                                final SimpleTmfPlaceholdersConfiguration placeholdersConfiguration) {
        for (val text : placeholdersConfiguration.texts) blackhole.consume(text.getText(null));
    }

    @Benchmark
    public void simplePlaceholdersWithAsmTmf(final Blackhole blackhole,
                                             final AsmTmfPlaceholdersConfiguration placeholdersConfiguration) {
        for (val text : placeholdersConfiguration.texts) blackhole.consume(text.getText(null));
    }

    /**
     * Basic generated configuration.
     */
    @State(Scope.Benchmark)
    public static class Configuration {
        protected final Map<String, String> replacements = new HashMap<>();

        protected final List<Pair<String, String>> textPairs = new ArrayList<>();

        @Param({"1", "2", "10", "30", "50", "100"})
        private int replacementsCount;
        @Param({"1", "5", "10", "100", "1000"})
        private int rawTextsCount;

        @Setup
        public void setup() {
            val random = ThreadLocalRandom.current();

            val indexedReplacements = new String[replacementsCount];
            for (var i = 0; i < replacementsCount; i++) {
                final String placeholder = "placeholder#" + i, replacement = Integer.toHexString(random.nextInt());
                replacements.put(placeholder, replacement);
                indexedReplacements[i] = replacement;
            }

            for (var i = 0; i < rawTextsCount; i++) {
                val raw = new StringBuilder();
                val formatted = new StringBuilder();

                val textBlocks = random.nextInt(0xFF);
                for (var blockIndex = random.nextBoolean() ? 0 : 1;
                     blockIndex < textBlocks; blockIndex++) if ((blockIndex & 0b1) == 0) {
                        // add plain text
                        val text = new StringBuilder();
                        val textLength = random.nextInt(0xF);

                        for (var textSubBlock = 0; textSubBlock < textLength; textSubBlock++) text
                                .append(Integer.toHexString(random.nextInt()));

                        raw.append(text);
                        formatted.append(text);
                    } else {
                        val placeholerId = random.nextInt(replacementsCount);
                        raw.append("{placeholder#").append(placeholerId).append('}');
                        formatted.append(indexedReplacements[placeholerId]);
                    }

                textPairs.add(SimplePair.of(raw.toString(), formatted.toString()));
            }
        }
    }

    @State(Scope.Benchmark)
    public static class StringReplaceConfiguration {
        private final List<String> texts = new ArrayList<>();

        @Setup
        public void setup(final Configuration configuration) {
            for (val textPair : configuration.textPairs) texts.add(textPair.getFirst());
        }
    }

    @State(Scope.Benchmark)
    public static class RegexConfiguration {
        // don't know why but Intellij forces escaping of `{`
        private final Pattern placeholderPattern = Pattern.compile("\\{(.*?)}");
        private final List<String> texts = new ArrayList<>();

        @Setup
        public void setup(final Configuration configuration) {
            for (val textPair : configuration.textPairs) texts.add(textPair.getFirst());
        }
    }

    @State(Scope.Benchmark)
    public static class PlaceholdersConfiguration {
        protected final Placeholders<Object> placeholders = SimplePlaceholders.builder().build();

        @Setup
        public void setup(final Configuration configuration) {
            for (val replacement : configuration.replacements.entrySet())
                placeholders.add(
                        // it is fair to get the value each time from map as regex implementation will have to do so
                        replacement.getKey(), (source, target) -> replacement.getValue()
                );
        }
    }

    @State(Scope.Benchmark)
    public static class SimpleTmfPlaceholdersConfiguration {
        private final List<TextModel<@Nullable ?>> texts = new ArrayList<>();

        @Setup
        public void setup(final Configuration configuration,
                          final PlaceholdersConfiguration placeholdersConfiguration) {
            val textModelFactory = SimpleTextModelFactory.<Object>get();
            for (val textPair : configuration.textPairs) texts
                    .add(placeholdersConfiguration.placeholders.parse(textModelFactory, textPair.getFirst()));
        }
    }

    @State(Scope.Benchmark)
    public static class AsmTmfPlaceholdersConfiguration {
        private final List<TextModel<@Nullable ?>> texts = new ArrayList<>();

        @Setup
        public void setup(final Configuration configuration,
                          final PlaceholdersConfiguration placeholdersConfiguration) {
            val textModelFactory = AsmTextModelFactory.<Object>get();
            for (val textPair : configuration.textPairs) texts
                    .add(placeholdersConfiguration.placeholders.parse(textModelFactory, textPair.getFirst()));
        }
    }
}
