/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.features.builtin;

import com.wynntils.models.containers.type.LootChestTier;
import com.wynntils.services.mapdata.features.impl.MapLocationImpl;
import com.wynntils.utils.mc.type.Location;

public final class FoundChestLocation extends MapLocationImpl {
    public FoundChestLocation(Location location, LootChestTier tier) {
        super(
                "found-chest" + "-" + location.hashCode(),
                "wynntils:personal:found-chest:tier-" + tier.getWaypointTier(),
                null,
                location);
    }
}
