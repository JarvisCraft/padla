package ru.progrm_jarvis.javacommons.pair;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Value
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SimplePair<F, S> implements Pair<F, S> {

    @NonNull F first;
    @NonNull S second;
}
