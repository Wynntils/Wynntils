/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.seaskipper.type;

import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.BoundingPolygon;
import java.util.List;
import org.joml.Vector2f;

public record SeaskipperDestinationProfile(String destination, int combatLevel, List<Location> points) {
    public Vector2f getCenter() {
        return BoundingPolygon.fromLocations(points).centroid();
    }
}
