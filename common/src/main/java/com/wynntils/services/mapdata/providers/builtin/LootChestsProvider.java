/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.builtin;

import com.wynntils.core.components.Managers;
import com.wynntils.features.map.MainMapFeature;
import com.wynntils.models.containers.type.LootChestTier;
import com.wynntils.services.mapdata.providers.json.JsonMapLocation;
import com.wynntils.services.mapdata.type.MapFeature;
import com.wynntils.utils.mc.type.Location;
import java.util.stream.Stream;

public class LootChestsProvider extends BuiltInProvider {
    @Override
    public String getProviderId() {
        return "loot-chests";
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return Managers.Feature.getFeatureInstance(MainMapFeature.class).foundChestLocations.get().stream()
                .map(location -> location);
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
