/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.builtin;

import com.wynntils.models.containers.type.LootChestTier;
import com.wynntils.services.map.Label;
import com.wynntils.services.map.type.CombatKind;
import com.wynntils.services.map.type.ServiceKind;
import com.wynntils.services.mapdata.attributes.AbstractMapAttributes;
import com.wynntils.services.mapdata.attributes.FixedMapVisibility;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapIcon;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.services.mapdata.type.MapCategory;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class CategoriesProvider extends BuiltInProvider {
    private static final List<MapCategory> PROVIDED_CATEGORIES = new ArrayList<>();

    public CategoriesProvider() {
        for (ServiceKind kind : ServiceKind.values()) {
            PROVIDED_CATEGORIES.add(new ServiceCategory(kind));
        }
        for (CombatKind kind : CombatKind.values()) {
            PROVIDED_CATEGORIES.add(new CombatCategory(kind));
        }
        for (Label.LabelLayer layer : Label.LabelLayer.values()) {
            PROVIDED_CATEGORIES.add(new PlaceCategory(layer));
        }
        for (int tier = 1; tier <= LootChestTier.values().length; tier++) {
            PROVIDED_CATEGORIES.add(new FoundChestCategory(tier));
        }
        PROVIDED_CATEGORIES.add(new WaypointCategory());
        PROVIDED_CATEGORIES.add(new WynntilsCategory());
    }

    @Override
    public String getProviderId() {
        return "categories";
    }

    @Override
    public Stream<MapCategory> getCategories() {
        return PROVIDED_CATEGORIES.stream();
    }

    private static final class WynntilsCategory implements MapCategory {
        @Override
        public String getCategoryId() {
            return "wynntils";
        }

        @Override
        public Optional<String> getName() {
            return Optional.of("All Wynntils Map Features");
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<String> getIconId() {
                    return Optional.of(MapIconsProvider.FALLBACK_ICON_ID);
                }
            });
        }
    }

    private static final class WaypointCategory implements MapCategory {
        @Override
        public String getCategoryId() {
            return "wynntils:personal:waypoint";
        }

        @Override
        public Optional<String> getName() {
            return Optional.of("Personal Waypoints");
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<Integer> getPriority() {
                    return Optional.of(1000);
                }

                @Override
                public Optional<CustomColor> getLabelColor() {
                    return Optional.of(CommonColors.GREEN);
                }

                @Override
                public Optional<MapVisibility> getLabelVisibility() {
                    return Optional.of(FixedMapVisibility.LABEL_NEVER);
                }
            });
        }
    }

    private static final class FoundChestCategory implements MapCategory {
        private static final MapVisibility TIER_1_VISIBILITY =
                MapVisibility.builder().withMin(57f);
        private static final MapVisibility TIER_2_VISIBILITY =
                MapVisibility.builder().withMin(57f);
        private static final MapVisibility TIER_3_VISIBILITY =
                MapVisibility.builder().withMin(30f);
        private static final MapVisibility TIER_4_VISIBILITY =
                MapVisibility.builder().withMin(30f);

        private final int tier;

        private FoundChestCategory(int tier) {
            this.tier = tier;
        }

        @Override
        public String getCategoryId() {
            return "wynntils:personal:found-chest:tier-" + tier;
        }

        @Override
        public Optional<String> getName() {
            return Optional.of("Found Loot Chests");
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<String> getIconId() {
                    return Optional.of("wynntils:icon:lootchest:tier-" + tier);
                }

                @Override
                public Optional<String> getLabel() {
                    return Optional.of("Loot Chest Tier " + MathUtils.toRoman(tier));
                }

                @Override
                public Optional<Integer> getPriority() {
                    return Optional.of(500);
                }

                @Override
                public Optional<CustomColor> getLabelColor() {
                    return Optional.of(CommonColors.GREEN);
                }

                @Override
                public Optional<MapVisibility> getIconVisibility() {
                    return Optional.of(
                            switch (tier) {
                                case 1 -> TIER_1_VISIBILITY;
                                case 2 -> TIER_2_VISIBILITY;
                                case 3 -> TIER_3_VISIBILITY;
                                case 4 -> TIER_4_VISIBILITY;
                                    // This should never happen
                                default -> FixedMapVisibility.ICON_ALWAYS;
                            });
                }

                @Override
                public Optional<MapVisibility> getLabelVisibility() {
                    return Optional.of(FixedMapVisibility.LABEL_NEVER);
                }
            });
        }
    }

    private static final class ServiceCategory implements MapCategory {
        private static final MapVisibility FAST_TRAVEL_VISIBILITY =
                MapVisibility.builder().withMin(18f);
        private static final MapVisibility OTHER_VISIBILITY =
                MapVisibility.builder().withMin(57f);

        private final ServiceKind kind;

        private ServiceCategory(ServiceKind kind) {
            this.kind = kind;
        }

        @Override
        public String getCategoryId() {
            return "wynntils:service:" + kind.getMapDataId();
        }

        @Override
        public Optional<String> getName() {
            return Optional.of(kind.getName());
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<String> getLabel() {
                    return Optional.of(kind.getName());
                }

                @Override
                public Optional<String> getIconId() {
                    return Optional.of("wynntils:icon:service:" + kind.getMapDataId());
                }

                @Override
                public Optional<Integer> getPriority() {
                    return Optional.of(100);
                }

                @Override
                public Optional<CustomColor> getLabelColor() {
                    return Optional.of(CommonColors.GREEN);
                }

                @Override
                public Optional<MapVisibility> getIconVisibility() {
                    if (kind == ServiceKind.FAST_TRAVEL) {
                        return Optional.of(FAST_TRAVEL_VISIBILITY);
                    } else {
                        return Optional.of(OTHER_VISIBILITY);
                    }
                }

                @Override
                public Optional<MapVisibility> getLabelVisibility() {
                    return Optional.of(FixedMapVisibility.LABEL_NEVER);
                }
            });
        }
    }

    private static final class CombatCategory implements MapCategory {
        private static final MapVisibility CAVES_VISIBILITY =
                MapVisibility.builder().withMin(31f);
        private static final MapVisibility OTHER_VISIBILITY =
                MapVisibility.builder().withMin(19f);

        private final CombatKind kind;

        private CombatCategory(CombatKind kind) {
            this.kind = kind;
        }

        @Override
        public String getCategoryId() {
            return "wynntils:content:" + kind.getMapDataId();
        }

        @Override
        public Optional<String> getName() {
            return Optional.of(kind.getName());
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<String> getLabel() {
                    return Optional.of(kind.getName());
                }

                @Override
                public Optional<String> getIconId() {
                    return Optional.of("wynntils:icon:content:" + kind.getMapDataId());
                }

                @Override
                public Optional<Integer> getPriority() {
                    return Optional.of(100);
                }

                @Override
                public Optional<CustomColor> getLabelColor() {
                    return Optional.of(CommonColors.GREEN);
                }

                @Override
                public Optional<MapVisibility> getIconVisibility() {
                    if (kind == CombatKind.CAVES) {
                        return Optional.of(CAVES_VISIBILITY);
                    } else {
                        return Optional.of(OTHER_VISIBILITY);
                    }
                }

                @Override
                public Optional<MapVisibility> getLabelVisibility() {
                    return Optional.of(FixedMapVisibility.LABEL_NEVER);
                }
            });
        }
    }

    private static final class PlaceCategory implements MapCategory {
        private static final MapVisibility PROVINCE_VISIBILITY =
                MapVisibility.builder().withMax(32f);
        private static final MapVisibility CITY_VISIBILITY =
                MapVisibility.builder().withMax(74f);
        private static final MapVisibility PLACE_VISIBILITY =
                MapVisibility.builder().withMin(32f).withMax(86f);

        private final Label.LabelLayer layer;

        private PlaceCategory(Label.LabelLayer layer) {
            this.layer = layer;
        }

        @Override
        public String getCategoryId() {
            return "wynntils:place:" + layer.getMapDataId();
        }

        @Override
        public Optional<String> getName() {
            return Optional.of(layer.getName());
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<String> getIconId() {
                    return Optional.of(MapIcon.NO_ICON_ID);
                }

                @Override
                public Optional<CustomColor> getLabelColor() {
                    return Optional.of(
                            switch (layer) {
                                case PROVINCE -> CommonColors.DARK_AQUA;
                                case CITY -> CommonColors.YELLOW;
                                case TOWN_OR_PLACE -> CommonColors.WHITE;
                            });
                }

                @Override
                public Optional<Integer> getPriority() {
                    return Optional.of(700);
                }

                @Override
                public Optional<MapVisibility> getLabelVisibility() {
                    return Optional.of(
                            switch (layer) {
                                case PROVINCE -> PROVINCE_VISIBILITY;
                                case CITY -> CITY_VISIBILITY;
                                case TOWN_OR_PLACE -> PLACE_VISIBILITY;
                            });
                }
            });
        }
    }
}
