/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.builtin;

import com.wynntils.services.mapdata.features.builtin.WaypointLocation;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.type.MapIcon;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class WaypointsProvider extends BuiltInProvider {
    private static final List<MapFeature> PROVIDED_FEATURES = new ArrayList<>();
    private static final List<MapIcon> PROVIDED_ICONS = new ArrayList<>();

    @Override
    public String getProviderId() {
        return "waypoints";
    }

    @Override
    public Stream<MapIcon> getIcons() {
        return PROVIDED_ICONS.stream();
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return PROVIDED_FEATURES.stream();
    }

    @Override
    public void reloadData() {}

    public void updateWaypoints(List<WaypointLocation> waypoints) {
        PROVIDED_FEATURES.forEach(this::notifyCallbacks);
        PROVIDED_FEATURES.clear();
        waypoints.forEach(WaypointsProvider::registerFeature);
    }

    public void updateIcons(List<? extends MapIcon> icons) {
        PROVIDED_ICONS.forEach(this::notifyCallbacks);
        PROVIDED_ICONS.clear();
        icons.forEach(WaypointsProvider::registerIcon);
    }

    private static void registerFeature(WaypointLocation waypoint) {
        PROVIDED_FEATURES.add(waypoint);
    }

    private static void registerIcon(MapIcon icon) {
        PROVIDED_ICONS.add(icon);
    }
}
