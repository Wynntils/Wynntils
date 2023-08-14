/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.type;

import com.wynntils.utils.type.BoundingBox;
import net.minecraft.core.Position;

public enum LootrunLocation {
    SILENT_EXPANSE(400, -1150, 1600, -180),
    CORKUS(-2150, -3500, -1200, -2100);

    private final BoundingBox boundingBox;

    LootrunLocation(int startX, int startZ, int endX, int endZ) {
        this.boundingBox = new BoundingBox(startX, startZ, endX, endZ);
    }

    public static LootrunLocation fromCoordinates(Position position) {
        for (LootrunLocation location : LootrunLocation.values()) {
            if (location.boundingBox.contains((float) position.x(), (float) position.z())) {
                return location;
            }
        }
        return null;
    }
}
