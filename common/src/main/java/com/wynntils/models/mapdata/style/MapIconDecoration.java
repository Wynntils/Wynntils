/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.style;

// Currently only for player health bar, but can be extended to more types of
// overlays
public interface MapIconDecoration {
    boolean isVisible();

    int getBarPercentage();
}
