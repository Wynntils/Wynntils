/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.marker;

import com.wynntils.core.components.Model;
import com.wynntils.models.marker.type.MarkerInfo;
import com.wynntils.models.marker.type.MarkerProvider;
import com.wynntils.services.map.pois.Poi;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class MarkerModel extends Model {
    public static final UserWaypointMarkerProvider USER_WAYPOINTS_PROVIDER = new UserWaypointMarkerProvider();

    private final List<MarkerProvider> markerProviders = new ArrayList<>();

    public MarkerModel() {
        super(List.of());

        registerMarkerProvider(USER_WAYPOINTS_PROVIDER);
    }

    public void registerMarkerProvider(MarkerProvider provider) {
        markerProviders.add(provider);
    }

    public Stream<MarkerInfo> getAllMarkers() {
        return markerProviders.stream().filter(MarkerProvider::isEnabled).flatMap(MarkerProvider::getMarkerInfos);
    }

    public Stream<Poi> getAllPois() {
        return markerProviders.stream().filter(MarkerProvider::isEnabled).flatMap(MarkerProvider::getPois);
    }
}
