package ru.progrm_jarvis.ultimatemessenger.message;

import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;

/**
 * Type of a message.
 *
 * @param <C> type of message context
 * @param <R> type of message receivers
 *
 * @apiNote this interface has overloads for various iterable types in order to allow custom optimizations for them
 */
@FunctionalInterface
public interface Message<C, R> {

    /**
     * Sends a message in the given context to the receiver.
     *
     * @param context context of the message
     * @param receiver receiver of the message
     */
    void send(@NotNull C context, @NotNull R receiver);

    /**
     * Sends a message in the given context to each of the receivers.
     *
     * @param context context of the message
     * @param receivers receivers of the message
     */
    @SuppressWarnings("unchecked") // generic vararg
    default void send(@NotNull C context, @NotNull R... receivers) {
        for (val receiver : receivers) send(context, receiver);
    }

    /**
     * Sends a message in the given context to each of the receivers.
     *
     * @param context context of the message
     * @param receivers receivers of the message
     */
    default void send(@NotNull C context, @NotNull Iterator<@NotNull R> receivers) {
        while (receivers.hasNext()) send(context, receivers.next());
    }

    default void send(@NotNull C context, @NotNull Spliterator<@NotNull R> receivers) {
        receivers.forEachRemaining(receiver -> send(context, receiver));
    }

    /**
     * Sends a message in the given context to each of the receivers.
     *
     * @param context context of the message
     * @param receivers receivers of the message
     */
    default void send(@NotNull C context, @NotNull Iterable<@NotNull R> receivers) {
        for (val receiver : receivers) send(context, receiver);
    }

    /**
     * Sends a message in the given context to each of the receivers.
     *
     * @param context context of the message
     * @param receivers receivers of the message
     */
    default void send(@NotNull C context, @NotNull Collection<@NotNull R> receivers) {
        for (val receiver : receivers) send(context, receiver);
    }

    /**
     * Sends a message in the given context to each of the receivers.
     *
     * @param context context of the message
     * @param receivers receivers of the message
     */
    default void send(@NotNull C context, @NotNull List<@NotNull R> receivers) {
        for (val receiver : receivers) send(context, receiver);
    }
}
