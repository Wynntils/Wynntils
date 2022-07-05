/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.objects;

@FunctionalInterface
public interface ThrowingBiPredicate<T, U, E extends Throwable> {
    boolean test(T t, U u) throws E;
}
