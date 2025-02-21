/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.impl;

import com.wynntils.services.mapdata.attributes.type.MapAreaAttributes;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;

public final class MapAreaAttributesImpl extends MapAttributesImpl implements MapAreaAttributes {
    public MapAreaAttributesImpl(
            int priority,
            int level,
            String label,
            String description,
            MapVisibilityImpl labelVisibility,
            CustomColor labelColor,
            TextShadow labelShadow,
            CustomColor fillColor,
            CustomColor borderColor,
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
                fillColor,
                borderColor,
                borderWidth);
    }

    public MapAreaAttributesImpl(MapAttributesImpl attributes) {
        super(attributes);
    }
}
