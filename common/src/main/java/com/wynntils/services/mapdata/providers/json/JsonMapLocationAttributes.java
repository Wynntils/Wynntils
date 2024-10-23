/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.json;

import com.wynntils.services.mapdata.attributes.type.MapLocationAttributes;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;

public final class JsonMapLocationAttributes extends JsonMapAttributes implements MapLocationAttributes {
    public JsonMapLocationAttributes(
            int priority,
            int level,
            String label,
            JsonMapVisibility labelVisibility,
            CustomColor labelColor,
            TextShadow labelShadow,
            String icon,
            JsonMapVisibility iconVisibility,
            CustomColor iconColor,
            JsonMarkerOptions markerOptions) {
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
                markerOptions);
    }
}
