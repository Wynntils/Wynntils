/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

/**
 * The Pair Type Holds 1 field of type T and 1 field of type J
 */
public record Pair<T, J>(T a, J b) {
    public static <T, J> Pair<T, J> of(T a, J b) {
        return new Pair<>(a, b);
    }

    // Convenience aliases for typical usage
    public T key() {
        return a;
    }

    public J value() {
        return b;
    }

    @Override
    public String toString() {
        return "<" + a.toString() + ", " + b.toString() + ">";
    }
}
