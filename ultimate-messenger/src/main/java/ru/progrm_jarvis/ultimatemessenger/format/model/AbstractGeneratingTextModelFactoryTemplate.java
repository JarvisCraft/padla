package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Common abstract base for {@link TextModelFactory.TextModelTemplate} capable of caching
 * which generates {@link TextModel text models} using its internal elements.
 *
 * @param <T> type of object according to which the created text models are formatted
 */
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true) // because why not? (also allows caching)
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AbstractGeneratingTextModelFactoryTemplate<T> extends AbstractCachingTextModelFactoryTemplate<T> {

    /**
     * Instantiates new {@link AbstractGeneratingTextModelFactoryTemplate} using {@link ArrayList} for its backend.
     */
    protected AbstractGeneratingTextModelFactoryTemplate() {
        this(new ArrayList<>());
    }

    /**
     * Elements of the text model
     */
    @NonNull List<Element<T>> elements;

    /**
     * Amount of dynamic text model elements
     */
    @NonFinal transient int dynamicElementCount = 0,

    /**
     * Length of static text part (minimal length of resulting text)
     */
    staticLength = 0;

    /**
     * Last appended element
     */
    @NonFinal @Nullable transient Element<T> lastAppendedElement;

    @Override
    public TextModelFactory.TextModelTemplate<T> append(@NonNull final String staticText) {
        if (!staticText.isEmpty()) {
            val tail = lastAppendedElement;

            if (tail == null || tail.isDynamic()) elements
                    .add(lastAppendedElement = new StaticElement<>(staticText)); // add new static element
            else tail.appendStaticContent(staticText); //  join nearby static elements

            // increment length of static content by the given text's length
            staticLength += staticText.length();

            markAsChanged();
        }

        return this;
    }

    @Override
    public TextModelFactory.TextModelTemplate<T> append(@NonNull final TextModel<T> dynamicText) {
        elements.add(lastAppendedElement = new DynamicElement<>(dynamicText));
        // increment the amount of dynamic elements
        dynamicElementCount++;

        markAsChanged();

        return this;
    }

    @Override
    public TextModelFactory.TextModelTemplate<T> clear() {
        if (!elements.isEmpty()) {
            elements.clear();
            lastAppendedElement = null;
            staticLength = dynamicElementCount = 0;

            markAsChanged();
        }

        return this;
    }

    /**
     * Creates new {@link TextModel} according to this template's state.
     * This should not handle caching to {@link #cachedTextModel} as this will be done by the calling method.
     *
     * @param release {@code true} if this template will be released after the call and {@code false} otherwise
     * @return created text model
     *
     * @apiNote gets called by {@link #createTextModel(boolean)}
     * whenever it cannot create a fast universal implementation
     */
    protected abstract TextModel<T> performTextModelCreation(final boolean release);

    /**
     * {@inheritDoc}
     * <p>
     * Attempts to create a fast implementation if there is no dynamic content in this template.
     *
     * @param release {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected TextModel<T> createTextModel(final boolean release) {
        if (elements.isEmpty()) return TextModel.empty();
        if (dynamicElementCount == 0) {
            val tail = lastAppendedElement;
            // this should never happen actually, but it might be an error marker for broken implementations
            assert tail != null;
            return StaticTextModel.of(tail.getStaticContent());
        }

        return performTextModelCreation(release);
    }

    /**
     * Element of the not generated {@link TextModel}.
     *
     * @param <T> type of object according to which the created text models are formatted
     */
    protected interface Element<T> {

        /**
         * Marker indicating whether this element is dynamic.
         *
         * @return {@code true} if this text model is dynamic and {@code false} otherwise
         */
        boolean isDynamic();

        /**
         * Gets this element's dynamic content.
         *
         * @return dynamic content of this element
         *
         * @throws UnsupportedOperationException if this element is static
         */
        @NotNull TextModel<T> getDynamicContent();

        /**
         * Gets this element's static content.
         *
         * @return static content of this element
         *
         * @throws UnsupportedOperationException if this element is dynamic
         */
        @NotNull String getStaticContent();

        /**
         * Appends a static element to this element's content.
         *
         * @param content content to append to this element's current content
         *
         * @throws UnsupportedOperationException if this element is dynamic
         */
        void appendStaticContent(@NotNull String content);
    }

    /**
     * Dynamic {@link Element}.
     *
     * @param <T> type of object according to which the created text models are formatted
     */
    @Value
    @FieldDefaults(level = AccessLevel.PRIVATE)
    protected static class DynamicElement<T> implements Element<T> {

        /**
         * Dynamic content of this element
         */
        @NotNull TextModel<T> dynamicContent;

        @Override
        public boolean isDynamic() {
            return true;
        }

        @Override
        @NotNull public String getStaticContent() {
            throw new UnsupportedOperationException("This is a dynamic element");
        }

        @Override
        public void appendStaticContent(@NotNull final String content) {
            throw new UnsupportedOperationException("This is a dynamic element");
        }
    }

    /**
     * Static {@link Element}.
     *
     * @param <T> type of object according to which the created text models are formatted
     */
    @Value
    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    protected static class StaticElement<T> implements Element<T> {

        @NotNull StringBuilder staticContent;

        public StaticElement(@NonNull final String content) {
            this(new StringBuilder(content));
        }

        @Override
        public boolean isDynamic() {
            return false;
        }

        @Override
        @NotNull public String getStaticContent() {
            return staticContent.toString();
        }

        @Override
        public void appendStaticContent(@NotNull final String content) {
            staticContent.append(content);
        }

        @Override
        @NotNull public TextModel<T> getDynamicContent() {
            throw new UnsupportedOperationException("This is a static element");
        }
    }
}
