package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.*;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;
import ru.progrm_jarvis.javacommons.ownership.annotation.Own;
import ru.progrm_jarvis.javacommons.ownership.annotation.Ref;
import ru.progrm_jarvis.javacommons.primitive.NumberUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link TextModel Text model} consisting of other {@link TextModel text models}.
 *
 * @param <T> type of object according to which the text model is formatted
 */
public interface CompoundTextModel<T> extends TextModel<T>, List<TextModel<T>> {

    @Override
    default @NotNull String getText(@NotNull T target) {
        return stream()
                .map(element -> element.getText(target))
                .collect(Collectors.joining());
    }

    @Override
    default @NotNull StringBuilder write(final @NonNull StringBuilder output, final T target) {
        for (val element : this) output.append(element.getText(target));

        return output;
    }

    @Override
    default @NotNull StringBuffer write(final @NonNull StringBuffer output, final T target) {
        for (val element : this) output.append(element.getText(target));

        return output;
    }

    @Override
    default void write(final @NonNull DataOutputStream output, final T target) throws IOException {
        for (val element : this) element.write(output, target);
    }

    @Override
    default void write(final @NonNull Writer output, final T target) throws IOException {
        for (val element : this) element.write(output, target);
    }

    @Override
    default void write(final @NonNull PrintWriter output, final T target) {
        for (val element : this) element.write(output, target);
    }

    /**
     * Creates a new unmodifiable compound text model using the given collection for its backend.
     *
     * @param elements elements to use as this compound text model's content
     * @param <T> type of object according to which the text model is formatted
     * @return created delegating nesting text model
     */
    static <T> @NotNull CompoundTextModel<T> from(final @NonNull @Own List<TextModel<T>> elements) {
        var minLength = 0;
        var maxLength = 0;
        for (val element : elements) {
            minLength += element.getMinLength();
            maxLength = NumberUtil.saturatingSum(maxLength, element.getMaxLength());
        }

        return new DelegatingCompoundTextModel<>(Collections.unmodifiableList(elements), minLength, maxLength);
    }

    /**
     * Creates a new unmodifiable compound text model using the copy of the given collection's copy for its backend.
     *
     * @param elements elements copied to the new collection which will be used as this text model's content
     * @param <T> type of object according to which the text model is formatted
     * @return created delegating nesting text model
     */
    static <T> @NotNull CompoundTextModel<T> fromCopyOf(final @NonNull @Ref List<TextModel<T>> elements) {
        val copy = new ArrayList<TextModel<T>>(elements.size());
        var minLength = 0;
        var maxLength = 0;
        for (val element : elements) {
            copy.add(element);
            minLength += element.getMinLength();
            maxLength = NumberUtil.saturatingSum(maxLength, element.getMaxLength());
        }

        return new DelegatingCompoundTextModel<>(Collections.unmodifiableList(copy), minLength, maxLength);
    }

    /**
     * {@link CompoundTextModel} which delegates its {@link List list} methods to the inner {@link List}.
     *
     * @param <T> type of object according to which the text model is formatted
     */
    @Value
    @RequiredArgsConstructor(access = AccessLevel.PROTECTED) // allow extension
    @FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
    class DelegatingCompoundTextModel<T> implements CompoundTextModel<T> {

        /**
         * Content of this {@link CompoundTextModel nesting text model}
         */
        @Delegate @NonNull @Unmodifiable List<TextModel<T>> elements;

        /**
         * Minimal possible length of the produced string as specified by {@link TextModel#getMinLength()}
         */
        int minLength;

        /**
         * Maximal possible length of the produced string as specified by {@link TextModel#getMaxLength()}
         */
        int maxLength;

        @Override
        public @NotNull String getText(@NotNull final T target) {
            return write(new StringBuilder(minLength /* O(1) */), target).toString();
        }

        @Override
        public @Range(from = 0, to = Integer.MAX_VALUE) int getMinLength() {
            return minLength;
        }

        @Override
        public @Range(from = 0, to = Integer.MAX_VALUE) int getMaxLength() {
            return maxLength;
        }
    }
}
