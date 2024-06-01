/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.builtin;

import com.wynntils.models.containers.type.LootChestTier;
import com.wynntils.services.mapdata.providers.json.JsonMapLocation;
import com.wynntils.services.mapdata.type.MapFeature;
import com.wynntils.utils.mc.type.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class LootChestsProvider extends BuiltInProvider {
    private static final List<MapFeature> PROVIDED_FEATURES = new ArrayList<>();

    @Override
    public String getProviderId() {
        return "loot-chests";
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return PROVIDED_FEATURES.stream();
    }

    public void updateFoundChests(List<FoundChestLocation> foundChests) {
        PROVIDED_FEATURES.forEach(this::notifyCallbacks);
        PROVIDED_FEATURES.clear();
        foundChests.forEach(LootChestsProvider::registerFeature);
    }

    public static void registerFeature(FoundChestLocation foundChest) {
        PROVIDED_FEATURES.add(foundChest);
    }

    public static final class FoundChestLocation extends JsonMapLocation {
        public FoundChestLocation(Location location, LootChestTier tier) {
            super(
                    "found-chest" + "-" + location.hashCode(),
                    "wynntils:personal:found-chest:tier-" + tier.getWaypointTier(),
                    null,
                    location);
        }
    }
}
