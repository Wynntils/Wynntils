/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.style;

import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;

public interface MapFeatureAttributes {
    String getLabel();

    MapIcon getIcon();

    // 0-1000, 1000 is highest priority (drawn on top of everything else)
    Integer getPriority();

    MapVisibility getLabelVisibility();

    CustomColor getLabelColor();

    TextShadow getLabelShadow();

    MapVisibility getIconVisibility();

    CustomColor getIconColor();

    MapIconDecoration getIconDecoration();
}
