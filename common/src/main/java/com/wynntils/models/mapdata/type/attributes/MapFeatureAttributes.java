/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.type.attributes;

import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;

public interface MapFeatureAttributes {
    String getLabel();

    String getIconId();

    // 0-1000, 1000 is highest priority (drawn on top of everything else)
    int getPriority();

    MapFeatureVisibility getLabelVisibility();

    CustomColor getLabelColor();

    TextShadow getLabelShadow();

    MapFeatureVisibility getIconVisibility();

    CustomColor getIconColor();

    MapFeatureDecoration getIconDecoration();
}
