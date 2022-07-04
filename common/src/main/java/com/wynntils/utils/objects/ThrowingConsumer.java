/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.objects;

@FunctionalInterface
public interface ThrowingConsumer<T, EXC extends Throwable> {
    void accept(T t) throws EXC;
}
