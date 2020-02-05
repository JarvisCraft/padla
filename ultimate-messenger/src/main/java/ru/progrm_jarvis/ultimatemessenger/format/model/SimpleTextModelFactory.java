package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.lazy.Lazy;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation of {@link TextModelFactory text model factory}.
 */
public class SimpleTextModelFactory<T> implements TextModelFactory<T> {

    /**
     * Lazy singleton of this text model factory
     */
    private static final Lazy<SimpleTextModelFactory<?>> INSTANCE
            = Lazy.createThreadSafe(SimpleTextModelFactory::new);

    /**
     * Returns this {@link TextModelFactory text model factory} singleton.
     *
     * @param <T> generic type of got {@link TextModelFactory text model factory}
     * @return shared instance of this {@link TextModelFactory text model factory}
     */
    @SuppressWarnings("unchecked")
    @NotNull public static <T> SimpleTextModelFactory<T> get() {
        return (SimpleTextModelFactory<T>) INSTANCE.get();
    }

    @Override
    public @NotNull TextModelFactory.TextModelBuilder<T> newBuilder() {
        return new TextModelBuilder<>();
    }

    /**
     * Simple implementation of
     * {@link TextModelFactory.TextModelBuilder text model builder}
     * capable of joining nearby static text blocks and optimizing {@link #buildAndRelease()}.
     *
     * @param <T> type of object according to which the created text models are formatted
     */
    @ToString
    @EqualsAndHashCode(callSuper = true) // simply, why not? :) (this also allows instance caching)
    @FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
    protected static class TextModelBuilder<T> extends AbstractCachingTextModelFactoryBuilder<T> {

        @NonNull List<TextModel<T>> elements = new ArrayList<>();

        @NonFinal transient String lastStaticText;

        @Override
        @NotNull public TextModelFactory.TextModelBuilder<T> append(@NonNull final String staticText) {
            if (!staticText.isEmpty()) {
                if (lastStaticText == null) elements.add(StaticTextModel.of(lastStaticText = staticText));
                else elements.set(elements.size() - 1, StaticTextModel.of(lastStaticText += staticText)); // ...
                // ... join nearby static text blocks

                markAsChanged();
            }

            return this;
        }

        @Override
        @NotNull public TextModelFactory.TextModelBuilder<T> append(@NonNull final TextModel<T> dynamicText) {
            elements.add(dynamicText);
            lastStaticText = null;
            markAsChanged();

            return this;
        }

        @Override
        @NotNull public TextModelFactory.TextModelBuilder<T> clear() {
            if (!elements.isEmpty()) {
                elements.clear();
                lastStaticText = null;

                markAsChanged();
            }

            return this;
        }

        @Override
        @NotNull protected TextModel<T> buildTextModel(final boolean release) {
            return elements.isEmpty() ? TextModel.empty() : DelegatingNestingTextModel.fromCopyOf(elements);
        }
    }
}
