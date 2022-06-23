package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import ru.progrm_jarvis.javacommons.primitive.NumberUtil;

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
        SN extends AbstractGeneratingTextModelFactoryBuilder.StaticNode,
        DN extends AbstractGeneratingTextModelFactoryBuilder.DynamicNode<T>>
        extends AbstractCachingTextModelFactoryBuilder<T> {

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
    staticLength = 0,
    /**
     * Length of static text part (minimal length of resulting text)
     */
    minDynamicLength = 0;

    /**
     * Last appended node
     */
    @NonFinal transient N lastNode;

    /**
     * Instantiates new abstract generating text model factory builder using {@link ArrayList} for its backend.
     */
    protected AbstractGeneratingTextModelFactoryBuilder() {
        this(new ArrayList<>());
    }

    /**
     * Ends the modification of ta static node.
     *
     * @param staticNode node whose modification should be ended
     */
    @MustBeInvokedByOverriders
    protected void endModification(final @NotNull SN staticNode) {
        // increase the length of static text part
        staticLength += staticNode.getTextLength();
    }

    /**
     * Ends the modification of a dynamic one.
     *
     * @param dynamicNode node whose modification should be ended
     */
    @MustBeInvokedByOverriders
    protected void endModification(final @NotNull DN dynamicNode) {
        // increment the amount of dynamic elements
        dynamicNodeCount++;
        minDynamicLength = NumberUtil.saturatingSum(minDynamicLength, dynamicNode.getContent().getMinLength());
    }

    /**
     * Ends the modification of the last node delegating
     * to {@link #endModification(DynamicNode)} if the {@link #lastNode last node} {@link Node#isDynamic() is dynamic}
     * or {@link #endModification(StaticNode)} otherwise.
     */
    protected void endLastNodeModification() {
        if (lastNode != null) if (lastNode.isDynamic()) endModification(lastNode.asDynamic());
        else endModification(lastNode.asStatic());
    }

    /**
     * Creates a new static node to be used for creation of the {@link TextModel text model}.
     *
     * @param text static text of the created node
     * @return created static node
     */
    protected abstract @NotNull N newStaticNode(final @NotNull String text);

    /**
     * Creates a new dynamic node to be used for creation of the {@link TextModel text model}.
     *
     * @param content dynamic content of the created node
     * @return created dynamic node
     */
    protected abstract @NotNull N newDynamicNode(final @NotNull TextModel<T> content);

    @Override
    public @NotNull TextModelFactory.TextModelBuilder<T> append(final @NonNull String staticText) {
        if (!staticText.isEmpty()) {
            val tail = lastNode;

            if (tail == null) {
                val node = newStaticNode(staticText); // add new static element
                nodes.add(lastNode = node);
            } else if (tail.isDynamic()) {
                endModification(lastNode.asDynamic());

                val node = newStaticNode(staticText); // add new static element
                nodes.add(lastNode = node);
            } else tail.asStatic().appendText(staticText); //  join nearby static elements

            markAsChanged();
        }

        return this;
    }

    @Override
    public @NotNull TextModelFactory.TextModelBuilder<T> append(final @NonNull TextModel<T> dynamicText) {
        if (dynamicText.isDynamic()) {
            endLastNodeModification();

            val node = newDynamicNode(dynamicText);
            nodes.add(lastNode = node);

            markAsChanged();
        } else append(dynamicText.getText(null));

        return this;
    }

    @Override
    public @NotNull TextModelFactory.TextModelBuilder<T> clear() {
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
     * @apiNote gets called by {@link #buildTextModel(boolean)}
     * whenever it cannot create a fast universal implementation
     */
    protected abstract @NotNull TextModel<T> performTextModelBuild(boolean release);

    /**
     * {@inheritDoc}
     * <p>Attempts to create a fast implementation if there is no dynamic content in this text model builder.</p>
     *
     * @param release {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected @NotNull TextModel<T> buildTextModel(final boolean release) {
        if (nodes.isEmpty()) return TextModel.empty();

        // make sure that the last node modification is ended properly
        endLastNodeModification();

        if (dynamicNodeCount == 0) { // no dynamic elements
            val tail = lastNode;
            // this should never happen actually, but it might be an error marker for broken implementations
            assert tail != null;
            return TextModel.of(tail.asStatic().getText());
        }
        if (staticLength == 0 && dynamicNodeCount == 1) { // only 1 dynamic element without static ones
            val tail = lastNode;
            // this should never happen actually, but it might be an error marker for broken implementations
            assert tail != null;
            return tail.asDynamic().getContent();
        }

        return performTextModelBuild(release);
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
    protected interface Node<T, S extends StaticNode, D extends DynamicNode<T>> {

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
        @NotNull S asStatic();

        /**
         * Gets this node's dynamic view if it is {@link #isDynamic() dynamic} otherwise throwing an exception.
         *
         * @return dynamic view of this node
         * @throws UnsupportedOperationException if this node is not {@link #isDynamic() dynamic}
         *
         * @see #isDynamic() to check if this method can be used
         */
        @NotNull D asDynamic();
    }

    /**
     * Static {@link Node node}.
     *
     * @apiNote this interface does not extend {@link Node} but it is a common practice to have a class implement both
     * in order to decrease the amount of allocated objects
     */
    protected interface StaticNode {

        /**
         * Gets this element's static content.
         *
         * @return static content of this element
         *
         * @throws UnsupportedOperationException if this element is dynamic
         */
        @NotNull String getText();

        /**
         * Gets the length of the static text's content.
         *
         * @return length of the static text's content
         */
        @Range(from = 0, to = Integer.MAX_VALUE) int getTextLength();

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
    @FunctionalInterface
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
    protected static class SimpleStaticNode<T> implements Node<T, StaticNode, DynamicNode<T>>, StaticNode {

        /**
         * Text of this node
         */
        @SuppressWarnings("StringBufferField") @NotNull StringBuilder text;

        protected SimpleStaticNode(final @NonNull String text) {
            this(new StringBuilder(text));
        }

        @Override
        public boolean isDynamic() {
            return false;
        }

        @Override
        public @NotNull StaticNode asStatic() {
            return this;
        }

        @Override
        public @NotNull String getText() {
            return text.toString();
        }

        @Override
        public @Range(from = 0, to = Integer.MAX_VALUE) int getTextLength() {
            return text.length();
        }

        @Override
        public void appendText(final @NotNull String text) {
            this.text.append(text);
        }

        @Override
        public @NotNull DynamicNode<T> asDynamic() {
            throw new UnsupportedOperationException("This is not a dynamic node");
        }
    }

    /**
     * Simple implementation of {@link DynamicNode} also being a {@link Node}.
     *
     * @param <T> type of object according to which the created text models are formatted
     */
    @Value
    @FieldDefaults(level = AccessLevel.PRIVATE)
    protected static class SimpleDynamicNode<T> implements Node<T, StaticNode, DynamicNode<T>>, DynamicNode<T> {

        /**
         * Dynamic content of this node
         */
        @NotNull TextModel<T> content;

        @Override
        public boolean isDynamic() {
            return true;
        }

        @Override
        public @NotNull DynamicNode<T> asDynamic() {
            return this;
        }

        @Override
        public @NotNull StaticNode asStatic() {
            throw new UnsupportedOperationException("This is not a static node");
        }
    }
}
