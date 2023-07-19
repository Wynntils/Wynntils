/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.style;

import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;

public interface MapFeatureLabelStyle {
    MapVisibility getVisibility();

    CustomColor getColor();

    TextShadow getShadow();
}
