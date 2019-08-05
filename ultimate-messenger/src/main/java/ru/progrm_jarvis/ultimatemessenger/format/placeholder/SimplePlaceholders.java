package ru.progrm_jarvis.ultimatemessenger.format.placeholder;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.ultimatemessenger.format.StringFormatter;
import ru.progrm_jarvis.ultimatemessenger.format.model.TextModel;
import ru.progrm_jarvis.ultimatemessenger.format.model.TextModelFactory;
import ru.progrm_jarvis.ultimatemessenger.format.model.TextModelParser;
import ru.progrm_jarvis.ultimatemessenger.format.util.StringMicroOptimizationUtil;

import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * {@link Placeholders} implementation which recognized placeholders by the given prefix and suffix
 * and allows usage of escape character in order to allow raw usage of those.
 *
 * @param <T> type of the object according to which the string should be formatted
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class SimplePlaceholders<T> implements Placeholders<T>, TextModelParser<T> {

    /**
     * Formatters used for handling placeholders which accept placeholder value and formatting target
     */
    @NonNull @Singular Map<String, StringFormatter<T>> handlers;
    /**
     * Prefix of placeholders
     */
    @Builder.Default char prefix = '{',
    /**
     * Suffix of placeholders
     */
    suffix = '}',
    /**
     * Delimiter separating placeholders' keys from values
     */
    delimiter = ':',
    /**
     * Character used for escaping other characters (including itself)
     */
    escapeCharacter = '\\',
    /* ********************************************* Special characters ********************************************* */
    /**
     * Tab character ({@code \t})
     */
    tabCharacter = 't',
    /**
     * Backspace character ({@code \b})
     */
    backspaceCharacter = 'b',
    /**
     * New line character ({@code \n})
     */
    newLineCharacter = 'n',
    /**
     * Carriage return character ({@code \r})
     */
    carriageReturnCharacter = 'r',
    /**
     * Form feed character ({@code \f})
     */
    formFeedCharacter = 'f';
    @Builder.Default @NonNull String unknownPlaceholderReplacement = "???";

    @Override
    public String format(@NotNull String source, final T target) {
        if (source.isEmpty()) return source;

        @Nullable StringBuilder result = null;
        val characters = StringMicroOptimizationUtil.getStringChars(source);
        {
            @Nullable StringBuilder unescapedPlaceholder;
            // micro-optimization: out-of-loop dynamic variable
            @Nullable String placeholder /* also reused as its key */, value;
            @Nullable StringFormatter<T> formatter;
            boolean inPlaceholder = false, escaping = false;
            char character;
            int lastWriteIndex = -1 /* always in length bounds when result != null */,
                    placeholderStartIndex = -1, delimiterIndex, escapeCount = 0;
            for (var index = 0; index < characters.length; index++) {
                character = characters[index];

                // ... if the current character is an escape one, switch to escape mode
                if (inPlaceholder) { // handle placeholder logic
                    // ... for escape character increment the amount of escape characters in the placeholder ...
                    // ... also toggling the escape mode (to use it for making sure the next token isn't a suffix)
                    if (escaping) escaping = false;
                    else if (character == suffix) { // handle end of placeholder
                        format:
                        {
                            if (index == placeholderStartIndex + 1) break format; // handle "PrefSuf"
                            if (index == placeholderStartIndex + 2) { // handle "PrefCharSuf"
                                // rare case
                                val singleChar = source.charAt(index - 1);
                                if (singleChar == delimiter) break format; // "PrefDelSuf"

                                value = "";
                                placeholder = Character.toString(singleChar);
                            } else { // handle "PrefPlaceholderSuf"
                                placeholder = source.substring(placeholderStartIndex + 1, index);
                                if (escapeCount > 0) {
                                    unescapedPlaceholder = new StringBuilder(placeholder.length() - escapeCount);
                                    for (val placeholderChar : placeholder.toCharArray())
                                        if (placeholderChar
                                                != escapeCharacter) unescapedPlaceholder.append(placeholderChar);
                                    placeholder = unescapedPlaceholder.toString();
                                }
                                // find index of delimiter
                                if ((delimiterIndex = placeholder.indexOf(delimiter)) == 0) break format; // ...
                                // ... it was "PrefDel...Suf" (no placeholder)

                                if (delimiterIndex == -1) value = ""; // "PrefKeySuf"
                                else { // PrefKeyDelValSuf
                                    value = placeholder.substring(delimiterIndex + 1);
                                    placeholder = placeholder.substring(0, delimiterIndex); // placeholder <~ key
                                }
                            }

                            // apply formatter if it is present
                            formatter = handlers.get(placeholder);

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
    public TextModel<T> parse(@NotNull final TextModelFactory<T> factory,
                              @NonNull final String text) {
        if (text.isEmpty()) return factory.empty();

        val template = factory.newTemplate();
        {
            val characters = StringMicroOptimizationUtil.getStringChars(text);
            boolean escaping = false, inPlaceholder = false;
            @Nullable StringBuilder lastRawText = null, unescapedPlaceholder; // currently read text or
            @Nullable String placeholder /* also reused as placeholder key */, value;
            int lastFlushIndex = -1, // index of the last flushed (written) character
                    placeholderStartIndex = -1, // index at which the currently scanned placeholder starts
                    delimiterIndex, // index of the delimiter contextually
                    escapeCount = 0; // amount of escapes inside the placeholder
            val length = characters.length;
            char character;
            for (var index = 0; index < length; index++) {
                character = characters[index];
                if (inPlaceholder) {
                    if (escaping) escaping = false;
                    else {
                        if (character == suffix) {
                            format:
                            {
                                // make sure this is a placeholder
                                if (index == placeholderStartIndex + 1) break format; // it was not a placeholder ...
                                // ... but just a sequence "PrefSuf"
                                if (index == placeholderStartIndex + 2) {
                                    // rare case
                                    val singleChar = text.charAt(index - 1);
                                    if (singleChar == delimiter) break format; // "PrefDelSuf"

                                    value = "";
                                    placeholder = Character.toString(singleChar);
                                } else {
                                    placeholder = text.substring(placeholderStartIndex + 1, index);
                                    if (escapeCount > 0) {
                                        unescapedPlaceholder = new StringBuilder(placeholder.length() + escapeCount);
                                        for (val placeholderChar : placeholder.toCharArray()) if (placeholderChar
                                                != escapeCharacter) unescapedPlaceholder.append(placeholderChar);
                                        placeholder = unescapedPlaceholder.toString();
                                    }
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
                                if (placeholderStartIndex - 1 != lastFlushIndex) {
                                    if (lastRawText == null) template.append(
                                            text.substring(lastFlushIndex + 1, placeholderStartIndex)
                                    );
                                    else {
                                        template.append(
                                                lastRawText.append(text, lastFlushIndex + 1, placeholderStartIndex)
                                                        .toString()
                                        );
                                        // reset `lastRawText` so that it is reused effectively
                                        lastRawText.delete(0, lastRawText.length());
                                    }
                                }
                                // 2.) mark last flushed index at the position of the placeholder end
                                lastFlushIndex = index;
                                // add the very element
                                {
                                    val finalKey = placeholder;
                                    val finalValue = value;
                                    template.append(target -> {
                                        val formatter = handlers.get(finalKey);

                                        return formatter == null
                                                ? unknownPlaceholderReplacement // replacement for unknown placeholder
                                                : formatter.format(finalValue, target); // normal placeholder handling
                                    });
                                }
                            }

                            inPlaceholder = false;
                        } else if (escaping = character == escapeCharacter) escapeCount++;
                    }
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
                if (lastFlushIndex != length) template.append(text.substring(lastFlushIndex + 1));
            } else template.append(lastRawText.append(text.substring(lastFlushIndex + 1)).toString());
        }

        return template.createAndRelease();
    }

    @Override
    public void add(@NonNull final String name, @NonNull final StringFormatter<T> formatter) {
        checkArgument(
                name.indexOf(escapeCharacter) == -1, "name should not contain escape character (%s)", escapeCharacter
        );
        checkArgument(!name.isEmpty(), "name should not be empty (%s)");

        handlers.put(name, formatter);
    }

    @Override
    public Optional<StringFormatter<T>> get(@NonNull final String name) {
        return Optional.ofNullable(handlers.get(name));
    }

    @Override
    public Optional<StringFormatter<T>> remove(@NonNull final String name) {
        return Optional.ofNullable(handlers.remove(name));
    }
}
