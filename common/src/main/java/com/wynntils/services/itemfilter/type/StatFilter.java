/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.type;

/**
 * A filter type to be used for filtering {@link ItemStatProvider} values.
 * Create these with {@link StatFilterFactory}.
 * @param <T> The type of value this filter works on
 */
public abstract class StatFilter<T> {
    public abstract boolean matches(T value);

    public abstract String asString();
}
