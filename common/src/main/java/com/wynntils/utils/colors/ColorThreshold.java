/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.colors;

public enum ColorThreshold {
    NINETY_FIVE(95f),
    NINETY_SIX(96f);

    private final float threshold;

    ColorThreshold(float threshold) {
        this.threshold = threshold;
    }

    public float getThreshold() {
        return threshold;
    }
}
