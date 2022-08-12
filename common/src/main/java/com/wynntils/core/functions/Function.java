/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functions;

public interface Function<T> {
    T getValue(String argument);

    String getName();

    String getDescription();

    default void init() {}
}
