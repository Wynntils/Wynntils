/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.objects;

@FunctionalInterface
public interface PentaConsumer<A, B, C, D, E> {
    void consume(A a, B b, C c, D d, E e);
}
