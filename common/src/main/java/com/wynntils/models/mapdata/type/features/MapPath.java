/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.type.features;

import com.wynntils.utils.mc.type.Location;
import java.util.List;

public interface MapPath extends MapFeature {
    // The path is described by a sequence of locations
    List<Location> getPath();
}
