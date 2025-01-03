/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.features.type;

import com.wynntils.services.mapdata.attributes.type.MapAreaAttributes;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.BoundingPolygon;
import com.wynntils.utils.type.BoundingShape;
import java.util.List;

public interface MapArea extends MapFeature<MapAreaAttributes> {
    // The area is described by a polygon. This list is the sequence of
    // vertices of that convex polygon, ordered in a counterclockwise orientation.
    // The last segment of the polygon connects from the last vertice to the first.
    List<Location> getPolygonArea();

    // Ideally, this should be cached, constructed from the polygon area.
    BoundingPolygon getBoundingPolygon();

    @Override
    default boolean isVisible(BoundingShape boundingShape) {
        return boundingShape.intersects(getBoundingPolygon());
    }
}
