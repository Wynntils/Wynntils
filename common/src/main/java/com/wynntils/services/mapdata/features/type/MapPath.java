/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.features.type;

import com.wynntils.services.mapdata.attributes.type.MapPathAttributes;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.BoundingShape;
import java.util.List;

public interface MapPath extends MapFeature<MapPathAttributes> {
    // The path is described by a sequence of locations
    List<Location> getPath();

    @Override
    default boolean isVisible(BoundingShape boundingShape) {
        return false;
    }
}
