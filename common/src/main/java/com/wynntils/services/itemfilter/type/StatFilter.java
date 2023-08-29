/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.type;

import java.util.List;

/**
 * A filter type to be used for filtering {@link ItemStatProvider} values.
 * @param <T>
 */
public interface StatFilter<T> {
    boolean matches(T value);

    default boolean matches(List<T> values) {
        return values.stream().anyMatch(this::matches);
    }
}
