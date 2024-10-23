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
            int priority,
            int level,
            String label,
            JsonMapVisibility labelVisibility,
            CustomColor labelColor,
            TextShadow labelShadow) {
        super(priority, level, label, labelVisibility, labelColor, labelShadow, null, null, null, null);
    }
}
