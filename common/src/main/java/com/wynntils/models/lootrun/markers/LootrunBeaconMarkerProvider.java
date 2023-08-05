/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.markers;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.features.combat.CustomLootrunBeaconsFeature;
import com.wynntils.models.beacons.type.BeaconColor;
import com.wynntils.models.lootrun.type.TaskPrediction;
import com.wynntils.models.marker.type.MarkerInfo;
import com.wynntils.models.marker.type.MarkerProvider;
import com.wynntils.models.marker.type.StaticLocationSupplier;
import com.wynntils.services.map.pois.MarkerPoi;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.type.PoiLocation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class LootrunBeaconMarkerProvider implements MarkerProvider<MarkerPoi> {
    private List<MarkerInfo> taskMarkers = new ArrayList<>();
    private List<MarkerPoi> pois = new ArrayList<>();

    public void reloadTaskMarkers() {
        taskMarkers.clear();

        for (Map.Entry<BeaconColor, TaskPrediction> entry :
                Models.Lootrun.getBeacons().entrySet()) {
            taskMarkers.add(new MarkerInfo(
                    EnumUtils.toNiceString(entry.getKey()) + " Beacon",
                    new StaticLocationSupplier(entry.getValue().taskLocation().location()),
                    entry.getValue().taskLocation().taskType().getTexture(),
                    entry.getKey().getColor(),
                    CommonColors.WHITE,
                    entry.getKey().getColor()));
        }

        pois.clear();
        for (MarkerInfo entry : taskMarkers) {
            pois.add(new MarkerPoi(PoiLocation.fromLocation(entry.location()), entry.name(), entry.texture()));
        }
    }

    @Override
    public Stream<MarkerInfo> getMarkerInfos() {
        return taskMarkers.stream();
    }

    @Override
    public Stream<MarkerPoi> getPois() {
        return pois.stream();
    }

    @Override
    public boolean isEnabled() {
        // FIXME: Feature-Model dependency
        return Models.Lootrun.getState().isRunning()
                && Managers.Feature.getFeatureInstance(CustomLootrunBeaconsFeature.class)
                        .isEnabled();
    }
}
