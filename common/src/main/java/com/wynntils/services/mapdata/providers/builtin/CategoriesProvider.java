/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.builtin;

import com.wynntils.models.containers.type.LootChestType;
import com.wynntils.services.map.Label;
import com.wynntils.services.map.type.CombatKind;
import com.wynntils.services.map.type.ServiceKind;
import com.wynntils.services.mapdata.attributes.AbstractMapAttributes;
import com.wynntils.services.mapdata.attributes.type.FullMapVisibility;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapIcon;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.services.mapdata.type.MapCategory;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import java.util.ArrayList;
import java.util.List;
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
        for (int tier = 1; tier <= LootChestType.values().length; tier++) {
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
        private static final MapVisibility DEFAULT_VISIBILITY = MapVisibility.DEFAULT_VISIBILITY;

        @Override
        public String getCategoryId() {
            return "wynntils";
        }

        @Override
        public String getName() {
            return "All Wynntils Map Features";
        }

        @Override
        public MapAttributes getAttributes() {
            return new AbstractMapAttributes() {
                @Override
                public String getIconId() {
                    return "wynntils:icon:symbols:waypoint";
                }

                @Override
                public int getPriority() {
                    return 500;
                }

                @Override
                public MapVisibility getIconVisibility() {
                    return DEFAULT_VISIBILITY;
                }

                @Override
                public MapVisibility getLabelVisibility() {
                    return DEFAULT_VISIBILITY;
                }
            };
        }
    }

    private static final class WaypointCategory implements MapCategory {
        @Override
        public String getCategoryId() {
            return "wynntils:personal:waypoint";
        }

        @Override
        public String getName() {
            return "Personal Waypoints";
        }

        @Override
        public MapAttributes getAttributes() {
            return new AbstractMapAttributes() {
                @Override
                public int getPriority() {
                    return 1000;
                }

                @Override
                public CustomColor getLabelColor() {
                    return CommonColors.GREEN;
                }

                @Override
                public MapVisibility getLabelVisibility() {
                    return MapVisibility.NEVER;
                }
            };
        }
    }

    private static final class FoundChestCategory implements MapCategory {
        private static final MapVisibility TIER_1_VISIBILITY = new FullMapVisibility(57, 100, 6);
        private static final MapVisibility TIER_2_VISIBILITY = new FullMapVisibility(57, 100, 6);
        private static final MapVisibility TIER_3_VISIBILITY = new FullMapVisibility(30, 100, 6);
        private static final MapVisibility TIER_4_VISIBILITY = new FullMapVisibility(30, 100, 6);

        private final int tier;

        private FoundChestCategory(int tier) {
            this.tier = tier;
        }

        @Override
        public String getCategoryId() {
            return "wynntils:personal:found-chest:tier-" + tier;
        }

        @Override
        public String getName() {
            return "Found Loot Chests";
        }

        @Override
        public MapAttributes getAttributes() {
            return new AbstractMapAttributes() {
                @Override
                public String getIconId() {
                    return "wynntils:icon:lootchest:tier-" + tier;
                }

                @Override
                public String getLabel() {
                    return "Loot Chest Tier " + MathUtils.toRoman(tier);
                }

                @Override
                public int getPriority() {
                    return 500;
                }

                @Override
                public CustomColor getLabelColor() {
                    return CommonColors.GREEN;
                }

                @Override
                public MapVisibility getIconVisibility() {
                    return switch (tier) {
                        case 1 -> TIER_1_VISIBILITY;
                        case 2 -> TIER_2_VISIBILITY;
                        case 3 -> TIER_3_VISIBILITY;
                        case 4 -> TIER_4_VISIBILITY;
                            // This should never happen
                        default -> MapVisibility.ALWAYS;
                    };
                }

                @Override
                public MapVisibility getLabelVisibility() {
                    return MapVisibility.NEVER;
                }
            };
        }
    }

    private static final class ServiceCategory implements MapCategory {
        private static final MapVisibility FAST_TRAVEL_VISIBLITY = new FullMapVisibility(18, 100, 6);
        private static final MapVisibility OTHER_VISIBLITY = new FullMapVisibility(57, 100, 6);

        private final ServiceKind kind;

        private ServiceCategory(ServiceKind kind) {
            this.kind = kind;
        }

        @Override
        public String getCategoryId() {
            return "wynntils:service:" + kind.getMapDataId();
        }

        @Override
        public String getName() {
            return kind.getName();
        }

        @Override
        public MapAttributes getAttributes() {
            return new AbstractMapAttributes() {
                @Override
                public String getLabel() {
                    return kind.getName();
                }

                @Override
                public String getIconId() {
                    return "wynntils:icon:service:" + kind.getMapDataId();
                }

                @Override
                public int getPriority() {
                    return 100;
                }

                @Override
                public CustomColor getLabelColor() {
                    return CommonColors.GREEN;
                }

                @Override
                public MapVisibility getIconVisibility() {
                    if (kind == ServiceKind.FAST_TRAVEL) {
                        return FAST_TRAVEL_VISIBLITY;
                    } else {
                        return OTHER_VISIBLITY;
                    }
                }

                @Override
                public MapVisibility getLabelVisibility() {
                    return MapVisibility.NEVER;
                }
            };
        }
    }

    private static final class CombatCategory implements MapCategory {
        private static final MapVisibility CAVES_VISIBILITY = new FullMapVisibility(31, 100, 6);
        private static final MapVisibility OTHER_VISIBILITY = new FullMapVisibility(19, 100, 6);

        private final CombatKind kind;

        private CombatCategory(CombatKind kind) {
            this.kind = kind;
        }

        @Override
        public String getCategoryId() {
            return "wynntils:content:" + kind.getMapDataId();
        }

        @Override
        public String getName() {
            return kind.getName();
        }

        @Override
        public MapAttributes getAttributes() {
            return new AbstractMapAttributes() {
                @Override
                public String getLabel() {
                    return kind.getName();
                }

                @Override
                public String getIconId() {
                    return "wynntils:icon:content:" + kind.getMapDataId();
                }

                @Override
                public int getPriority() {
                    return 100;
                }

                @Override
                public CustomColor getLabelColor() {
                    return CommonColors.GREEN;
                }

                @Override
                public MapVisibility getIconVisibility() {
                    if (kind == CombatKind.CAVES) {
                        return CAVES_VISIBILITY;
                    } else {
                        return OTHER_VISIBILITY;
                    }
                }

                @Override
                public MapVisibility getLabelVisibility() {
                    return MapVisibility.NEVER;
                }
            };
        }
    }

    private static final class PlaceCategory implements MapCategory {
        private static final MapVisibility PROVINCE_VISIBILITY = new FullMapVisibility(0, 32, 3);
        private static final MapVisibility CITY_VISIBILITY = new FullMapVisibility(0, 74, 3);
        private static final MapVisibility PLACE_VISIBILITY = new FullMapVisibility(32, 86, 3);

        private final Label.LabelLayer layer;

        private PlaceCategory(Label.LabelLayer layer) {
            this.layer = layer;
        }

        @Override
        public String getCategoryId() {
            return "wynntils:place:" + layer.getMapDataId();
        }

        @Override
        public String getName() {
            return layer.getName();
        }

        @Override
        public MapAttributes getAttributes() {
            return new AbstractMapAttributes() {
                @Override
                public String getIconId() {
                    return MapIcon.NO_ICON_ID;
                }

                @Override
                public CustomColor getLabelColor() {
                    return switch (layer) {
                        case PROVINCE -> CommonColors.DARK_AQUA;
                        case CITY -> CommonColors.YELLOW;
                        case TOWN_OR_PLACE -> CommonColors.WHITE;
                    };
                }

                @Override
                public int getPriority() {
                    return 700;
                }

                @Override
                public MapVisibility getLabelVisibility() {
                    return switch (layer) {
                        case PROVINCE -> PROVINCE_VISIBILITY;
                        case CITY -> CITY_VISIBILITY;
                        case TOWN_OR_PLACE -> PLACE_VISIBILITY;
                    };
                }
            };
        }
    }
}
