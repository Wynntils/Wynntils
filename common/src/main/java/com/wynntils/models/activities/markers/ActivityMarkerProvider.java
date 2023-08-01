/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.markers;

import com.wynntils.core.components.Managers;
import com.wynntils.features.combat.ContentTrackerFeature;
import com.wynntils.models.beacons.type.BeaconColor;
import com.wynntils.models.marker.type.MarkerInfo;
import com.wynntils.models.marker.type.MarkerProvider;
import com.wynntils.models.marker.type.StaticLocationSupplier;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.type.Pair;
import java.util.stream.Stream;

public class ActivityMarkerProvider implements MarkerProvider {
    private Pair<Location, MarkerInfo> spawnInfo;
    private Pair<Location, MarkerInfo> trackedActivityInfo;

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnInfo = spawnLocation == null
                ? null
                : Pair.of(
                        spawnLocation,
                        new MarkerInfo(
                                new StaticLocationSupplier(spawnLocation),
                                Texture.QUESTS_BUTTON,
                                CommonColors.WHITE,
                                CommonColors.WHITE));
    }

    public Location getSpawnLocation() {
        return spawnInfo.a();
    }

    public void setTrackedActivityLocation(Location trackedActivityLocation, BeaconColor beaconColor) {
        this.trackedActivityInfo = trackedActivityLocation == null
                ? null
                : Pair.of(
                        trackedActivityLocation,
                        new MarkerInfo(
                                new StaticLocationSupplier(trackedActivityLocation),
                                Texture.QUESTS_BUTTON,
                                beaconColor.getColor(),
                                CommonColors.WHITE));
    }

    public Location getTrackedActivityLocation() {
        return trackedActivityInfo.a();
    }

    @Override
    public Stream<MarkerInfo> getMarkerInfos() {
        Stream<MarkerInfo> stream = Stream.empty();

        if (spawnInfo != null) {
            stream = Stream.concat(stream, Stream.of(spawnInfo.b()));
        }
        if (trackedActivityInfo != null) {
            stream = Stream.concat(stream, Stream.of(trackedActivityInfo.b()));
        }

        return stream;
    }

    @Override
    public boolean isEnabled() {
        // FIXME: Move the config after config refactor
        return Managers.Feature.getFeatureInstance(ContentTrackerFeature.class)
                        .autoTrackCoordinates
                        .get()
                && (spawnInfo != null || trackedActivityInfo != null);
    }
}
