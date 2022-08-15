/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> {
    void accept(T t) throws E;
}
