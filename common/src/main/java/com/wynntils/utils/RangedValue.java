/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

public record RangedValue(int low, int high) {
    public static final RangedValue NONE = new RangedValue(0, 0);
}
