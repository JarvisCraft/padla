package ru.progrm_jarvis.ultimatemessenger.message;

import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;

/**
 * A group of {@link Message messages} which itself is treated as a {@link Message message} too.
 *
 * @param <C> type of message context
 * @param <R> type of message receivers
 */
public interface MessageList<C, R> extends Message<C, R>, List<Message<C, R>> {

    /**
     * {@inheritDoc}
     *
     * @param context {@inheritDoc}
     * @param receiver {@inheritDoc}
     *
     * @implNote iterates through its messages sending each to the receiver
     */
    @Override
    default void send(@NotNull C context, @NotNull R receiver) {
        for (val message : this) message.send(context, receiver);
    }

    /**
     * {@inheritDoc}
     *
     * @param context {@inheritDoc}
     * @param receivers {@inheritDoc}
     *
     * @implNote iterates through its messages sending each to the receivers
     */
    @Override
    @SuppressWarnings("unchecked") // generic varargs
    default void send(@NotNull C context, @NotNull R... receivers) {
        for (val message : this) message.send(context, receivers);
    }

    /**
     * {@inheritDoc}
     *
     * @param context {@inheritDoc}
     * @param receivers {@inheritDoc}
     *
     * @implNote iterates through its messages sending each to the receivers
     */
    @Override
    default void send(@NotNull C context, @NotNull Iterator<@NotNull R> receivers) {
        for (val message : this) message.send(context, receivers);
    }

    /**
     * {@inheritDoc}
     *
     * @param context {@inheritDoc}
     * @param receivers {@inheritDoc}
     *
     * @implNote iterates through its messages sending each to the receivers
     */
    @Override
    default void send(@NotNull C context, @NotNull Spliterator<@NotNull R> receivers) {
        for (val message : this) message.send(context, receivers);
    }

    /**
     * {@inheritDoc}
     *
     * @param context {@inheritDoc}
     * @param receivers {@inheritDoc}
     *
     * @implNote iterates through its messages sending each to the receivers
     */
    @Override
    default void send(@NotNull C context, @NotNull Iterable<@NotNull R> receivers) {
        for (val message : this) message.send(context, receivers);
    }

    /**
     * {@inheritDoc}
     *
     * @param context {@inheritDoc}
     * @param receivers {@inheritDoc}
     *
     * @implNote iterates through its messages sending each to the receivers
     */
    @Override
    default void send(@NotNull C context, @NotNull Collection<@NotNull R> receivers) {
        for (val message : this) message.send(context, receivers);
    }

    /**
     * {@inheritDoc}
     *
     * @param context {@inheritDoc}
     * @param receivers {@inheritDoc}
     *
     * @implNote iterates through its messages sending each to the receivers
     */
    @Override
    default void send(@NotNull C context, @NotNull List<@NotNull R> receivers) {
        for (val message : this) message.send(context, receivers);
    }
}
