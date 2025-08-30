/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.type;

import com.wynntils.utils.mc.type.PreciseLocation;

public record Beacon<T extends BeaconKind>(PreciseLocation position, T beaconKind) {
    @Override
    public String toString() {
        return "Beacon[" + "position=" + position + ", " + "beaconKind=" + beaconKind + ']';
    }
}
