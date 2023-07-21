/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.attributes.type;

import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;

public interface MapAttributes {
    // If this is the empty string (""), then no label will be displayed
    // null means inherit
    String getLabel();

    // If this is MapFeatureIcon.NO_ICON_ID ("none"), then no icon will be displayed
    // null means inherit
    String getIconId();

    // 1-1000, 1000 is highest priority (drawn on top of everything else)
    // 0 means no value specified; inherit
    int getPriority();

    MapVisibility getLabelVisibility();

    CustomColor getLabelColor();

    TextShadow getLabelShadow();

    MapVisibility getIconVisibility();

    CustomColor getIconColor();

    MapDecoration getIconDecoration();
}
