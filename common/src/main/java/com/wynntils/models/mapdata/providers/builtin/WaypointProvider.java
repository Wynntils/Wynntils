/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers.builtin;

import com.wynntils.models.mapdata.providers.BuiltInProvider;
import com.wynntils.models.mapdata.style.MapFeatureAttributes;
import com.wynntils.models.mapdata.type.MapCategory;
import com.wynntils.models.mapdata.type.MapFeature;
import com.wynntils.models.mapdata.type.MapLocation;
import com.wynntils.utils.mc.type.Location;
import java.util.List;
import java.util.stream.Stream;

public class WaypointProvider extends BuiltInProvider {
    @Override
    public Stream<MapFeature> getFeatures() {
        return null;
    }

    private class WaypointLocation implements MapLocation {
        @Override
        public String getId() {
            return "wynntils:waypoint:marker";
        }

        @Override
        public MapCategory getCategory() {
            return null;
        }

        @Override
        public MapFeatureAttributes getAttributes() {
            return null;
        }

        @Override
        public List<String> getTags() {
            return null;
        }

        @Override
        public Location getLocation() {
            return null;
        }
    }
}
