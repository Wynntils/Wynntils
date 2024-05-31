/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.builtin;

import com.wynntils.services.map.pois.CustomPoi;
import com.wynntils.services.mapdata.attributes.FixedMapVisibility;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.services.mapdata.providers.json.JsonMapAttributesBuilder;
import com.wynntils.services.mapdata.providers.json.JsonMapLocation;
import com.wynntils.services.mapdata.providers.json.JsonMapVisibility;
import com.wynntils.services.mapdata.type.MapFeature;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class WaypointsProvider extends BuiltInProvider {
    private static final MapVisibility WAYPOINT_VISIBILITY =
            MapVisibility.builder().withMin(30f);

    private static final List<MapFeature> PROVIDED_FEATURES = new ArrayList<>();

    @Override
    public String getProviderId() {
        return "waypoints";
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return PROVIDED_FEATURES.stream();
    }

    public void updateWaypoints(List<CustomPoi> waypoints) {
        // FIXME: Add PROVIDED_FEATURES.forEach(feature -> notifyCallbacks(feature));
        PROVIDED_FEATURES.clear();
        waypoints.forEach(WaypointsProvider::registerFeature);
    }

    public static void registerFeature(CustomPoi customPoi) {
        String iconId = MapIconsProvider.getIconIdFromTexture(customPoi.getIcon());
        PROVIDED_FEATURES.add(new WaypointLocation(
                customPoi.getLocation().asLocation(),
                customPoi.getName(),
                iconId,
                customPoi.getColor(),
                new JsonMapVisibility(
                        switch (customPoi.getVisibility()) {
                            case DEFAULT -> WAYPOINT_VISIBILITY;
                            case ALWAYS -> FixedMapVisibility.ICON_ALWAYS;
                            case HIDDEN -> FixedMapVisibility.ICON_NEVER;
                        })));
    }

    public static final class WaypointLocation extends JsonMapLocation {
        private WaypointLocation(
                Location location, String name, String iconId, CustomColor iconColor, JsonMapVisibility visibility) {
            super(
                    "waypoint" + "-" + name.toLowerCase(Locale.ROOT).replaceAll("\\s", "_"),
                    "wynntils:personal:waypoint",
                    new JsonMapAttributesBuilder()
                            .setLabel(name)
                            .setIcon(iconId)
                            .setIconColor(iconColor)
                            .setIconVisibility(visibility)
                            .build(),
                    location);
        }
    }
}
