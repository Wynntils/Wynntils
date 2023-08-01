/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun;

import com.wynntils.core.components.Models;
import com.wynntils.models.compass.type.CompassInfo;
import com.wynntils.models.compass.type.CompassProvider;
import java.util.stream.Stream;

public class LootrunBeaconCompassProvider implements CompassProvider {
    @Override
    public Stream<CompassInfo> getCompassInfos() {
        // TODO: Implement this
        return Stream.of();
    }

    @Override
    public boolean isEnabled() {
        return Models.Lootrun.getState().isRunning();
    }
}
