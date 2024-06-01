/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.builtin;

import com.wynntils.services.mapdata.providers.json.JsonMapAttributes;
import com.wynntils.services.mapdata.providers.json.JsonMapLocation;
import com.wynntils.services.mapdata.type.MapFeature;
import com.wynntils.utils.mc.type.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class WaypointsProvider extends BuiltInProvider {
    private static final List<MapFeature> PROVIDED_FEATURES = new ArrayList<>();

    @Override
    public String getProviderId() {
        return "waypoints";
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return PROVIDED_FEATURES.stream();
    }

    public void updateWaypoints(List<WaypointLocation> waypoints) {
        PROVIDED_FEATURES.forEach(this::notifyCallbacks);
        PROVIDED_FEATURES.clear();
        waypoints.forEach(WaypointsProvider::registerFeature);
    }

    public static void registerFeature(WaypointLocation waypoint) {
        //        private static final MapVisibility WAYPOINT_VISIBILITY =
        //                MapVisibility.builder().withMin(30f);
        //        String iconId = MapIconsProvider.getIconIdFromTexture(customPoi.getIcon());
        //        PROVIDED_FEATURES.add(new WaypointLocation(
        //                customPoi.getLocation().asLocation(),
        //                customPoi.getName(),
        //                "",
        //                iconId,
        //                customPoi.getColor(),
        //                new JsonMapVisibility(
        //                        switch (customPoi.getVisibility()) {
        //                            case DEFAULT -> WAYPOINT_VISIBILITY;
        //                            case ALWAYS -> FixedMapVisibility.ICON_ALWAYS;
        //                            case HIDDEN -> FixedMapVisibility.ICON_NEVER;
        //                        })));

        PROVIDED_FEATURES.add(waypoint);
    }

    public static final class WaypointLocation extends JsonMapLocation {
        public WaypointLocation(Location location, String label, String subcategory, JsonMapAttributes attributes) {
            super(
                    "waypoint" + "-" + label.toLowerCase(Locale.ROOT).replaceAll("\\s", "_") + "-"
                            + location.hashCode(),
                    "wynntils:personal:waypoint" + (subcategory.isEmpty() ? "" : ":" + subcategory),
                    attributes,
                    location);
        }
    }
}
