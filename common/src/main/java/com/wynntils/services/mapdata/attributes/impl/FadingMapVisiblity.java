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
        // A feature stats fading in at min - fade, is fully visible at min,
        // and starts to fade out at max, and is fully invisible at max + fade

        // Fully invisible (zoom level is too low)
        if (zoomLevel < min - fade) {
            return 0;
        }

        // Start fading in
        if (zoomLevel < min) {
            return (zoomLevel - (min - fade)) / fade;
        }

        // Fully visible
        if (zoomLevel < max) {
            return 1;
        }

        // Start fading out
        if (zoomLevel < max + fade) {
            return 1 - (zoomLevel - max) / fade;
        }

        // Fully invisible (zoom level is too high)
        return 0;
    }
}
