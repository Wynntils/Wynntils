/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.type;

import com.wynntils.models.mapdata.type.attributes.MapFeatureAttributes;

public interface MapFeatureCategory {
    // Required
    String getCategoryId();

    // Optional
    String getDisplayName();

    // Optional
    MapFeatureAttributes getAttributes();
}
