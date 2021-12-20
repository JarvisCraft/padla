package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Legacy factory of static {@link TextModel text models}.
 *
 * @deprecated use {@link TextModel#of(String)} instead,
 * this one will most probably be removed before release <b>1.0.0</b>
 */
@Deprecated
@UtilityClass
public class StaticTextModel {

    /**
     * Creates a text model of the given static text.
     *
     * @param text static text of the text model
     * @param <T> type of object according to which the text model is formatted (effectively unused)
     * @return text model of the given static text
     *
     * @deprecated use {@link TextModel#of(String)} instead
     */
    @Deprecated
    public <T> TextModel<T> of(final @NonNull String text) {
        return TextModel.of(text);
    }
}
