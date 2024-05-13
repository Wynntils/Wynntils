/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.type;

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

    // the minimum combat level for which this feature is suitable for
    // 1 means suitable for all levels (players start at level 1)
    // 0 means inherit
    // -1 means no information is available, or the feature is suitable for all levels
    int getLevel();

    MapVisibility getLabelVisibility();

    CustomColor getLabelColor();

    TextShadow getLabelShadow();

    MapVisibility getIconVisibility();

    CustomColor getIconColor();

    MapDecoration getIconDecoration();
}
