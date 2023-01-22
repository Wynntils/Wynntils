/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

import java.util.Objects;

public class CappedValue {
    public static final CappedValue EMPTY = new CappedValue(0, 0);
    private int current;
    private int max;

    public CappedValue(int current, int max) {
        this.current = current;
        this.max = max;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    @Override
    public String toString() {
        return "[" + current + "/" + max + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CappedValue that = (CappedValue) o;
        return current == that.current && max == that.max;
    }

    @Override
    public int hashCode() {
        return Objects.hash(current, max);
    }
}
