/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.features.type;

import com.wynntils.services.mapdata.attributes.type.MapLocationAttributes;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.BoundingShape;

public interface MapLocation extends MapFeature<MapLocationAttributes> {
    Location getLocation();

    @Override
    default boolean isVisible(BoundingShape boundingShape) {
        return boundingShape.contains(getLocation().x(), getLocation().z());
    }
}
