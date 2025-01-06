/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.seaskipper.type;

public record SeaskipperDestinationProfile(
        String destination, int combatLevel, int startX, int startZ, int endX, int endZ) {
    public int getX() {
        return (startX + endX) / 2;
    }

    public int getZ() {
        return (startZ + endZ) / 2;
    }
}
