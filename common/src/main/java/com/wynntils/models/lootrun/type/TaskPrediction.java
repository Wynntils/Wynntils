/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.type;

import com.wynntils.models.beacons.type.Beacon;
import com.wynntils.models.lootrun.beacons.LootrunBeaconMarkerKind;

public record TaskPrediction(
        Beacon beacon,
        LootrunBeaconMarkerKind lootrunMarker,
        int distance,
        TaskLocation taskLocation,
        double predictionScore) {}
