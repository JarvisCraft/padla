package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.ownership.annotation.Own;
import ru.progrm_jarvis.javacommons.ownership.annotation.Ref;

import java.util.List;

/**
 * Legacy factory of static {@link CompoundTextModel compound text models}.
 *
 * @deprecated use {@link CompoundTextModel#from(List)} and {@link CompoundTextModel#fromCopyOf(List)} instead,
 * this one will most probably be removed before release <b>1.0.0</b>
 */
@Deprecated
@UtilityClass
public class DelegatingNestingTextModel {

    /**
     * Creates a new unmodifiable compound text model using the given collection for its backend.
     *
     * @param elements elements to use as this compound text model's content
     * @param <T> type of object according to which the text model is formatted
     * @return created delegating nesting text model
     * 
     * @deprecated use {@link CompoundTextModel#from(List)} instead
     */
    @Deprecated
    public <T> @NotNull CompoundTextModel<T> from(final @NonNull @Own List<TextModel<T>> elements) {
        return CompoundTextModel.from(elements);
    }

    /**
     * Creates a new unmodifiable compound text model using the copy of the given collection's copy for its backend.
     *
     * @param elements elements copied to the new collection which will be used as this text model's content
     * @param <T> type of object according to which the text model is formatted
     * @return created delegating nesting text model
     *
     * @deprecated use {@link CompoundTextModel#fromCopyOf(List)} instead
     */
    @Deprecated
    public <T> @NotNull CompoundTextModel<T> fromCopyOf(final @NonNull @Ref List<TextModel<T>> elements) {
        return CompoundTextModel.fromCopyOf(elements);
    }
}
