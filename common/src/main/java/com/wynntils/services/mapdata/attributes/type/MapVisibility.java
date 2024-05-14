/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.type;

public final class MapVisibility {
    public static final MapVisibility NEVER = new MapVisibility(100, 0, 6);
    public static final MapVisibility ALWAYS = new MapVisibility(0, 100, 6);

    private final float min;
    private final float max;
    private final float fade;

    public MapVisibility(float min, float max, float fade) {
        this.min = min;
        this.max = max;
        this.fade = fade;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public float getFade() {
        return fade;
    }
}
