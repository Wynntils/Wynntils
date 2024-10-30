/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.impl;

import com.wynntils.services.mapdata.attributes.type.MapPathAttributes;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;

public final class MapPathAttributesImpl extends MapAttributesImpl implements MapPathAttributes {
    public MapPathAttributesImpl(
            int priority,
            int level,
            String label,
            MapVisibilityImpl labelVisibility,
            CustomColor labelColor,
            TextShadow labelShadow) {
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
                false,
                null,
                null,
                null,
                0f);
    }
}
