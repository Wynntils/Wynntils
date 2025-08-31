/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.builtin;

import com.wynntils.services.map.pois.CustomPoi;
import com.wynntils.services.mapdata.attributes.AbstractMapAttributes;
import com.wynntils.services.mapdata.attributes.FixedMapVisibility;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.services.mapdata.type.MapFeature;
import com.wynntils.services.mapdata.type.MapLocation;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class WaypointsProvider extends BuiltInProvider {
    private static final List<MapFeature> PROVIDED_FEATURES = new ArrayList<>();
    private static int counter = 0;

    @Override
    public String getProviderId() {
        return "waypoints";
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return PROVIDED_FEATURES.stream();
    }

    public static void resetFeatures() {
        PROVIDED_FEATURES.clear();
    }

    public static void registerFeature(CustomPoi customPoi) {
        int tier =
                switch (customPoi.getIcon()) {
                    case CHEST_T1 -> 1;
                    case CHEST_T2 -> 2;
                    case CHEST_T3 -> 3;
                    case CHEST_T4 -> 4;
                    default -> 0;
                };
        if (tier == 0) {
            String iconId = MapIconsProvider.getIconIdFromTexture(customPoi.getIcon());
            PROVIDED_FEATURES.add(new WaypointLocation(
                    customPoi.getLocation().asLocation(),
                    customPoi.getName(),
                    iconId,
                    customPoi.getColor(),
                    customPoi.getVisibility()));
        } else {
            PROVIDED_FEATURES.add(new FoundChestLocation(customPoi.getLocation().asLocation(), tier));
        }
    }

    private static final class WaypointLocation implements MapLocation {
        protected static final MapVisibility WAYPOINT_VISIBILITY =
                MapVisibility.builder().withMin(30f);
        private final Location location;
        private final String name;
        private final String iconId;
        private final CustomColor color;
        private final CustomPoi.Visibility visibility;
        private final int number;

        private WaypointLocation(
                Location location, String name, String iconId, CustomColor color, CustomPoi.Visibility visibility) {
            this.location = location;
            this.name = name;
            this.iconId = iconId;
            this.color = color;
            this.visibility = visibility;
            this.number = WaypointsProvider.counter++;
        }

        @Override
        public String getFeatureId() {
            return "waypoint" + "-" + number;
        }

        @Override
        public String getCategoryId() {
            return "wynntils:personal:waypoint";
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<String> getIconId() {
                    return Optional.ofNullable(iconId);
                }

                @Override
                public Optional<String> getLabel() {
                    return Optional.ofNullable(name);
                }

                @Override
                public Optional<CustomColor> getIconColor() {
                    return Optional.of(color);
                }

                @Override
                public Optional<MapVisibility> getIconVisibility() {
                    return Optional.of(
                            switch (visibility) {
                                case DEFAULT -> WAYPOINT_VISIBILITY;
                                case ALWAYS -> FixedMapVisibility.ICON_ALWAYS;
                                case HIDDEN -> FixedMapVisibility.ICON_NEVER;
                            });
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

    private static final class FoundChestLocation implements MapLocation {
        private final Location location;
        private final int tier;
        private final int number;

        private FoundChestLocation(Location location, int tier) {
            this.location = location;
            this.tier = tier;
            this.number = WaypointsProvider.counter++;
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
            return Optional.empty();
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
