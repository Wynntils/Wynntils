/*
 * Copyright © Wynntils 2024.
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
            MapVisibilityImpl labelVisibility,
            CustomColor labelColor,
            TextShadow labelShadow,
            CustomColor fillColor,
            CustomColor borderColor,
            float borderWidth) {
        super(
                priority,
                level,
                label,
                labelVisibility,
                labelColor,
                labelShadow,
                null,
                null,
                null,
                null,
                fillColor,
                borderColor,
                borderWidth);
    }
}
