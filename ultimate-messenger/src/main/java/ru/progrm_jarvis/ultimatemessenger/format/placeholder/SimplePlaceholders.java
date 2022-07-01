package ru.progrm_jarvis.ultimatemessenger.format.placeholder;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.ultimatemessenger.format.StringFormatter;
import ru.progrm_jarvis.ultimatemessenger.format.model.TextModel;
import ru.progrm_jarvis.ultimatemessenger.format.model.TextModelFactory;
import ru.progrm_jarvis.ultimatemessenger.format.util.StringMicroOptimizationUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * {@link Placeholders} implementation which recognized placeholders by the given prefix and suffix
 * and allows usage of escape character in order to allow raw usage of those.
 *
 * @param <T> type of the object according to which the string should be formatted
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class SimplePlaceholders<T> implements Placeholders<T> {

    /**
     * Formatters used for handling placeholders which accept placeholder value and formatting target
     */
    // Note: @Singular can't be used here as Lombok does not allow further modifications to the created collection
    @Builder.Default @NonNull Map<String, StringFormatter<T>> handlers = new HashMap<>();
    /**
     * Prefix of placeholders
     */
    @Builder.Default char prefix = '{';

    /**
     * Suffix of placeholders
     */
    @Builder.Default char suffix = '}';

    /**
     * Delimiter separating placeholders' keys from values
     */
    @Builder.Default char delimiter = ':';

    /**
     * Character used for escaping other characters (including itself)
     */
    @Builder.Default char escapeCharacter = '\\';

    /* ********************************************* Special characters ********************************************* */

    /**
     * Tab character ({@code \t})
     */
    @Builder.Default char tabCharacter = 't';

    /**
     * Backspace character ({@code \b})
     */
    @Builder.Default char backspaceCharacter = 'b';

    /**
     * New line character ({@code \n})
     */
    @Builder.Default char newLineCharacter = 'n';

    /**
     * Carriage return character ({@code \r})
     */
    @Builder.Default char carriageReturnCharacter = 'r';

    /**
     * Form feed character ({@code \f})
     */
    @Builder.Default char formFeedCharacter = 'f';

    /**
     * Text used to replace occurrences of empty placeholders
     */
    @Builder.Default @NonNull String unknownPlaceholderReplacement = "<?>";

    @Override
    public @NotNull String format(final @NotNull String source, final T target) {
        if (source.isEmpty()) return source;

        @Nullable StringBuilder result = null;
        val characters = StringMicroOptimizationUtil.getStringChars(source);
        {
            // micro-optimization: out-of-loop dynamic variable
            boolean inPlaceholder = false, escaping = false;
            int lastWriteIndex = -1 /* always in length bounds when result != null */,
                    placeholderStartIndex = -1, escapeCount = 0;
            val charactersLength = characters.length;
            for (var index = 0; index < charactersLength; index++) {
                char character = characters[index];

                // ... if the current character is an escape one, switch to escape mode
                if (inPlaceholder) { // handle placeholder logic
                    // ... for escape character increment the amount of escape characters in the placeholder ...
                    // ... also toggling the escape mode (to use it for making sure the next token isn't a suffix)
                    if (escaping) escaping = false;
                    else if (character == suffix) { // handle end of placeholder
                        format:
                        {
                            if (index == placeholderStartIndex + 1) break format; // handle "PrefSuf"
                            String value;/* also reused as its key */
                            String placeholder;
                            if (index == placeholderStartIndex + 2) { // handle "PrefCharSuf"
                                // rare case
                                val singleChar = source.charAt(index - 1);
                                if (singleChar == delimiter) break format; // "PrefDelSuf"

                                value = "";
                                placeholder = Character.toString(singleChar);
                            } else { // handle "PrefPlaceholderSuf"
                                placeholder = source.substring(placeholderStartIndex + 1, index);
                                if (escapeCount > 0) {
                                    StringBuilder unescapedPlaceholder = new StringBuilder(
                                            placeholder.length() - escapeCount);
                                    for (val placeholderChar : placeholder.toCharArray())
                                        if (placeholderChar
                                                != escapeCharacter) unescapedPlaceholder.append(placeholderChar);
                                    placeholder = unescapedPlaceholder.toString();
                                }
                                // find index of delimiter
                                int delimiterIndex;
                                if ((delimiterIndex = placeholder.indexOf(delimiter)) == 0) break format; // ...
                                // ... it was "PrefDel...Suf" (no placeholder)

                                if (delimiterIndex == -1) value = ""; // "PrefKeySuf"
                                else { // PrefKeyDelValSuf
                                    value = placeholder.substring(delimiterIndex + 1);
                                    placeholder = placeholder.substring(0, delimiterIndex); // placeholder <~ key
                                }
                            }

                            // apply formatter if it is present
                            StringFormatter<T> formatter = handlers.get(placeholder);

                            /*
                             * Finally update the result
                             * Note: all source updates happen here so that upper `break format;`s work
                             */
                            (result == null
                                    // "no placeholders {placeholder}"
                                    ? result = new StringBuilder(source.substring(0, placeholderStartIndex))
                                    // "some placeholders {placeholder}"
                                    : result.append(source, lastWriteIndex + 1, placeholderStartIndex))
                                    .append(formatter == null
                                            ? unknownPlaceholderReplacement : formatter.format(value, target)
                                    );

                            lastWriteIndex = index; // mark placeholder end as the index of last written character
                        }

                        inPlaceholder = false;
                        // placeholderStart has no need to be reset as it updates with inPlaceholder
                    } else if (escaping = character == escapeCharacter) escapeCount++; // ...
                } else if (escaping) { // handle escaping if in escape mode and not inside of placeholder
                    // reset escape state
                    escaping = false;

                    // convert the current character to a special character if needed
                    if (character == tabCharacter) character = '\t';
                    else if (character == backspaceCharacter) character = '\b';
                    else if (character == newLineCharacter) character = '\n';
                    else if (character == carriageReturnCharacter) character = '\r';
                    else if (character == formFeedCharacter) character = '\f';

                    // update the result with the given text
                    (result == null                                 /* not include current char & escape char */
                            ? result = new StringBuilder(source.substring(0, (lastWriteIndex = index) - 1))
                            : result.append(source, lastWriteIndex + 1, (lastWriteIndex = index) - 1))
                            .append(character);
                } else if (character == prefix) { // enable placeholder mode thanks to prefix
                    inPlaceholder = true;
                    // reset escape count
                    escapeCount = 0;
                    // mark the start index of the placeholder to the current position
                    placeholderStartIndex = index;
                } else escaping = character == escapeCharacter; // otherwise, ...
            }

            // add missing string part (from index of last written character) if the result was initialized
            if (result != null && lastWriteIndex != -1) result.append(source.substring(lastWriteIndex + 1));
        }

        return result == null ? source : result.toString();
    }

    @Override
    public @NotNull TextModel<T> parse(final @NonNull TextModelFactory<T> factory, final @NonNull String text) {
        if (text.isEmpty()) return factory.empty();

        val builder = factory.newBuilder();
        {
            val characters = StringMicroOptimizationUtil.getStringChars(text);
            boolean escaping = false, inPlaceholder = false;
            @Nullable StringBuilder lastRawText = null;  // currently read text or
            int lastFlushIndex = -1, // index of the last flushed (written) character
                    placeholderStartIndex = -1, // index at which the currently scanned placeholder starts
                    // index of the delimiter contextually
                    escapeCount = 0; // amount of escapes inside the placeholder
            val length = characters.length;
            for (var index = 0; index < length; index++) {
                char character = characters[index];
                if (inPlaceholder) {
                    if (escaping) escaping = false;
                    else if (character == suffix) {
                        format:
                        {
                            // make sure this is a placeholder
                            if (index == placeholderStartIndex + 1) break format; // it was not a placeholder ...
                            // ... but just a sequence "PrefSuf"
                            String value /* also reused as placeholder key */, placeholder;
                            if (index == placeholderStartIndex + 2) {
                                // rare case
                                val singleChar = text.charAt(index - 1);
                                if (singleChar == delimiter) break format; // "PrefDelSuf"

                                value = "";
                                placeholder = Character.toString(singleChar);
                            } else {
                                placeholder = text.substring(placeholderStartIndex + 1, index);
                                if (escapeCount > 0) {
                                    val unescapedPlaceholder = new StringBuilder(
                                            placeholder.length() + escapeCount);
                                    for (val placeholderChar : placeholder.toCharArray()) if (placeholderChar
                                                != escapeCharacter) unescapedPlaceholder.append(placeholderChar);
                                    placeholder = unescapedPlaceholder.toString();
                                }
                                int delimiterIndex;
                                if ((delimiterIndex = placeholder.indexOf(delimiter)) == 0) break format; // ...
                                // ... it was not a placeholder but just a sequence "PrefDel...Suf"
                                if (delimiterIndex == -1) value = ""; // "PrefKeySuf"
                                else {
                                    value = placeholder.substring(delimiterIndex + 1);
                                    placeholder = placeholder.substring(0, delimiterIndex);
                                }


                            }
                            // close the placeholder:
                            // 1.) as it was an actual placeholder, close the previous text element (if it hasn't
                            // been yet)
                            if (lastRawText == null) {
                                if (lastFlushIndex != placeholderStartIndex - 1) builder
                                        .append(text.substring(lastFlushIndex + 1, placeholderStartIndex));
                            } else {
                                builder.append(
                                        lastRawText.append(text, lastFlushIndex + 1, placeholderStartIndex)
                                                .toString()
                                );
                                // reset `lastRawText` so that it is reused effectively
                                lastRawText.delete(0, lastRawText.length());
                            }

                            // 2.) mark last flushed index at the position of the placeholder end
                            lastFlushIndex = index;
                            // add the very element
                            {
                                val finalKey = placeholder;
                                val finalValue = value;
                                builder.append(target -> {
                                    val formatter = handlers.get(finalKey);

                                    return formatter == null
                                            ? unknownPlaceholderReplacement // replacement for unknown placeholder
                                            : formatter.format(finalValue, target); // normal placeholder handling
                                });
                            }
                        }

                        inPlaceholder = false;
                    } else if (escaping = character == escapeCharacter) escapeCount++;
                } else if (escaping) {
                    // reset escape state
                    escaping = false;

                    // convert the current character to a special character if needed
                    if (character == tabCharacter) character = '\t';
                    else if (character == backspaceCharacter) character = '\b';
                    else if (character == newLineCharacter) character = '\n';
                    else if (character == carriageReturnCharacter) character = '\r';

                    // update text according to escaping
                    (lastRawText == null
                            ? lastRawText = new StringBuilder(
                            text.substring(lastFlushIndex + 1, (lastFlushIndex = index) - 1))
                            : lastRawText.append(text, lastFlushIndex + 1, (lastFlushIndex = index) - 1))
                            .append(character);
                } else if (character == prefix) { // handle start of placeholder
                    inPlaceholder = true;
                    escapeCount = 0;
                    // mark the start index of the placeholder to the current position
                    placeholderStartIndex = index;
                } else escaping = character == escapeCharacter;
            }

            // add the end og the text if it was not
            if (lastRawText == null) {
                if (lastFlushIndex != length) builder.append(text.substring(lastFlushIndex + 1));
            } else builder.append(lastRawText.append(text.substring(lastFlushIndex + 1)).toString());
        }

        return builder.buildAndRelease();
    }

    @Override
    public void add(final @NonNull String name, final @NonNull StringFormatter<T> formatter) {
        if (name.indexOf(escapeCharacter) != -1) throw new IllegalArgumentException(
                "Placeholder name should not contain the escape character (" + escapeCharacter + ')'
        );
        if (name.isEmpty()) throw new IllegalArgumentException("Placeholder name should not be empty");

        handlers.put(name, formatter);
    }

    @Override
    public @NotNull Optional<StringFormatter<T>> get(final @NonNull String name) {
        return Optional.ofNullable(handlers.get(name));
    }

    @Override
    public @NotNull Optional<StringFormatter<T>> remove(final @NonNull String name) {
        return Optional.ofNullable(handlers.remove(name));
    }
}
