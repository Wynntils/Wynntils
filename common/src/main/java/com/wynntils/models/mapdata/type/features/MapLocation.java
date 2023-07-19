/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.type.features;

import com.wynntils.utils.mc.type.Location;

public interface MapLocation extends MapFeature {
    Location getLocation();
}
