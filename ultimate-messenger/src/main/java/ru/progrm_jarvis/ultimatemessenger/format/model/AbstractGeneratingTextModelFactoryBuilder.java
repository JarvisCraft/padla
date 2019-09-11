package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayList;
import java.util.List;

/**
 * Common abstract base for {@link TextModelFactory.TextModelBuilder} capable of caching
 * which generates {@link TextModel text models} using its internal elements.
 *
 * @param <T> type of object according to which the created text models are formatted
 * @param <N> type of {@link Node nodes} used for this builder's backend
 * @param <SN> type of {@link StaticNode static nodes} used for this builder's backend
 * @param <DN> type of {@link DynamicNode dynamic nodes} used for this builder's backend
 */
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true) // because why not? (also allows caching)
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AbstractGeneratingTextModelFactoryBuilder<T,
        N extends AbstractGeneratingTextModelFactoryBuilder.Node<T, SN, DN>,
        SN extends AbstractGeneratingTextModelFactoryBuilder.StaticNode<T>,
        DN extends AbstractGeneratingTextModelFactoryBuilder.DynamicNode<T>>
        extends AbstractCachingTextModelFactoryBuilder<T> {

    /**
     * Instantiates new {@link AbstractGeneratingTextModelFactoryBuilder} using {@link ArrayList} for its backend.
     */
    protected AbstractGeneratingTextModelFactoryBuilder() {
        this(new ArrayList<>());
    }

    /**
     * Nodes of the text model
     */
    List<N> nodes;

    /**
     * Amount of dynamic nodes
     */
    @NonFinal transient int dynamicNodeCount = 0,

    /**
     * Length of static text part (minimal length of resulting text)
     */
    staticLength = 0;

    /**
     * Last appended node
     */
    @NonFinal transient N lastNode;

    /**
     * Ends the modification of the last node being a static one.
     */
    @OverridingMethodsMustInvokeSuper
    protected void endLastStaticNodeModification() {
        // increase the length of static text part
        staticLength += lastNode.asStatic().getTextLength();
    }

    /**
     * Ends the modification of the last node being a dynamic one.
     */
    @OverridingMethodsMustInvokeSuper
    protected void endLastDynamicNodeModification() {
        // increment the amount of dynamic elements
        dynamicNodeCount++;
    }

    /**
     * Ends the modification of the last node delegating to {@link #endLastDynamicNodeModification()}
     * if the {@link #lastNode last node} {@link Node#isDynamic() is dynamic}
     * or {@link #endLastDynamicNodeModification()} otherwise.
     */
    protected void endLastNodeModification() {
        if (lastNode != null) {
            if (lastNode.isDynamic()) endLastDynamicNodeModification();
            else endLastStaticNodeModification();
        }
    }

    /**
     * Creates a new static node to be used for creation of the {@link TextModel text model}.
     *
     * @param text static text of the created node
     * @return created static node
     */
    @NotNull protected abstract N newStaticNode(@NotNull final String text);

    /**
     * Creates a new dynamic node to be used for creation of the {@link TextModel text model}.
     *
     * @param content dynamic content of the created node
     * @return created dynamic node
     */
    @NotNull protected abstract N newDynamicNode(@NotNull final TextModel<T> content);

    @Override
    @NotNull public TextModelFactory.TextModelBuilder<T> append(@NonNull final String staticText) {
        if (!staticText.isEmpty()) {
            val tail = lastNode;

            if (tail == null) {
                val node = newStaticNode(staticText); // add new static element
                nodes.add(lastNode = node);
            } else if (tail.isDynamic()) {
                endLastDynamicNodeModification();

                val node = newStaticNode(staticText); // add new static element
                nodes.add(lastNode = node);
            } else tail.asStatic().appendText(staticText); //  join nearby static elements

            markAsChanged();
        }

        return this;
    }

    @Override
    @NotNull public TextModelFactory.TextModelBuilder<T> append(@NonNull final TextModel<T> dynamicText) {
        endLastNodeModification();

        val node = newDynamicNode(dynamicText);
        nodes.add(lastNode = node);

        markAsChanged();

        return this;
    }

    @Override
    @NotNull public TextModelFactory.TextModelBuilder<T> clear() {
        if (!nodes.isEmpty()) {
            nodes.clear();
            lastNode = null;
            staticLength = dynamicNodeCount = 0;

            markAsChanged();
        }

        return this;
    }

    /**
     * Creates new {@link TextModel} according to this text model builder's state.
     * This should not handle caching to {@link #cachedTextModel} as this will be done by the calling method.
     *
     * @param release {@code true} if this text model builder will be released after the call and {@code false} otherwise
     * @return created text model
     *
     * @apiNote gets called by {@link #createTextModel(boolean)}
     * whenever it cannot create a fast universal implementation
     */
    @NotNull protected abstract TextModel<T> performTextModelCreation(boolean release);

    /**
     * {@inheritDoc}
     * <p>
     * Attempts to create a fast implementation if there is no dynamic content in this text model builder.
     *
     * @param release {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    @NotNull protected TextModel<T> createTextModel(final boolean release) {
        if (nodes.isEmpty()) return TextModel.empty();

        // make sure that the last node modification is ended properly
        endLastNodeModification();

        if (dynamicNodeCount == 0) { // no dynamic elements
            val tail = lastNode;
            // this should never happen actually, but it might be an error marker for broken implementations
            assert tail != null;
            return StaticTextModel.of(tail.asStatic().getText());
        }
        if (staticLength == 0 && dynamicNodeCount == 1) { // only 1 dynamic element without static ones
            val tail = lastNode;
            // this should never happen actually, but it might be an error marker for broken implementations
            assert tail != null;
            return tail.asDynamic().getContent();
        }

        return performTextModelCreation(release);
    }

    /**
     * Node by which the created {@link TextModel} is formed.
     * This is actually a type-safe wrapper for static and dynamic nodes (actually, a union-type)
     * which usually do implement this interface returning themselves in the corresponding methods
     * in order to decrease the amount of allocated objects.
     *
     * @param <T> type of object according to which the created text models are formatted
     * @param <S> type of static node wrapped
     * @param <D> type of dynamic node wrapped
     */
    protected interface Node<T, S extends StaticNode<T>, D extends DynamicNode<T>> {

        /**
         * Marker indicating whether this element is dynamic.
         *
         * @return {@code true} if this text model is dynamic and {@code false} otherwise
         */
        boolean isDynamic();

        /**
         * Gets this node's static view if it is not {@link #isDynamic() dynamic} otherwise throwing an exception.
         *
         * @return static view of this node
         * @throws UnsupportedOperationException if this node is {@link #isDynamic() dynamic}
         *
         * @see #isDynamic() to check if this method can be used
         */
        default S asStatic() {
            throw new UnsupportedOperationException("This is not a static element");
        }

        /**
         * Gets this node's dynamic view if it is {@link #isDynamic() dynamic} otherwise throwing an exception.
         *
         * @return dynamic view of this node
         * @throws UnsupportedOperationException if this node is not {@link #isDynamic() dynamic}
         *
         * @see #isDynamic() to check if this method can be used
         */
        default D asDynamic() {
            throw new UnsupportedOperationException("This is not a dynamic element");
        }
    }

    /**
     * Static {@link Node node}.
     *
     * @param <T> type of object according to which the created text models are formatted
     *
     * @apiNote this interface does not extend {@link Node} but it is a common practice to have a class implement both
     * in order to decrease the amount of allocated objects
     */
    protected interface StaticNode<T> {

        /**
         * Gets this element's static content.
         *
         * @return static content of this element
         *
         * @throws UnsupportedOperationException if this element is dynamic
         */
        @NotNull String getText();

        int getTextLength();

        /**
         * Appends a static element to this element's content.
         *
         * @param text text to append to this element's current text
         *
         * @throws UnsupportedOperationException if this element is dynamic
         */
        void appendText(@NotNull String text);
    }


    /**
     * Dynamic {@link Node node}.
     *
     * @param <T> type of object according to which the created text models are formatted
     *
     * @apiNote this interface does not extend {@link Node} but it is a common practice to have a class implement both
     * in order to decrease the amount of allocated objects
     */
    protected interface DynamicNode<T> {
        /**
         * Gets this element's dynamic content.
         *
         * @return dynamic content of this element
         *
         * @throws UnsupportedOperationException if this element is static
         */
        @NotNull TextModel<T> getContent();
    }

    /**
     * Simple implementation of {@link StaticNode} also being a {@link Node}.
     *
     * @param <T> type of object according to which the created text models are formatted
     */
    @Value
    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SimpleStaticNode<T> implements Node<T, StaticNode<T>, DynamicNode<T>>, StaticNode<T> {

        /**
         * Text of this node
         */
        @NotNull StringBuilder text;

        protected SimpleStaticNode(@NonNull final String text) {
            this(new StringBuilder(text));
        }

        @Override
        public boolean isDynamic() {
            return false;
        }

        @Override
        public SimpleStaticNode<T> asStatic() {
            return this;
        }

        @Override
        @NotNull public String getText() {
            return text.toString();
        }

        @Override
        public int getTextLength() {
            return text.length();
        }

        @Override
        public void appendText(@NotNull final String text) {
            this.text.append(text);
        }
    }

    /**
     * Simple implementation of {@link DynamicNode} also being a {@link Node}.
     *
     * @param <T> type of object according to which the created text models are formatted
     */
    @Value
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SimpleDynamicNode<T> implements Node<T, StaticNode<T>, DynamicNode<T>>, DynamicNode<T> {

        /**
         * Dynamic content of this node
         */
        @NotNull TextModel<T> content;

        @Override
        public boolean isDynamic() {
            return true;
        }

        @Override
        public DynamicNode<T> asDynamic() {
            return this;
        }
    }
}
