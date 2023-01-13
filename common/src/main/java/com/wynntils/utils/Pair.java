/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import java.util.Objects;

/**
 * The Pair Type Holds 1 field of type T and 1 field of type J
 */
public record Pair<T, J>(T a, J b) {

    @Override
    public String toString() {
        return "<" + a.toString() + ", " + b.toString() + ">";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (!(obj instanceof Pair<?, ?> other)) {
            return false;
        }

        return Objects.deepEquals(a, other.a) && Objects.deepEquals(b, other.b);
    }

    public static <T, J> Pair<T, J> of(T a, J b) {
        return new Pair<>(a, b);
    }
}
