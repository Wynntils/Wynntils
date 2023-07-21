/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.type;

import com.wynntils.models.mapdata.attributes.type.MapAttributes;

public interface MapCategory {
    // Required
    String getCategoryId();

    // Optional
    String getName();

    // Optional
    MapAttributes getAttributes();
}
