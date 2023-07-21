/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.attributes.type;

// Currently only for player health bar, but can be extended to more types of
// overlays
public interface MapDecoration {
    boolean isVisible();

    int getBarPercentage();
}
