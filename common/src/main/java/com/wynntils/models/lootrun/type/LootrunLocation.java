/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.type;

import com.wynntils.utils.mc.type.Location;
import net.minecraft.core.Position;

public enum LootrunLocation {
    SILENT_EXPANSE(new Location(990, 77, -785), "\uDAFF\uDFF2\uE00A\uDAFF\uDF6F\uF00A"),
    CORKUS(new Location(-1560, 97, -2675), "\uDAFF\uDFF2\uE00A\uDAFF\uDF6F\uF007"),
    MOLTEN_HEIGHTS_HIKE(new Location(1270, 10, -5130), "\uDAFF\uDFF2\uE00A\uDAFF\uDF6F\uF008"),
    SKY_ISLANDS_EXPLORATION(new Location(1035, 135, -4420), "\uDAFF\uDFF2\uE00A\uDAFF\uDF6F\uF009"),
    CANYON_OF_THE_LOST_EXCURSION(new Location(580, 78, -5021), "\uDAFF\uDFF2\uE00A\uDAFF\uDF6F\uF006"),
    UNKNOWN(null, null);

    private final Location location;
    private final String containerTitle;

    LootrunLocation(Location location, String containerTitle) {
        this.location = location;
        this.containerTitle = containerTitle;
    }

    public Location getLocation() {
        return location;
    }

    public String getContainerTitle() {
        return containerTitle;
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

    public static LootrunLocation fromContainerTitle(String title) {
        for (LootrunLocation location : values()) {
            if (location.getContainerTitle() != null
                    && location.getContainerTitle().equals(title)) {
                return location;
            }
        }
        return UNKNOWN;
    }
}
