/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.type;

public final class FullMapVisibility extends MapVisibility {
    private final float min;
    private final float max;
    private final float fade;

    public FullMapVisibility(float min, float max, float fade) {
        this.min = min;
        this.max = max;
        this.fade = fade;
    }

    public FullMapVisibility applyDerived(DerivedMapVisibility visibility) {
        float newMin = visibility.getMin() != -1 ? visibility.getMin() : min;
        float newMax = visibility.getMax() != -1 ? visibility.getMax() : max;
        float newFade = visibility.getFade() != -1 ? visibility.getFade() : fade;

        return new FullMapVisibility(newMin, newMax, newFade);
    }

    @Override
    public float getMin() {
        return min;
    }

    @Override
    public float getMax() {
        return max;
    }

    @Override
    public float getFade() {
        return fade;
    }
}
