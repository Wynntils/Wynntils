/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.type;

import com.wynntils.models.beacons.type.BeaconMarker;
import com.wynntils.models.lootrun.beacons.LootrunBeaconKind;
import com.wynntils.models.lootrun.beacons.LootrunBeaconMarkerKind;

public record TaskPrediction(
        BeaconMarker beaconMarker,
        LootrunBeaconKind lootrunBeaconKind,
        LootrunBeaconMarkerKind lootrunMarkerKind,
        int distance,
        TaskLocation taskLocation,
        double predictionScore) {}
