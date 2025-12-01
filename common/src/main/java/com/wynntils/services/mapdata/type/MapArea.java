/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.type;

import com.wynntils.utils.mc.type.Location;
import java.util.List;

public interface MapArea extends MapFeature {
    // The area is described by a polygon. This list is the sequence of
    // vertices of that polygon, ordered in a counterclockwise orientation.
    // The last segment of the polygon connects from the last vertex to the first.
    List<Location> getPolygonArea();
}
