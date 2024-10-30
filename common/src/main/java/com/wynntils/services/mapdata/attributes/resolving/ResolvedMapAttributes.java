/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.resolving;

import com.wynntils.services.mapdata.attributes.type.MapDecoration;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;

public record ResolvedMapAttributes(
        int priority,
        int level,
        String label,
        ResolvedMapVisibility labelVisibility,
        CustomColor labelColor,
        TextShadow labelShadow,
        String iconId,
        ResolvedMapVisibility iconVisibility,
        CustomColor iconColor,
        MapDecoration iconDecoration,
        boolean hasMarker,
        ResolvedMarkerOptions markerOptions,
        CustomColor fillColor,
        CustomColor borderColor,
        float borderWidth) {}
