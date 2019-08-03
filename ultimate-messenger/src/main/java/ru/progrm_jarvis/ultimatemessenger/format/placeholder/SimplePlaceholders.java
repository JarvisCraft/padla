package ru.progrm_jarvis.ultimatemessenger.format.placeholder;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.progrm_jarvis.ultimatemessenger.format.StringFormatter;

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
public class SimplePlaceholders<T> implements Placeholders<T> {

    @NonNull Map<String, StringFormatter<T>> formatters;
    char prefix, suffix, delimiter, escapeCharacter;

    @Override
    public String format(@NotNull String source, final T target) {
        @Nullable StringBuilder result = null, unescapedPlaceholder;
        val characters = source.toCharArray();
        {
            // micro-optimization: out-of-loop dynamic variable
            @Nullable String placeholder, key, value;
            @Nullable StringFormatter<T> formatter;
            boolean inPlaceholder = false, escaping = false;
            char character;
            int lastWriteIndex = -1 /* always in length bounds when result != null */,
                    placeholderStartIndex = -1, delimiterIndex, escapeCount = 0;
            for (var index = 0; index < characters.length; index++) {
                character = characters[index];

                if (inPlaceholder) { // handle placeholder logic
                    if (!escaping) {
                        if (character == suffix) { // handle end of placeholder
                            format: {
                                if (index == placeholderStartIndex + 1) break format; // handle "PrefSuf"
                                if (index == placeholderStartIndex + 2) { // handle "PrefCharSuf"
                                    key = Character.toString(source.charAt(index - 1));
                                    value = null;
                                } else { // handle "PrefPlaceholderSuf"
                                    placeholder = source.substring(placeholderStartIndex + 1, index);
                                    if (escapeCount > 0) {
                                        unescapedPlaceholder = new StringBuilder(placeholder.length() + escapeCount);
                                        for (val placeholderChar : placeholder.toCharArray()) if (placeholderChar
                                                != escapeCharacter) unescapedPlaceholder.append(placeholderChar);
                                        placeholder = unescapedPlaceholder.toString();
                                    }
                                    // find index of delimiter
                                    if ((delimiterIndex = placeholder.indexOf(delimiter)) == 0) break format; // ...
                                    // ... it was "PrefDel...Suf" (no placeholder)

                                    if (delimiterIndex == -1) { // "PrefKeySuf"
                                        key = placeholder;
                                        value = null;
                                    } else { // PrefKeyDelValSuf
                                        key = placeholder.substring(0, delimiterIndex);
                                        value = placeholder.substring(delimiterIndex + 1);
                                    }
                                }

                                // apply formatter if it is present
                                formatter = formatters.get(key);
                                if (formatter == null) break format;

                                /*
                                 * Finally update the result
                                 * Note: all source updates happen here so that upper `break format;`s work
                                 */

                                // "no placeholders {placeholder}"
                                if (result == null) result = new StringBuilder(source.substring(0, placeholderStartIndex));
                                    // "some placeholders {placeholder}"
                                else result.append(source, lastWriteIndex + 1, placeholderStartIndex);

                                // append the very placeholder
                                result.append(formatter.apply(value, target));
                                lastWriteIndex = index; // mark placeholder end as the index of last written character
                            }

                            inPlaceholder = false;
                            // placeholderStart has no need to be reset as it updates with inPlaceholder
                        } else if (escaping = character == escapeCharacter) escapeCount++; // ...
                        // ... for escape character increment the amount of escape characters in the placeholder ...
                        // ... also toggling the escape mode (to use it for making sure the next token isn't a suffix)
                    } else escaping = false;
                } else {
                    if (escaping) { // handle escaping if in escape mode and not inside of placeholder
                        // reset escape state
                        escaping = false;
                        // initialize result if needed
                        if (result == null) result                /* not include current char & escape char */
                                = new StringBuilder(source.substring(0, (lastWriteIndex = index) - 1));
                        else result.append(source, lastWriteIndex + 1, (lastWriteIndex = index) - 1);
                        result.append(character);
                    } else if (character == prefix) { // enable placeholder mode thanks to prefix
                        inPlaceholder = true;
                        // reset escape count
                        escapeCount = 0;
                        // mark the start index of the placeholder to the current position
                        placeholderStartIndex = index;
                    } else escaping = character == escapeCharacter; // otherwise, ...
                    // ... if the current character is an escape one, switch to escape mode
                }
            }

            // add missing string part (from index of last written character) if the result was initialized
            if (result != null && lastWriteIndex != -1) result.append(source.substring(lastWriteIndex + 1));
        }

        return result == null ? source : result.toString();
    }

    @Override
    public void add(@NonNull final String name, @NonNull final StringFormatter<T> formatter) {
        checkArgument(
                name.indexOf(escapeCharacter) == -1, "name should not contain escape character (%s)", escapeCharacter
        );
        checkArgument(!name.isEmpty(), "name should not be empty (%s)");

        formatters.put(name, formatter);
    }

    @Override
    public Optional<StringFormatter<T>> get(@NonNull final String name) {
        return Optional.ofNullable(formatters.get(name));
    }

    @Override
    public Optional<StringFormatter<T>> remove(@NonNull final String name) {
        return Optional.ofNullable(formatters.remove(name));
    }
}
