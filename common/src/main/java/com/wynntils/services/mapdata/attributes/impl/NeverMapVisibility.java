/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.impl;

import com.wynntils.services.mapdata.attributes.type.MapVisibility;

public final class NeverMapVisibility implements MapVisibility {
    @Override
    public float getVisibility(float zoomLevel) {
        return 0f;
    }
}
