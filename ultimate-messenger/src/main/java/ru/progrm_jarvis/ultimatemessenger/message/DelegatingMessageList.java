package ru.progrm_jarvis.ultimatemessenger.message;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple {@link MessageList} implementation which delegates its {@link List list operations}
 * to its inner {@link List list} {@link #messages}
 *
 * @param <C> type of message context
 * @param <R> type of message receivers
 */
@Data
@RequiredArgsConstructor(access = AccessLevel.PROTECTED) // allow extension
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class DelegatingMessageList<C, R> implements MessageList<C, R> {

    /**
     * {@link List} of messages to whom this message list delegates its methods
     */
    @Delegate @NonNull List<Message<C, R>> messages;

    /**
     * Creates a new {@link DelegatingMessageList} using the given list of messages.
     *
     * @param messages list of messages to use for created message list's backend
     * @param <C> type of message context
     * @param <R> type of message receivers
     * @return creates simple message list
     *
     * @implNote the created  message list will use the given list as its internal storage of messages
     * and so any changes to it from outside may also affect this message list
     */
    public static <C, R> DelegatingMessageList<C, R> from(@NonNull final List<Message<C, R>> messages) {
        return new DelegatingMessageList<>(messages);
    }

    /**
     * Creates a new {@link DelegatingMessageList} using the copy of the given list of messages.
     *
     * @param messages list of messages to be copied for use in created {@link MessageList message list's} backend
     * @param <C> type of message context
     * @param <R> type of message receivers
     * @return creates simple message list
     *
     * @implNote the created  message list will use the given list as its internal storage of messages
     * and so any changes to it from outside may also affect this message list
     */
    public static <C, R> DelegatingMessageList<C, R> fromCopyOf(@NonNull final List<Message<C, R>> messages) {
        return new DelegatingMessageList<>(new ArrayList<>(messages));
    }
}
