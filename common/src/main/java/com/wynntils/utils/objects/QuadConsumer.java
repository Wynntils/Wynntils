/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.objects;

@FunctionalInterface
public interface QuadConsumer<A, B, C, D> {
    void consume(A a, B b, C c, D d);
}
