package ru.progrm_jarvis.ultimatemessenger.format.model;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.OverridingMethodsMustInvokeSuper;

/**
 * Common abstract base for {@link TextModelFactory.TextModelBuilder} capable of caching.
 *
 * @param <T> type of object according to which the created text models are formatted
 */
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class AbstractCachingTextModelFactoryBuilder<T> implements TextModelFactory.TextModelBuilder<T> {

    /**
     * Cached instance of the last created text model reset on change
     */
    transient @Nullable TextModel<T> cachedTextModel;

    /**
     * Method called whenever an update to text model builder's content happens.
     *
     * @apiNote all {@code append(}<i>...</i>{@code )} methods should invoke this method
     * whenever the text model builder gets updated
     */
    @OverridingMethodsMustInvokeSuper
    protected void markAsChanged() {
        cachedTextModel = null;
    }

    /**
     * Creates new {@link TextModel} according to this text model builder's state.
     * This should not handle caching to {@link #cachedTextModel} as this will be done by the calling method.
     *
     * @param release {@code true} if this text model builder will be released after the call and {@code false} otherwise
     * @return created text model
     */
    protected abstract @NotNull TextModel<T> buildTextModel(boolean release);

    @Override
    public @NotNull TextModel<T> buildAndRelease() {
        // no need to cache as this method is guaranteed to be called at last on this instance
        // (at least in the given context, custom implementations may allow instance caching and reuse)
        return cachedTextModel == null ? buildTextModel(true) : cachedTextModel;
    }

    @Override
    public @NotNull TextModel<T> build() {
        // cache the computed value so that it does not get computed again if attempt to create the same model happens
        return cachedTextModel == null ? cachedTextModel = buildTextModel(false) : cachedTextModel;
    }
}
