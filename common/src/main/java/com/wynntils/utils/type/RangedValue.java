/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

import java.util.Objects;

public record RangedValue(int low, int high) {
    public static final RangedValue NONE = new RangedValue(0, 0);

    public static RangedValue of(int low, int high) {
        return new RangedValue(low, high);
    }

    public static RangedValue fromString(String range) {
        String[] pair = range.split("-");
        return new RangedValue(Integer.parseInt(pair[0]), Integer.parseInt(pair[1]));
    }

    public String asString() {
        return low + "-" + high;
    }

    public boolean isFixed() {
        return low == high;
    }

    @Override
    public String toString() {
        return "<" + low + "-" + high + '>';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RangedValue that = (RangedValue) o;
        return low == that.low && high == that.high;
    }

    @Override
    public int hashCode() {
        return Objects.hash(low, high);
    }
}
