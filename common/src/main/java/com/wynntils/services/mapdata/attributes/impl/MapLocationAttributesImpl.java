/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.impl;

import com.wynntils.services.mapdata.attributes.type.MapLocationAttributes;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;
import java.util.List;

public final class MapLocationAttributesImpl extends MapAttributesImpl implements MapLocationAttributes {
    public MapLocationAttributesImpl(
            Integer priority,
            Integer level,
            String label,
            String description,
            MapVisibilityImpl labelVisibility,
            CustomColor labelColor,
            TextShadow labelShadow,
            String icon,
            MapVisibilityImpl iconVisibility,
            CustomColor iconColor,
            Boolean hasMarker,
            MapMarkerOptionsImpl markerOptions) {
        super(
                priority,
                level,
                label,
                description,
                labelVisibility,
                labelColor,
                labelShadow,
                icon,
                iconVisibility,
                iconColor,
                hasMarker,
                markerOptions,
                List.of(),
                List.of(),
                null);
    }

    public MapLocationAttributesImpl(MapAttributesImpl attributes) {
        super(attributes);
    }
}
