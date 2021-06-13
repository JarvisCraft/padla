package ru.progrm_jarvis.javacommons.recursion;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.primitive.wrapper.ReferenceWrapper;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Utilities for performing common recursive operations.
 *
 * @author xdark
 * @author progrm_jarvis
 */
@UtilityClass
public class Recursions {

    /**
     * Recursively traverses the sources evaluating to the recursive hierarchy.
     * The provided {@link Stream} will attempt to be as lazy as possible.
     *
     * @param sources stream of sources which should be traversed recursively
     * @param digger function used to generate the stream of child elements from the base one
     * @param <S> type of source elements
     * @return stream of recursive hierarchy
     *
     * @throws NullPointerException if {@code sources} is {@code null}
     * @throws NullPointerException if {@code digger} is {@code null}
     * @see #recurse(Object, Function) equivalent method with single source
     */
    public <S> Stream<S> recurse(
            final @NonNull Stream<? extends S> sources,
            final @NonNull Function<? super S, @NotNull Stream<? extends S>> digger
    ) {
        return sources.flatMap(source -> lazyRecursiveStep(source, digger));
    }

    /**
     * <p>Recursively traverses the source evaluating to the recursive hierarchy.
     * The provided {@link Stream} will attempt to be as lazy as possible.</p>
     * <p>An example providing the stream of class hierarchy of {@link String} class:
     * <pre>{@code
     * Recursions.<Class<?>, Method>recurse(
     *         String.class,
     *         clazz -> {
     *             final Class<?> superClass;
     *             return Stream.concat(
     *                     (superClass = clazz.getSuperclass()) == null
     *                             ? Stream.empty() : Stream.of(superClass),
     *                     Arrays.stream(clazz.getInterfaces())
     *             );
     *         }
     * )
     * }</pre>
     * </p>
     *
     * @param source source element used
     * @param digger function used to generate the stream of child elements from the base one
     * @param <S> type of source elements
     * @return stream of recursive hierarchy
     *
     * @throws NullPointerException if {@code digger} is {@code null}
     * @see #recurse(Stream, Function) equivalent method with {@link Stream} source
     */
    public <S> Stream<S> recurse(
            final S source,
            final @NonNull Function<? super S, @NotNull Stream<? extends S>> digger
    ) {
        return lazyRecursiveStep(source, digger);
    }

    /**
     * Recursively traverses the sources evaluating to the {@link Stream stream} of hierarchy members' components.
     *
     * @param sources stream of sources which should be traversed recursively
     * @param digger function used to generate the stream of child elements from the base one
     * @param elementGetter function used to get the elements from a source
     * @param <S> type of source elements
     * @param <E> type of resulting elements
     * @return stream of elements got from recursive hierarchy
     *
     * @throws NullPointerException if {@code sources} is {@code null}
     * @throws NullPointerException if {@code digger} is {@code null}
     * @throws NullPointerException if {@code elementGetter} is {@code null}
     * @see #recurseFully(Object, Function, Function) equivalent method with single source
     */
    public <S, E> Stream<E> recurseFully(
            final @NonNull Stream<? extends S> sources,
            final @NonNull Function<? super S, @NotNull Stream<? extends S>> digger,
            final @NonNull Function<? super S, @NotNull Stream<? extends E>> elementGetter
    ) {
        return recurseFullyInternal(sources.map(SourceOrElement::source), digger, elementGetter);
    }

    /**
     * <p>Recursively traverses the source evaluating to the {@link Stream stream} hierarchy members' components.</p>
     * <p>An example providing the stream of all declared method in {@link String} class hierarchy:
     * <pre>{@code
     * Recursions.<Class<?>, Method>recurseFully(
     *         String.class,
     *         clazz -> {
     *             final Class<?> superClass;
     *             return Stream.concat(
     *                     (superClass = clazz.getSuperclass()) == null
     *                             ? Stream.empty() : Stream.of(superClass),
     *                     Arrays.stream(clazz.getInterfaces())
     *             );
     *         },
     *         clazz -> Arrays.stream(clazz.getDeclaredMethods())
     * )
     * }</pre>
     * </p>
     *
     * @param source source element used
     * @param digger function used to generate the stream of child elements from the base one
     * @param elementGetter function used to get the elements from a source
     * @param <S> type of source elements
     * @param <E> type of resulting elements
     * @return stream of elements got from recursive hierarchy
     *
     * @throws NullPointerException if {@code digger} is {@code null}
     * @throws NullPointerException if {@code elementGetter} is {@code null}
     * @see #recurseFully(Stream, Function, Function) equivalent method with {@link Stream} source
     */
    public <S, E> Stream<E> recurseFully(
            final S source,
            final @NonNull Function<? super S, @NotNull Stream<? extends S>> digger,
            final @NonNull Function<? super S, @NotNull Stream<? extends E>> elementGetter
    ) {
        return recurseFullyInternal(Stream.of(SourceOrElement.source(source)), digger, elementGetter);
    }

    /**
     * Performs a lazy <i>recursive step</i>.
     *
     * @param source source element used
     * @param digger function used to generate the stream of child elements from the base one
     * @param <S> type of source elements
     * @return stream of elements in recursive hierarchy
     */
    private <S> Stream<S> lazyRecursiveStep(
            final S source,
            final @NotNull Function<? super S, @NotNull Stream<? extends S>> digger
    ) {
        return Stream.concat(
                Stream.of(source),
                Stream.of(source).flatMap(digger).flatMap(child -> lazyRecursiveStep(child, digger))
        );
    }

    /**
     * Recursively traverses the sources evaluating to the stream of results.
     *
     * @param source source element used
     * @param digger function used to generate the stream of child elements from the base one
     * @param elementGetter function used to get the elements from a source
     * @param <S> type of source elements
     * @param <E> type of resulting elements
     * @return stream of elements got from recursive hierarchy
     */
    private <S, E> @NotNull Stream<E> recurseFullyInternal(
            final @NotNull Stream<@NotNull SourceOrElement<S, E>> source,
            final @NotNull Function<? super S, @NotNull Stream<? extends S>> digger,
            final @NotNull Function<? super S, @NotNull Stream<? extends E>> elementGetter
    ) {
        final ReferenceWrapper<Function<SourceOrElement<S, E>, Stream<SourceOrElement<S, E>>>> mapperReference;
        (mapperReference = ReferenceWrapper.create())
                .set(sourceOrElement -> sourceOrElement.isSource() ? Stream.concat(
                        // recursively put child sources
                        digger.apply(sourceOrElement.asSource())
                                .<SourceOrElement<S, E>>map(SourceOrElement::source)
                                .flatMap(mapperReference.get()),
                        // put finite elements being looked up
                        elementGetter.apply(sourceOrElement.asSource())
                                .map(SourceOrElement::element)
                ) : /* no-op for finite elements in the stream */ Stream.of(sourceOrElement));

        return source
                .flatMap(mapperReference.get())
                // note: another `flatMap` could be used instead,
                // but filter + map is more reasonable here as it minimizes the amount of allocations
                // as they would be required per each element wrapped into a temporary single-element `Stream`
                .filter(SourceOrElement::isElement)
                .map(SourceOrElement::asElement);
    }

    /**
     * Either a source or an element.
     *
     * @param <S> type of source element
     * @param <E> type of target element
     */
    private interface SourceOrElement<S, E> {

        /**
         * Returns whether this is a source.
         *
         * @return {@code true} if this a source and {@code false} otherwise
         */
        boolean isSource();

        /**
         * Returns whether this is an element.
         *
         * @return {@code true} if this an element and {@code false} otherwise
         */
        boolean isElement();

        /**
         * Gets the value as a source.
         *
         * @return the value as a source
         *
         * @apiNote call on value which is not a {@link #isSource() source} is undefined behaviour
         */
        S asSource();

        /**
         * Gets the value as an element.
         *
         * @return the value as an element
         *
         * @apiNote call on value which is not an {@link #isElement() element} is undefined behaviour
         */
        E asElement();

        static <S, E> Recursions.SourceOrElement<S, E> source(final S source) {
            return new TaggedSourceOrElement<>(false, source);
        }

        static <S, E> Recursions.SourceOrElement<S, E> element(final E element) {
            return new TaggedSourceOrElement<>(true, element);
        }

        /**
         * {@link SourceOrElement} which identifies its type by having a <i>tag</i> field.
         *
         * @param <S> type of source element
         * @param <E> type of target element
         * @implNote unlike {@link ru.progrm_jarvis.javacommons.object.Result} which uses different classes for states,
         * tag-based implementation is more preferred here as there is no need gor guaranteeing the invariant
         * outside this class thus typed access is effectively just field-access wrapped in a method call
         * which can easily be monomorphized by JIT
         */
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) final class TaggedSourceOrElement<S, E>
                implements SourceOrElement<S, E> {
            @Getter boolean element;
            Object value;

            @Override
            public boolean isSource() {
                return !element;
            }

            @Override
            @SuppressWarnings("unchecked")
            public S asSource() {
                assert !element : "this is not a source";

                return (S) value;
            }

            @Override
            @SuppressWarnings("unchecked")
            public E asElement() {
                assert element : "this is not an element";

                return (E) value;
            }
        }
    }
}
