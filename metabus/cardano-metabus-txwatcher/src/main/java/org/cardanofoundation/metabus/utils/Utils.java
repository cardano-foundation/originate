package org.cardanofoundation.metabus.utils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Utils {
    private Utils() {

    }
    public static <T> List<T> filterNonNullValues(List<T> inputList) {
        return Optional.ofNullable(inputList)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(Objects::nonNull)
                .toList();
    }
}
