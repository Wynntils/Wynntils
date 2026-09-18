/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.type;

import com.wynntils.utils.EnumUtils;

public enum OverrideType {
    MAP_LOCATION_OVERRIDE,
    MAP_PATH_OVERRIDE,
    MAP_AREA_OVERRIDE;

    public String getDisplayName() {
        return EnumUtils.toNiceString(name());
    }
}
