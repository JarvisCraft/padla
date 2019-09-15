package ru.progrm_jarvis.ultimatemessenger.message;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.NonNull;
import lombok.val;

/**
 * Message which may be sent to receivers and which may be serialized.
 * <p>
 * The basic object of UltimateMessenger API.
 *
 * @param <R> type of message receivers
 */
public interface Message<R> {

    /**
     * Instance of {@link Gson} with its default configuration
     */
    Gson SIMPLE_GSON = new Gson();

    /**
     * Send the message to the receiver.
     *
     * @param receiver the one to whom to send the message
     */
    void send(@NonNull R receiver);

    /**
     * Send the message to the receivers.
     *
     * @param receivers those to whom to send the message
     * @throws NullPointerException if {@code receivers} is {@code null}
     *
     * @implNote simply iterates through receivers delegating to {@link #send(Object)} so may be overridden to optimize
     */
    @SuppressWarnings("unchecked")
    default void send(@NonNull final R... receivers) {
        for (val receiver : receivers) send(receiver);
    }

    /**
     * Send the message to the receivers.
     *
     * @param receivers those to whom to send the message
     * @throws NullPointerException if {@code receivers} is {@code null}
     *
     * @implNote simply iterates through receivers delegating to {@link #send(Object)},
     * may be overridden in order to optimize it
     */
    default void send(@NonNull final Iterable<? extends R> receivers) {
        for (val receiver : receivers) send(receiver);
    }

    /**
     * Serializes this object to {@link JsonElement json element}.
     *
     * @return {@link JsonElement json element} representation of this object
     *
     * @implNote uses {@link #SIMPLE_GSON}'s {@link Gson#toJsonTree(Object)},
     * may be overridden for more specific behaviour
     */
    default JsonElement serialize() {
        return SIMPLE_GSON.toJsonTree(this);
    }
}
