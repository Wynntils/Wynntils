/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.impl;

import com.wynntils.services.mapdata.attributes.type.MapAreaAttributes;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;
import java.util.List;

public final class MapAreaAttributesImpl extends MapAttributesImpl implements MapAreaAttributes {
    public MapAreaAttributesImpl(
            Integer priority,
            Integer level,
            String label,
            String description,
            MapVisibilityImpl labelVisibility,
            CustomColor labelColor,
            TextShadow labelShadow,
            List<CustomColor> fillColors,
            List<CustomColor> borderColors,
            Float borderWidth) {
        super(
                priority,
                level,
                label,
                description,
                labelVisibility,
                labelColor,
                labelShadow,
                null,
                null,
                null,
                null,
                null,
                fillColors,
                borderColors,
                borderWidth);
    }

    public MapAreaAttributesImpl(MapAttributesImpl attributes) {
        super(attributes);
    }
}
