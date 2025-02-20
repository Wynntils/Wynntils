/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.builtin;

import com.google.common.base.CaseFormat;
import com.wynntils.models.activities.type.ActivityType;
import com.wynntils.models.containers.type.LootChestTier;
import com.wynntils.services.hades.type.PlayerRelation;
import com.wynntils.services.mapdata.attributes.DefaultMapAttributes;
import com.wynntils.services.mapdata.attributes.MapMarkerOptionsBuilder;
import com.wynntils.services.mapdata.attributes.impl.AbstractMapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapMarkerOptions;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.services.mapdata.features.builtin.CombatLocation;
import com.wynntils.services.mapdata.features.builtin.PlaceLocation;
import com.wynntils.services.mapdata.features.builtin.ServiceLocation;
import com.wynntils.services.mapdata.type.MapCategory;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.Texture;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public class CategoriesProvider extends BuiltInProvider {
    private static final List<MapCategory> PROVIDED_CATEGORIES = new ArrayList<>();

    public CategoriesProvider() {
        for (ServiceLocation.ServiceKind kind : ServiceLocation.ServiceKind.values()) {
            PROVIDED_CATEGORIES.add(new ServiceCategory(kind));
        }
        for (CombatLocation.CombatKind kind : CombatLocation.CombatKind.values()) {
            PROVIDED_CATEGORIES.add(new CombatCategory(kind));
        }
        for (PlaceLocation.PlaceType layer : PlaceLocation.PlaceType.values()) {
            PROVIDED_CATEGORIES.add(new PlaceCategory(layer));
        }
        for (int tier = 1; tier <= LootChestTier.values().length; tier++) {
            PROVIDED_CATEGORIES.add(new FoundChestCategory(tier));
        }
        for (PlayerRelation relation : PlayerRelation.values()) {
            PROVIDED_CATEGORIES.add(new RemotePlayerCategory(relation));
        }
        PROVIDED_CATEGORIES.add(new ActivityCategory());
        for (ActivityType activityType : ActivityType.values()) {
            PROVIDED_CATEGORIES.add(new ActivityTypeCategory(activityType));
        }
        PROVIDED_CATEGORIES.add(new WaypointCategory());
        PROVIDED_CATEGORIES.add(new WynntilsCategory());
        PROVIDED_CATEGORIES.add(new PlayersCategory());
        PROVIDED_CATEGORIES.add(new SeaskipperDestinationCategory());
        PROVIDED_CATEGORIES.add(new LootrunCategory());
        PROVIDED_CATEGORIES.add(new GuildAttackCategory());
        PROVIDED_CATEGORIES.add(new UserMarkerCategory());
    }

    @Override
    public String getProviderId() {
        return "categories";
    }

    @Override
    public Stream<MapCategory> getCategories() {
        return PROVIDED_CATEGORIES.stream();
    }

    @Override
    public void reloadData() {}

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

                @Override
                public Optional<MapMarkerOptions> getMarkerOptions() {
                    return Optional.of(new MapMarkerOptionsBuilder().withHasLabel(false));
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
                    return Optional.of(DefaultMapAttributes.LABEL_NEVER);
                }

                @Override
                public Optional<MapMarkerOptions> getMarkerOptions() {
                    return Optional.of(new MapMarkerOptionsBuilder().withHasLabel(true));
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
                    return Optional.of("Loot Chest " + MathUtils.toRoman(tier));
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
                                default -> DefaultMapAttributes.ICON_ALWAYS;
                            });
                }

                @Override
                public Optional<MapVisibility> getLabelVisibility() {
                    return Optional.of(DefaultMapAttributes.LABEL_NEVER);
                }
            });
        }
    }

    private static final class ServiceCategory implements MapCategory {
        private static final MapVisibility FAST_TRAVEL_VISIBILITY =
                MapVisibility.builder().withMin(18f);
        private static final MapVisibility OTHER_VISIBILITY =
                MapVisibility.builder().withMin(57f);

        private final ServiceLocation.ServiceKind kind;

        private ServiceCategory(ServiceLocation.ServiceKind kind) {
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
                    if (kind == ServiceLocation.ServiceKind.FAST_TRAVEL) {
                        return Optional.of(FAST_TRAVEL_VISIBILITY);
                    } else {
                        return Optional.of(OTHER_VISIBILITY);
                    }
                }

                @Override
                public Optional<MapVisibility> getLabelVisibility() {
                    return Optional.of(DefaultMapAttributes.LABEL_NEVER);
                }
            });
        }
    }

    private static final class CombatCategory implements MapCategory {
        private static final MapVisibility CAVES_VISIBILITY =
                MapVisibility.builder().withMin(31f);
        private static final MapVisibility OTHER_VISIBILITY =
                MapVisibility.builder().withMin(19f);

        private final CombatLocation.CombatKind kind;

        private CombatCategory(CombatLocation.CombatKind kind) {
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
                    if (kind == CombatLocation.CombatKind.CAVES) {
                        return Optional.of(CAVES_VISIBILITY);
                    } else {
                        return Optional.of(OTHER_VISIBILITY);
                    }
                }

                @Override
                public Optional<MapVisibility> getLabelVisibility() {
                    return Optional.of(DefaultMapAttributes.LABEL_NEVER);
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

        private final PlaceLocation.PlaceType placeType;

        private PlaceCategory(PlaceLocation.PlaceType placeType) {
            this.placeType = placeType;
        }

        @Override
        public String getCategoryId() {
            return "wynntils:place:" + placeType.getMapDataId();
        }

        @Override
        public Optional<String> getName() {
            return Optional.of(placeType.getName());
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
                    return Optional.of(getPlaceColor());
                }

                @Override
                public Optional<Integer> getPriority() {
                    return Optional.of(700);
                }

                @Override
                public Optional<MapVisibility> getLabelVisibility() {
                    return Optional.of(
                            switch (placeType) {
                                case PROVINCE -> PROVINCE_VISIBILITY;
                                case CITY -> CITY_VISIBILITY;
                                case TOWN_OR_PLACE -> PLACE_VISIBILITY;
                            });
                }

                @Override
                public Optional<MapMarkerOptions> getMarkerOptions() {
                    return Optional.of(new MapMarkerOptionsBuilder()
                            .withHasDistance(true)
                            .withHasLabel(true)
                            .withBeaconColor(getPlaceColor()));
                }
            });
        }

        private CustomColor getPlaceColor() {
            return switch (placeType) {
                case PROVINCE -> CommonColors.DARK_AQUA;
                case CITY -> CommonColors.YELLOW;
                case TOWN_OR_PLACE -> CommonColors.WHITE;
            };
        }
    }

    private static final class PlayersCategory implements MapCategory {
        @Override
        public String getCategoryId() {
            return "wynntils:player";
        }

        @Override
        public Optional<String> getName() {
            return Optional.of("Player positions");
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<Integer> getPriority() {
                    return Optional.of(900);
                }

                @Override
                public Optional<String> getIconId() {
                    return Optional.of("wynntils:icon:player:head");
                }

                @Override
                public Optional<MapVisibility> getIconVisibility() {
                    return Optional.of(DefaultMapAttributes.ICON_ALWAYS);
                }
            });
        }
    }

    private static final class RemotePlayerCategory implements MapCategory {
        private final PlayerRelation relation;

        private RemotePlayerCategory(PlayerRelation relation) {
            this.relation = relation;
        }

        @Override
        public String getCategoryId() {
            return "wynntils:player:" + relation.name().toLowerCase();
        }

        @Override
        public Optional<String> getName() {
            return Optional.of(StringUtils.capitalizeFirst(relation.name().toLowerCase(Locale.ROOT)) + " Players");
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<CustomColor> getLabelColor() {
                    return Optional.of(relation.getRelationColor());
                }
            });
        }
    }

    private static final class ActivityCategory implements MapCategory {
        @Override
        public String getCategoryId() {
            return "wynntils:activity";
        }

        @Override
        public Optional<String> getName() {
            return Optional.of("Activity Locations");
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<Boolean> getHasMarker() {
                    return Optional.of(true);
                }

                @Override
                public Optional<MapMarkerOptions> getMarkerOptions() {
                    return Optional.of(new MapMarkerOptionsBuilder().withHasDistance(true));
                }

                @Override
                public Optional<MapVisibility> getIconVisibility() {
                    return Optional.of(DefaultMapAttributes.ICON_ALWAYS);
                }

                @Override
                public Optional<MapVisibility> getLabelVisibility() {
                    return Optional.of(DefaultMapAttributes.LABEL_ALWAYS);
                }

                @Override
                public Optional<Integer> getPriority() {
                    return Optional.of(900);
                }
            });
        }
    }

    private static final class ActivityTypeCategory implements MapCategory {
        private final ActivityType activityType;

        private ActivityTypeCategory(ActivityType activityType) {
            this.activityType = activityType;
        }

        @Override
        public String getCategoryId() {
            return "wynntils:activity:" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, activityType.name());
        }

        @Override
        public Optional<String> getName() {
            return Optional.of(activityType.getDisplayName() + " Activities");
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<MapMarkerOptions> getMarkerOptions() {
                    return Optional.of(
                            new MapMarkerOptionsBuilder().withHasIcon(true).withBeaconColor(activityType.getColor()));
                }

                @Override
                public Optional<CustomColor> getLabelColor() {
                    return Optional.of(activityType.getColor());
                }

                @Override
                public Optional<String> getIconId() {
                    return Optional.of("wynntils:icon:content:"
                            + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, activityType.name()));
                }
            });
        }
    }

    private static final class SeaskipperDestinationCategory implements MapCategory {
        @Override
        public String getCategoryId() {
            return "wynntils:seaskipper-destination";
        }

        @Override
        public Optional<String> getName() {
            return Optional.of("Seaskipper Destinations");
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<Integer> getPriority() {
                    return Optional.of(900);
                }
            });
        }
    }

    private static final class LootrunCategory implements MapCategory {
        @Override
        public String getCategoryId() {
            return "wynntils:lootrun";
        }

        @Override
        public Optional<String> getName() {
            return Optional.of("Lootrun Locations");
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<MapVisibility> getIconVisibility() {
                    return Optional.of(DefaultMapAttributes.ICON_ALWAYS);
                }

                @Override
                public Optional<MapVisibility> getLabelVisibility() {
                    return Optional.of(DefaultMapAttributes.LABEL_ALWAYS);
                }

                @Override
                public Optional<Boolean> getHasMarker() {
                    return Optional.of(true);
                }

                @Override
                public Optional<MapMarkerOptions> getMarkerOptions() {
                    return Optional.of(new MapMarkerOptionsBuilder()
                            .withHasIcon(true)
                            .withHasLabel(true)
                            .withHasDistance(true));
                }
            });
        }
    }

    private static final class GuildAttackCategory implements MapCategory {
        @Override
        public String getCategoryId() {
            return "wynntils:territory:attack";
        }

        @Override
        public Optional<String> getName() {
            return Optional.of("Guild Attack Locations");
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<MapVisibility> getLabelVisibility() {
                    return Optional.of(DefaultMapAttributes.LABEL_ALWAYS);
                }

                @Override
                public Optional<MapVisibility> getIconVisibility() {
                    return Optional.of(DefaultMapAttributes.ICON_ALWAYS);
                }

                @Override
                public Optional<Boolean> getHasMarker() {
                    return Optional.of(true);
                }

                @Override
                public Optional<MapMarkerOptions> getMarkerOptions() {
                    return Optional.of(new MapMarkerOptionsBuilder()
                            .withHasIcon(true)
                            .withHasLabel(true)
                            .withHasDistance(true));
                }
            });
        }
    }

    private static final class UserMarkerCategory implements MapCategory {
        @Override
        public String getCategoryId() {
            return "wynntils:personal:user-marker";
        }

        @Override
        public Optional<String> getName() {
            return Optional.of("User Markers");
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<Integer> getPriority() {
                    return Optional.of(1000);
                }

                @Override
                public Optional<MapVisibility> getIconVisibility() {
                    return Optional.of(DefaultMapAttributes.ICON_ALWAYS);
                }

                @Override
                public Optional<MapVisibility> getLabelVisibility() {
                    return Optional.of(DefaultMapAttributes.LABEL_ALWAYS);
                }

                @Override
                public Optional<String> getIconId() {
                    return Optional.of(MapIconsProvider.getIconIdFromTexture(Texture.WAYPOINT));
                }

                @Override
                public Optional<CustomColor> getLabelColor() {
                    return Optional.of(CommonColors.WHITE);
                }

                @Override
                public Optional<Boolean> getHasMarker() {
                    return Optional.of(true);
                }

                @Override
                public Optional<MapMarkerOptions> getMarkerOptions() {
                    return Optional.of(
                            new MapMarkerOptionsBuilder().withHasLabel(true).build());
                }
            });
        }
    }
}
