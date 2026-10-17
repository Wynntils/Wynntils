/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.type;

import com.wynntils.core.WynntilsMod;
import net.minecraft.core.BlockPos;

public enum LootrunLocation {
    SILENT_EXPANSE(BlockPos.containing(994.0, 77.0, -786.0)),
    CORKUS(BlockPos.containing(-1556.0, 96.0, -2674.0)),
    MOLTEN_HEIGHTS_HIKE(BlockPos.containing(1272.0, 10.0, -5131.0)),
    SKY_ISLANDS_EXPLORATION(BlockPos.containing(1038.0, 135.0, -4416.0)),
    CANYON_OF_THE_LOST_EXCURSION(BlockPos.containing(580.0, 78.0, -5022.0)),
    THE_FRUMA_FORAY_WEST(BlockPos.containing(-2025.0, 4.0, -780.0)),
    THE_FRUMA_FORAY_EAST(BlockPos.containing(-1287.0, 76.0, -1154.0)),
    UNKNOWN(null);

    private static final int START_LOCATION_RADIUS = 20;

    private BlockPos blockPos;

    LootrunLocation(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public static LootrunLocation fromBlockPos(BlockPos blockPos) {
        for (LootrunLocation lootrunLocation : values()) {
            if (lootrunLocation.getBlockPos() == null) continue;

            if (blockPos.distSqr(lootrunLocation.getBlockPos()) <= Math.pow(START_LOCATION_RADIUS, 2)) {
                return lootrunLocation;
            }
        }

        WynntilsMod.warn("Unknown lootrun location at " + blockPos);
        return UNKNOWN;
    }
}
