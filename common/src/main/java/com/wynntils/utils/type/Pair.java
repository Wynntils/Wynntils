/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

import com.mojang.serialization.Codec;

/**
 * The Pair Type Holds 1 field of type T and 1 field of type J
 */
public record Pair<T, J>(T a, J b) {
    public static <A, B> Codec<Pair<A, B>> codec(Codec<A> aCodec, Codec<B> bCodec) {
        return Codec.pair(aCodec, bCodec)
                .xmap(p -> new Pair<>(p.getFirst(), p.getSecond()), p -> com.mojang.datafixers.util.Pair.of(p.a, p.b));
    }

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
