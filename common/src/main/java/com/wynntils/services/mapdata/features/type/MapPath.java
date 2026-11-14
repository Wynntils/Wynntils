/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.features.type;

import com.wynntils.services.mapdata.attributes.type.MapPathAttributes;
import com.wynntils.services.mapdata.paths.PathNote;
import com.wynntils.utils.type.BoundingShape;
import com.wynntils.utils.type.PathGeometry;
import java.util.List;
import net.minecraft.world.phys.Vec3;

public interface MapPath extends MapFeature<MapPathAttributes> {
    // The path is described by a sequence of locations
    List<Vec3> getPath();

    default List<Vec3> getMapPath() {
        return getPath();
    }

    default List<Vec3> getHighlights() {
        return List.of();
    }

    default List<PathNote> getNotes() {
        return List.of();
    }

    @Override
    default boolean isVisible(BoundingShape boundingShape) {
        List<Vec3> vertices = getMapPath();
        for (int i = 1; i < vertices.size(); i++) {
            Vec3 a = vertices.get(i - 1);
            Vec3 b = vertices.get(i);
            if (PathGeometry.intersectsSegment(
                    boundingShape, (float) a.x(), (float) a.z(), (float) b.x(), (float) b.z())) return true;
        }

        return false;
    }
}
