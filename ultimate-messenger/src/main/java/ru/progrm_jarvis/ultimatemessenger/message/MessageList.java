package ru.progrm_jarvis.ultimatemessenger.message;

import lombok.NonNull;
import lombok.val;

import java.util.List;

/**
 * A group of {@link Message messages} which itself is treated as a {@link Message message} too.
 *
 * @param <R> type of message receivers
 */
public interface MessageList<R> extends Message<R>, List<Message<R>> {

    /**
     * {@inheritDoc}
     *
     * @param receiver {@inheritDoc}
     *
     * @implNote iterates through its messages sending each to the receiver
     */
    @Override
    default void send(@NonNull final R receiver) {
        for (val message : this) message.send(receiver);
    }

    /**
     * {@inheritDoc}
     *
     * @param receivers {@inheritDoc}
     *
     * @implNote iterates through its messages sending each to the receivers
     */
    @Override
    @SuppressWarnings("unchecked")
    default void send(@NonNull final R... receivers) {
        for (val message : this) message.send(receivers);
    }

    /**
     * {@inheritDoc}
     *
     * @param receivers {@inheritDoc}
     *
     * @implNote iterates through its messages sending each to the receivers
     */
    @Override
    default void send(@NonNull final Iterable<? extends R> receivers) {
        for (val message : this) message.send(receivers);
    }
}
