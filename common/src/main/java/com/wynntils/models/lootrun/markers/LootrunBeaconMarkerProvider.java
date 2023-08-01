/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.markers;

import com.wynntils.core.components.Models;
import com.wynntils.models.marker.type.MarkerInfo;
import com.wynntils.models.marker.type.MarkerProvider;
import java.util.stream.Stream;

public class LootrunBeaconMarkerProvider implements MarkerProvider {
    @Override
    public Stream<MarkerInfo> getMarkerInfos() {
        // TODO: Implement this
        return Stream.of();
    }

    @Override
    public boolean isEnabled() {
        return Models.Lootrun.getState().isRunning();
    }
}
