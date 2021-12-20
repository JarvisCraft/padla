package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.*;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import ru.progrm_jarvis.javacommons.ownership.annotation.Own;
import ru.progrm_jarvis.javacommons.primitive.NumberUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link CompoundTextModel} which delegates its {@link List list} methods to the inner {@link List}.
 *
 * @param <T> type of object according to which the text model is formatted
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PROTECTED) // allow extension
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class DelegatingCompoundTextModel<T> implements CompoundTextModel<T> {

    /**
     * Content of this {@link CompoundTextModel nesting text model}
     */
    @Delegate @NonNull List<TextModel<T>> elements;

    /**
     * Minimal possible length of the produced string
     */
    int minLength;

    /**
     * Maximal possible length
     */
    int maxLength;

    @Override
    public @NotNull String getText(@NotNull final T target) {
        return CompoundTextModel.super.getText(target);
    }

    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) int getMinLength() {
        return minLength;
    }

    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) int getMaxLength() {
        return maxLength;
    }

    /**
     * Creates a new delegating {@link CompoundTextModel} using the given collection fot its backend.
     *
     * @param elements elements to use as this {@link CompoundTextModel nesting text model's} content
     * @param <T> type of object according to which the text model is formatted
     * @return created delegating nesting text model
     */
    public static <T> @NotNull CompoundTextModel<T> from(final @Own @NonNull List<TextModel<T>> elements) {
        var minLength = 0;
        var maxLength = 0;

        for (val element : elements) {
            minLength += element.getMinLength();
            maxLength = NumberUtil.saturatingSum(maxLength, element.getMaxLength());
        }

        return new DelegatingCompoundTextModel<>(elements, minLength, maxLength);
    }

    /**
     * Creates a new delegating {@link CompoundTextModel} using the copy of the given collection fot its backend.
     *
     * @param elements elements copied to the new collection which will be used
     * as this {@link CompoundTextModel nesting text model's} content
     * @param <T> type of object according to which the text model is formatted
     * @return created delegating nesting text model
     */
    public static <T> @NotNull CompoundTextModel<T> fromCopyOf(final @NonNull List<TextModel<T>> elements) {
        val copy = new ArrayList<TextModel<T>>(elements.size());
        var minLength = 0;
        var maxLength = 0;

        for (val element : elements) {
            copy.add(element);
            minLength += element.getMinLength();
            maxLength = NumberUtil.saturatingSum(maxLength, element.getMaxLength());
        }

        return new DelegatingCompoundTextModel<>(copy, minLength, maxLength);
    }
}
