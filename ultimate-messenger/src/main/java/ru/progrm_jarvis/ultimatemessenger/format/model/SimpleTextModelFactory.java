package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation of {@link TextModelFactory text model factory}.
 */
public class SimpleTextModelFactory<T> implements TextModelFactory<T> {

    @Override
    public TextModelFactory.TextModelTemplate<T> newTemplate() {
        return new TextModelTemplate<>();
    }

    /**
     * Simple implementation of
     * {@link TextModelFactory.TextModelTemplate text model template}
     * capable of joining nearby static text blocks and optimizing {@link #createAndRelease()}.
     *
     * @param <T> type of object according to which the created text models are formatted
     */
    @ToString
    @EqualsAndHashCode(callSuper = true) // simply, why not? :) (this also allows instance caching)
    @FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
    protected static class TextModelTemplate<T> extends AbstractCachingTextModelFactoryTemplate<T> {

        @NonNull List<TextModel<T>> elements = new ArrayList<>();

        @NonFinal transient String lastStaticText;

        @Override
        public TextModelFactory.TextModelTemplate<T> append(@NonNull final String staticText) {
            if (!staticText.isEmpty()) {
                if (lastStaticText == null) elements.add(StaticTextModel.of(lastStaticText = staticText));
                else elements.set(elements.size() - 1, StaticTextModel.of(lastStaticText += staticText)); // ...
                // ... join nearby static text blocks

                markAsChanged();
            }

            return this;
        }

        @Override
        public TextModelFactory.TextModelTemplate<T> append(@NonNull final TextModel<T> dynamicText) {
            elements.add(dynamicText);
            lastStaticText = null;
            markAsChanged();

            return this;
        }

        @Override
        public TextModelFactory.TextModelTemplate<T> clear() {
            if (!elements.isEmpty()) {
                elements.clear();
                lastStaticText = null;

                markAsChanged();
            }

            return this;
        }

        @Override
        protected TextModel<T> createTextModel(final boolean release) {
            return elements.isEmpty() ? TextModel.empty() : DelegatingNestingTextModel.fromCopyOf(elements);
        }
    }
}
