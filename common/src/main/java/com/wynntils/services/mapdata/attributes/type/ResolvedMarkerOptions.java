/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.type;

import com.wynntils.utils.colors.CustomColor;

public record ResolvedMarkerOptions(
        float minDistance,
        float maxDistance,
        float fade,
        CustomColor beaconColor,
        boolean hasLabel,
        boolean hasDistance,
        boolean hasIcon) {}
