/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

@FunctionalInterface
public interface QuadConsumer<A, B, C, D> {
    void consume(A a, B b, C c, D d);
}
