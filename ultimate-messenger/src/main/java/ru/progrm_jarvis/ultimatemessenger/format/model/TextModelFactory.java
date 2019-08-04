package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.NonNull;

/**
 * Object used to create {@link TextModel text models}.
 * <p>
 * This is a mixture of builder and visitor which provides a common interface
 * for creating {@link TextModel text models} via its methods.
 *
 * @param <T> type of object according to which the created text models are formatted
 */
public interface TextModelFactory<T> {

    /**
     * Creates an empty {@link TextModel text model}.
     *
     * @return empty text model
     *
     * @apiNote as {@link TextModel text models} are immutable, this method may return an empty singleton
     */
    default TextModel<T> empty() {
        return TextModel.empty();
    }

    /**
     * Creates new empty {@link TextModelTemplate text model template}
     * which may be used for creation of {@link TextModel text models}.
     *
     * @return new text model template
     */
    TextModelTemplate<T> newTemplate();

    /**
     * Stateful object used for creation of {@link TextModel text model}.
     * <p>
     * This object may be reused in single-threaded environment via {@link #clear()} although it is not bad practice
     * to create new ones via {@link #newTemplate()}.
     *
     * @param <T> type of object according to which the created text models are formatted
     *
     * @apiNote this is not expected to be used in multi-threaded environment as it is (in most cases) meaningless
     * @apiNote this object is allowed to perform its optimizations such as joining nearby static text blocks
     */
    interface TextModelTemplate<T> {

        /**
         * Appends a block of static text.
         *
         * @param staticText static text in the added {@link TextModel text model} block
         * @return this text model template
         */
        TextModelTemplate<T> append(@NonNull String staticText);

        /**
         * Appends a possibly dynamic {@link TextModel text model} bolck.
         *
         * @param dynamicText function dynamically providing text
         * @return this text model template
         */
        TextModelTemplate<T> append(@NonNull TextModel<T> dynamicText);

        /**
         * Clears this template allowing its reuse.
         *
         * @return this text model cleared from any contents
         */
        TextModelTemplate<T> clear();

        /**
         * Creates a new {@link TextModel text model} from this template.
         *
         * @return text model created from this template
         *
         * @apiNote calls to this method allow reuse of this template;
         * if not planning to reuse it better use {@link #createAndRelease()} instead
         */
        TextModel<T> create();

        /**
         * Creates a new {@link TextModel text model} from this template releasing itself.
         *
         * @return text model created from this template
         *
         * @apiNote this method differs from {@link #create()} as call to it guarantees
         * that this template won't be used anymore
         */
        TextModel<T> createAndRelease();
    }
}
