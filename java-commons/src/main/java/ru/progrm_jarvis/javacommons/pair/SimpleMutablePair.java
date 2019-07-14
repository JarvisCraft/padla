package ru.progrm_jarvis.javacommons.pair;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PROTECTED)
public class SimpleMutablePair<F, S> implements MutablePair<F, S> {

    @NonNull F first;
    @NonNull S second;
}
