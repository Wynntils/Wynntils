/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.marker;

import com.wynntils.models.marker.type.LocationSupplier;
import com.wynntils.models.marker.type.MarkerInfo;
import com.wynntils.models.marker.type.MarkerProvider;
import com.wynntils.models.marker.type.StaticLocationSupplier;
import com.wynntils.services.map.pois.WaypointPoi;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.mc.type.PoiLocation;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.type.Pair;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

public class UserWaypointMarkerProvider implements MarkerProvider {
    private final Set<Pair<MarkerInfo, WaypointPoi>> markerInfoSet = new LinkedHashSet<>();

    public void addLocation(Location location, Texture texture, CustomColor beaconColor, CustomColor textColor) {
        addLocation(new MarkerInfo(new StaticLocationSupplier(location), texture, beaconColor, textColor));
    }

    public void addLocation(Location location, Texture texture, CustomColor beaconColor) {
        addLocation(new MarkerInfo(new StaticLocationSupplier(location), texture, beaconColor, CommonColors.WHITE));
    }

    public void addLocation(Location location, Texture texture) {
        addLocation(
                new MarkerInfo(new StaticLocationSupplier(location), texture, CustomColor.NONE, CommonColors.WHITE));
    }

    public void addLocation(Location location) {
        addLocation(new MarkerInfo(
                new StaticLocationSupplier(location), Texture.WAYPOINT, CustomColor.NONE, CommonColors.WHITE));
    }

    public void addLocation(LocationSupplier locationSupplier) {
        addLocation(new MarkerInfo(locationSupplier, Texture.WAYPOINT, CustomColor.NONE, CommonColors.WHITE));
    }

    public void addLocation(MarkerInfo markerInfo) {
        markerInfoSet.add(Pair.of(
                markerInfo,
                new WaypointPoi(() ->
                        PoiLocation.fromLocation(markerInfo.locationSupplier().getLocation()))));
    }

    public void removeLocation(Location location) {
        markerInfoSet.removeIf(info -> info.a().location().equals(location));
    }

    public void removeAllLocations() {
        markerInfoSet.clear();
    }

    public Stream<WaypointPoi> getWaypointPois() {
        return markerInfoSet.stream().map(Pair::b);
    }

    @Override
    public Stream<MarkerInfo> getMarkerInfos() {
        return markerInfoSet.stream().map(Pair::a);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
