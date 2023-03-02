/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

public record CappedValue(int current, int max) {
    public static final CappedValue EMPTY = new CappedValue(0, 0);

    public int getPercentage() {
        return Math.round((float) current / max * 100.0f);
    }

    @Override
    public String toString() {
        return "[" + current + "/" + max + ']';
    }
}
