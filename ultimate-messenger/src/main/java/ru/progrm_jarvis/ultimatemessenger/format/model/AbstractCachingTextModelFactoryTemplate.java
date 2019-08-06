package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

import javax.annotation.OverridingMethodsMustInvokeSuper;

/**
 * Common abstract base for {@link TextModelFactory.TextModelTemplate} capable of caching.
 *
 * @param <T> type of object according to which the created text models are formatted
 */
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class AbstractCachingTextModelFactoryTemplate<T> implements TextModelFactory.TextModelTemplate<T> {

    @Nullable transient TextModel<T> cachedTextModel;

    /**
     * Method called whenever an update to template's content happens.
     *
     * @apiNote all {@code append(}<i>...</i>{@code )} methods should invoke this method
     * whenever the template gets updated
     */
    @OverridingMethodsMustInvokeSuper
    protected void markAsChanged() {
        cachedTextModel = null;
    }

    /**
     * Creates new {@link TextModel} according to this template's state.
     * This should not handle caching to {@link #cachedTextModel} as this will be done by the calling method.
     *
     * @param release {@code true} if this template will be released after the call and {@code false} otherwise
     * @return created text model
     */
    protected abstract TextModel<T> createTextModel(boolean release);

    @Override
    public TextModel<T> createAndRelease() {
        // no need to cache as this method is guaranteed to be called at last on this instance
        // (at least in the given context, custom implementations may allow instance caching and reuse)
        return cachedTextModel == null ? createTextModel(true) : cachedTextModel;
    }

    @Override
    public TextModel<T> create() {
        // cache the computed value so that it does not get computed again if attempt to create the same model happens
        return cachedTextModel == null ? cachedTextModel = createTextModel(false) : cachedTextModel;
    }
}
