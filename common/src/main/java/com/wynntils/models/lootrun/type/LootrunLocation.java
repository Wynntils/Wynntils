/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.type;

import com.wynntils.utils.mc.type.Location;
import net.minecraft.core.Position;

public enum LootrunLocation {
    SILENT_EXPANSE(new Location(0, 0, 0)),
    CORKUS(new Location(0, 0, 0)),
    MOLTEN_HEIGHTS_HIKE(new Location(0, 0, 0)),
    SKY_ISLANDS_EXPLORATION(new Location(0, 0, 0)),
    CANYON_OF_THE_LOST_EXCURSION(new Location(0, 0, 0)),
    UNKNOWN(null);

    private final Location location;

    LootrunLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public static LootrunLocation getNearest(Location location) {
        if (location == null) return UNKNOWN;

        LootrunLocation nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (LootrunLocation lootrunLocation : values()) {
            if (lootrunLocation.getLocation() == null) continue;

            Position position = new Position() {
                @Override
                public double x() {
                    return location.x;
                }

                @Override
                public double y() {
                    return location.y;
                }

                @Override
                public double z() {
                    return location.z;
                }
            };

            double distance = lootrunLocation.getLocation().distanceToSqr(position);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = lootrunLocation;
            }
        }

        return nearest != null ? nearest : UNKNOWN;
    }
}
