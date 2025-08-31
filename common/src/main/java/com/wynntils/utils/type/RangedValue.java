/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RangedValue(int low, int high) {
    public static final Codec<RangedValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("low").forGetter(RangedValue::low),
                    Codec.INT.fieldOf("high").forGetter(RangedValue::high))
            .apply(instance, RangedValue::new));

    public static final RangedValue NONE = new RangedValue(0, 0);

    public static RangedValue of(int low, int high) {
        return new RangedValue(low, high);
    }

    public static RangedValue fromString(String range) {
        String[] pair = range.split("-");
        return new RangedValue(Integer.parseInt(pair[0]), Integer.parseInt(pair[1]));
    }

    public boolean isFixed() {
        return low == high;
    }

    public boolean inRange(int value) {
        return value >= low && value <= high;
    }

    public String asString() {
        return low + "-" + high;
    }

    @Override
    public String toString() {
        return "<" + low + "-" + high + '>';
    }
}
