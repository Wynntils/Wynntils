/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.builtin;

import com.wynntils.core.components.Managers;
import com.wynntils.features.map.MainMapFeature;
import com.wynntils.models.containers.type.LootChestTier;
import com.wynntils.services.mapdata.attributes.AbstractMapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.services.mapdata.type.MapFeature;
import com.wynntils.services.mapdata.type.MapLocation;
import com.wynntils.utils.mc.type.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class LootChestsProvider extends BuiltInProvider {
    private static final List<MapFeature> PROVIDED_FEATURES = new ArrayList<>();
    private static int counter;

    public static void resetFeatures() {
        PROVIDED_FEATURES.clear();
    }

    @Override
    public String getProviderId() {
        return "loot-chests";
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return PROVIDED_FEATURES.stream();
    }

    public static void registerFeature(FoundChestLocation foundChestLocation) {
        PROVIDED_FEATURES.add(foundChestLocation);
    }

    public static final class FoundChestLocation implements MapLocation {
        private final Location location;
        private final LootChestTier tier;
        private final transient int number;

        public FoundChestLocation(Location location, LootChestTier tier) {
            this.location = location;
            this.tier = tier;
            this.number = LootChestsProvider.counter++;
        }

        @Override
        public String getFeatureId() {
            return "found-chest" + "-" + number;
        }

        @Override
        public String getCategoryId() {
            return "wynntils:personal:found-chest:tier-" + tier;
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<MapVisibility> getIconVisibility() {
                    return switch (tier) {
                        case TIER_1 -> Optional.of(MapVisibility.builder()
                                .withMin(Managers.Feature.getFeatureInstance(MainMapFeature.class)
                                        .lootChestTier1PoiMinZoom
                                        .get()));
                        case TIER_2 -> Optional.of(MapVisibility.builder()
                                .withMin(Managers.Feature.getFeatureInstance(MainMapFeature.class)
                                        .lootChestTier2PoiMinZoom
                                        .get()));
                        case TIER_3 -> Optional.of(MapVisibility.builder()
                                .withMin(Managers.Feature.getFeatureInstance(MainMapFeature.class)
                                        .lootChestTier3PoiMinZoom
                                        .get()));
                        case TIER_4 -> Optional.of(MapVisibility.builder()
                                .withMin(Managers.Feature.getFeatureInstance(MainMapFeature.class)
                                        .lootChestTier4PoiMinZoom
                                        .get()));
                    };
                }
            });
        }

        @Override
        public List<String> getTags() {
            return List.of();
        }

        @Override
        public Location getLocation() {
            return location;
        }
    }
}
