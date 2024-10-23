/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.impl;

import com.wynntils.services.mapdata.attributes.type.MapLocationAttributes;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;

public final class MapLocationAttributesImpl extends MapAttributesImpl implements MapLocationAttributes {
    public MapLocationAttributesImpl(
            int priority,
            int level,
            String label,
            MapVisibilityImpl labelVisibility,
            CustomColor labelColor,
            TextShadow labelShadow,
            String icon,
            MapVisibilityImpl iconVisibility,
            CustomColor iconColor,
            MapMarkerOptionsImpl markerOptions) {
        super(
                priority,
                level,
                label,
                labelVisibility,
                labelColor,
                labelShadow,
                icon,
                iconVisibility,
                iconColor,
                markerOptions,
                null,
                null,
                0f);
    }
}
