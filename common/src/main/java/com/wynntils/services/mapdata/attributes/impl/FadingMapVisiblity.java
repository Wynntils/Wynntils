/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.impl;

import com.wynntils.services.mapdata.attributes.type.MapVisibility;

public class FadingMapVisiblity implements MapVisibility {
    private final float min;
    private final float max;
    private final float fade;

    public FadingMapVisiblity(float min, float max, float fade) {
        this.min = min;
        this.max = max;
        this.fade = fade;
    }

    @Override
    public float getVisibility(float zoomLevel) {
        float startFadeIn = min - fade / 2;
        float stopFadeIn = min + fade / 2;
        float startFadeOut = max - fade / 2;
        float stopFadeOut = max + fade / 2;

        if (zoomLevel < startFadeIn) {
            return 0;
        }
        if (zoomLevel < stopFadeIn) {
            // The visibility should be linearly interpolated between 0 and 1 for values
            // between startFadeIn and stopFadeIn.
            return (zoomLevel - startFadeIn) / fade;
        }

        if (zoomLevel < startFadeOut) {
            return 1;
        }

        if (zoomLevel < stopFadeOut) {
            // The visibility should be linearly interpolated between 1 and 0 for values
            // between startFadeIn and stopFadeIn.
            return 1 - (zoomLevel - startFadeOut) / fade;
        }

        return 0;
    }
}
