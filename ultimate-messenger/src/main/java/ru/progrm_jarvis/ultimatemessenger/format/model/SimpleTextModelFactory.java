package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.lazy.Lazy;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation of {@link TextModelFactory text model factory}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleTextModelFactory<T> implements TextModelFactory<T> {

    /**
     * Lazy singleton of this text model factory
     */
    private static final Lazy<TextModelFactory<?>> INSTANCE
            = Lazy.createThreadSafe(SimpleTextModelFactory::new);

    /**
     * Creates a simple {@link TextModelFactory text model factory}.
     *
     * @param <T> generic type of got {@link TextModelFactory text model factory}
     * @return created text model factory
     *
     * @implNote returned instance may be a singleton
     */
    @SuppressWarnings("unchecked")
    public static <T> @NotNull TextModelFactory<T> create() {
        return (TextModelFactory<T>) INSTANCE.get();
    }

    @Override
    public @NotNull TextModelFactory.TextModelBuilder<T> newBuilder() {
        return new SimpleTextModelBuilder<>();
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
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class SimpleTextModelBuilder<T> extends AbstractCachingTextModelFactoryBuilder<T> {

        /**
         * Elements appended to this builder.
         */
        @NonNull List<TextModel<T>> elements = new ArrayList<>();

        /**
         * Last static text or {@code null} if there are no elements or the last element is dynamic
         */
        @NonFinal transient String lastStaticText;

        @Override
        public @NotNull TextModelFactory.TextModelBuilder<T> append(final @NonNull String staticText) {
            if (!staticText.isEmpty()) {
                if (lastStaticText == null) elements.add(TextModel.of(lastStaticText = staticText));
                else elements.set(elements.size() - 1, TextModel.of(lastStaticText += staticText)); // ...
                // ... join nearby static text blocks

                markAsChanged();
            }

            return this;
        }

        @Override
        public @NotNull TextModelFactory.TextModelBuilder<T> append(final @NonNull TextModel<T> dynamicText) {
            elements.add(dynamicText);
            lastStaticText = null;
            markAsChanged();

            return this;
        }

        @Override
        public @NotNull TextModelFactory.TextModelBuilder<T> clear() {
            if (!elements.isEmpty()) {
                elements.clear();
                lastStaticText = null;

                markAsChanged();
            }

            return this;
        }

        @Override
        protected @NotNull TextModel<T> buildTextModel(final boolean release) {
            return elements.isEmpty() ? TextModel.empty() : CompoundTextModel.fromCopyOf(elements);
        }
    }
}
