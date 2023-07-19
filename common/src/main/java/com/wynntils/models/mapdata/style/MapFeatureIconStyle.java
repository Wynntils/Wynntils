/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.style;

import com.wynntils.utils.colors.CustomColor;
import java.util.Optional;

public interface MapFeatureIconStyle {
    MapVisibility getVisibility();

    CustomColor getColor();

    Optional<MapIconDecoration> getIconDecoration();
}
