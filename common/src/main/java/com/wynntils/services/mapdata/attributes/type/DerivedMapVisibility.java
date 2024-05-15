/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.type;

public final class DerivedMapVisibility extends MapVisibility {
    private final Float min;
    private final Float max;
    private final Float fade;

    private DerivedMapVisibility(Float min, Float max, Float fade) {
        this.min = min;
        this.max = max;
        this.fade = fade;
    }

    public static DerivedMapVisibility of(FullMapVisibility visibility) {
        return new DerivedMapVisibility(visibility.getMin(), visibility.getMax(), visibility.getFade());
    }

    public static DerivedMapVisibility withMin(Float min, Float max) {
        return new DerivedMapVisibility(min, max, null);
    }

    public static DerivedMapVisibility withMax(Float min, Float max) {
        return new DerivedMapVisibility(min, max, null);
    }

    public static DerivedMapVisibility withMinMax(Float min, Float max) {
        return new DerivedMapVisibility(min, max, null);
    }

    public static DerivedMapVisibility withFade(Float min, Float fade) {
        return new DerivedMapVisibility(min, null, fade);
    }

    @Override
    public float getMin() {
        return min == null ? -1 : min;
    }

    @Override
    public float getMax() {
        return max == null ? -1 : max;
    }

    @Override
    public float getFade() {
        return fade == null ? -1 : fade;
    }
}
