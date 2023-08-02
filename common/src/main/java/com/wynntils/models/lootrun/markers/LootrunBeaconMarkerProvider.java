/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.markers;

import com.wynntils.core.components.Models;
import com.wynntils.models.beacons.type.BeaconColor;
import com.wynntils.models.lootrun.type.TaskLocation;
import com.wynntils.models.marker.type.MarkerInfo;
import com.wynntils.models.marker.type.MarkerProvider;
import com.wynntils.models.marker.type.StaticLocationSupplier;
import com.wynntils.utils.colors.CommonColors;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class LootrunBeaconMarkerProvider implements MarkerProvider {
    private List<MarkerInfo> taskMarkers = new ArrayList<>();

    public void reloadTaskMarkers() {
        taskMarkers.clear();

        for (Map.Entry<BeaconColor, TaskLocation> entry :
                Models.Lootrun.getBeacons().entrySet()) {
            taskMarkers.add(new MarkerInfo(
                    new StaticLocationSupplier(entry.getValue().location()),
                    entry.getValue().taskType().getTexture(),
                    entry.getKey().getColor(),
                    CommonColors.WHITE));
        }
    }

    @Override
    public Stream<MarkerInfo> getMarkerInfos() {
        return taskMarkers.stream();
    }

    @Override
    public boolean isEnabled() {
        return Models.Lootrun.getState().isRunning(); // fixme disable this via config
    }
}
