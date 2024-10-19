/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.json;

import com.wynntils.services.mapdata.attributes.type.MapAreaAttributes;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;

public final class JsonMapAreaAttributes extends JsonMapAttributes implements MapAreaAttributes {
    public JsonMapAreaAttributes(
            String label,
            String icon,
            int priority,
            int level,
            CustomColor labelColor,
            TextShadow labelShadow,
            JsonMapVisibility labelVisibility,
            CustomColor iconColor,
            JsonMapVisibility iconVisibility) {
        super(label, icon, priority, level, labelColor, labelShadow, labelVisibility, iconColor, iconVisibility);
    }
}
