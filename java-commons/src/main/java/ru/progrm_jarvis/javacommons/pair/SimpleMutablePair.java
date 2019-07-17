package ru.progrm_jarvis.javacommons.pair;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data(staticConstructor = "of")
@FieldDefaults(level = AccessLevel.PROTECTED)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SimpleMutablePair<F, S> implements MutablePair<F, S> {

    @NonNull F first;
    @NonNull S second;
}
