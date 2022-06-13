package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.ultimatemessenger.format.util.StringMicroOptimizationUtil;

import java.util.function.Consumer;

/**
 * <p>Implementation of {@link TextModelFactory text model factory} for debugging purposes.
 * This does not itself implement the behaviour thus simply delegating it
 * to the {@link TextModelFactory text model factory} passed to it.</p>
 * <p>It may come in handy when implementing objects which use {@link TextModelFactory text model factories}.</p>
 *
 * @param <T> type of object according to which the created text models are formatted
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class DebuggingTextModelFactory<T> implements TextModelFactory<T> {

    /**
     * Text model factory who is used for actual logic implementation
     */
    @NotNull TextModelFactory<T> textModelFactory;

    /**
     * Handler accepting debug messages on each method called
     */
    @NotNull Consumer<@NotNull String> debugHandler;

    /**
     * Creates a new debugging {@link TextModel text model factory}.
     *
     * @param textModelFactory text model factory who is used for actual logic implementation
     * @param debugHandler handler accepting debug messages on each method called
     * @param <T> type of object according to which the created text models are formatted
     * @return created text model factory
     */
    public static <T> @NotNull TextModelFactory<T> create(
            final @NonNull TextModelFactory<T> textModelFactory,
            final @NonNull Consumer<@NotNull String> debugHandler
    ) {
        return new DebuggingTextModelFactory<>(textModelFactory, debugHandler);
    }

    @Override
    public @NotNull TextModel<T> empty() {
        debugHandler.accept("TextModelFactory#empty()");

        return textModelFactory.empty();
    }

    @Override
    public TextModelFactory.@NotNull TextModelBuilder<T> newBuilder() {
        debugHandler.accept("TextModelFactory#newBuilder()");

        return new DebuggingTextModelBuilder(textModelFactory.newBuilder());
    }

    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
    private final class DebuggingTextModelBuilder implements TextModelFactory.TextModelBuilder<T> {

        TextModelFactory.@NotNull TextModelBuilder<T> textModelBuilder;

        @Override
        public TextModelFactory.@NotNull TextModelBuilder<T> append(final @NonNull String staticText) {
            debugHandler.accept(
                    "TextModelBuilder#append(\""
                            + StringMicroOptimizationUtil.escapeJavaStringLiteral(staticText) + "\")"
            );

            return textModelBuilder.append(staticText);
        }

        @Override
        public TextModelFactory.@NotNull TextModelBuilder<T> append(final @NonNull TextModel<T> dynamicText) {
            debugHandler.accept("TextModelBuilder#append(" + dynamicText + ')');

            return textModelBuilder.append(dynamicText);
        }

        @Override
        public TextModelFactory.@NotNull TextModelBuilder<T> clear() {
            debugHandler.accept("TextModelBuilder#clear()");

            return textModelBuilder.clear();
        }

        @Override
        public @NotNull TextModel<T> build() {
            debugHandler.accept("TextModelBuilder#build()");

            return textModelBuilder.build();
        }

        @Override
        public @NotNull TextModel<T> buildAndRelease() {
            debugHandler.accept("TextModelBuilder#buildAndRelease()");

            return textModelBuilder.buildAndRelease();
        }
    }
}
