/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.marker;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.features.map.WorldWaypointDistanceFeature;
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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

public class UserWaypointMarkerProvider implements MarkerProvider<WaypointPoi> {
    private final Set<Pair<MarkerInfo, WaypointPoi>> markerInfoSet = new CopyOnWriteArraySet<>();

    public void addLocation(
            Location location, Texture texture, CustomColor beaconColor, CustomColor textColor, String additionalText) {
        addLocation(new MarkerInfo(
                "Waypoint",
                new StaticLocationSupplier(location),
                texture,
                beaconColor,
                textColor,
                CommonColors.WHITE,
                // FIXME: Feature-Model dependency
                Managers.Feature.getFeatureInstance(WorldWaypointDistanceFeature.class)
                                .showAdditionalTextInWorld
                                .get()
                        ? additionalText
                        : null));
    }

    public void addLocation(Location location, Texture texture, CustomColor beaconColor, String additionalText) {
        addLocation(new MarkerInfo(
                "Waypoint",
                new StaticLocationSupplier(location),
                texture,
                beaconColor,
                CommonColors.WHITE,
                CommonColors.WHITE,
                // FIXME: Feature-Model dependency
                Managers.Feature.getFeatureInstance(WorldWaypointDistanceFeature.class)
                                .showAdditionalTextInWorld
                                .get()
                        ? Models.Activity.getTrackedName()
                        : null));
    }

    public void addLocation(Location location, Texture texture, String additionalText) {
        addLocation(new MarkerInfo(
                "Waypoint",
                new StaticLocationSupplier(location),
                texture,
                CustomColor.NONE,
                CommonColors.WHITE,
                CommonColors.WHITE,
                // FIXME: Feature-Model dependency
                Managers.Feature.getFeatureInstance(WorldWaypointDistanceFeature.class)
                                .showAdditionalTextInWorld
                                .get()
                        ? additionalText
                        : null));
    }

    public void addLocation(Location location, String additionalText) {
        addLocation(new MarkerInfo(
                "Waypoint",
                new StaticLocationSupplier(location),
                Texture.WAYPOINT,
                CustomColor.NONE,
                CommonColors.WHITE,
                CommonColors.WHITE,
                // FIXME: Feature-Model dependency
                Managers.Feature.getFeatureInstance(WorldWaypointDistanceFeature.class)
                                .showAdditionalTextInWorld
                                .get()
                        ? additionalText
                        : null));
    }

    public void addLocation(LocationSupplier locationSupplier, String additionalText) {
        addLocation(new MarkerInfo(
                "Waypoint",
                locationSupplier,
                Texture.WAYPOINT,
                CustomColor.NONE,
                CommonColors.WHITE,
                CommonColors.WHITE,
                // FIXME: Feature-Model dependency
                Managers.Feature.getFeatureInstance(WorldWaypointDistanceFeature.class)
                                .showAdditionalTextInWorld
                                .get()
                        ? additionalText
                        : null));
    }

    private void addLocation(MarkerInfo markerInfo) {
        markerInfoSet.add(Pair.of(
                markerInfo,
                new WaypointPoi(
                        () -> PoiLocation.fromLocation(
                                markerInfo.locationSupplier().getLocation()),
                        markerInfo.name())));
    }

    public void removeLocation(Location location) {
        markerInfoSet.removeIf(info -> info.a().location().equals(location));
    }

    public void removeAllLocations() {
        markerInfoSet.clear();
    }

    @Override
    public Stream<WaypointPoi> getPois() {
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
