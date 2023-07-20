/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers.builtin;

import com.wynntils.models.mapdata.providers.BuiltInProvider;
import com.wynntils.models.mapdata.type.MapFeatureCategory;
import com.wynntils.models.mapdata.type.attributes.MapFeatureAttributes;
import com.wynntils.models.mapdata.type.attributes.MapFeatureDecoration;
import com.wynntils.models.mapdata.type.attributes.MapFeatureVisibility;
import com.wynntils.models.mapdata.type.features.MapFeature;
import com.wynntils.models.mapdata.type.features.MapLocation;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.type.TextShadow;
import java.util.List;
import java.util.stream.Stream;

public class CharacterProvider extends BuiltInProvider {
    private static final List<MapFeature> PROVIDED_FEATURES = List.of(new CharacterLocation());
    private static final List<MapFeatureCategory> PROVIDED_CATEGORIES = List.of(new PlayersCategory());

    @Override
    public Stream<MapFeature> getFeatures() {
        return PROVIDED_FEATURES.stream();
    }

    @Override
    public Stream<MapFeatureCategory> getCategories() {
        return PROVIDED_CATEGORIES.stream();
    }

    private static final class PlayersCategory implements MapFeatureCategory {
        @Override
        public String getCategoryId() {
            return "wynntils:players";
        }

        @Override
        public String getDisplayName() {
            return "Your position";
        }

        @Override
        public MapFeatureAttributes getAttributes() {
            return new MapFeatureAttributes() {
                @Override
                public String getLabel() {
                    return "Player position";
                }

                @Override
                public String getIconId() {
                    return "wynntils:icon:waypoint";
                }

                @Override
                public int getPriority() {
                    return 900;
                }

                @Override
                public MapFeatureVisibility getLabelVisibility() {
                    return null;
                }

                @Override
                public CustomColor getLabelColor() {
                    return null;
                }

                @Override
                public TextShadow getLabelShadow() {
                    return null;
                }

                @Override
                public MapFeatureVisibility getIconVisibility() {
                    return null;
                }

                @Override
                public CustomColor getIconColor() {
                    return null;
                }

                @Override
                public MapFeatureDecoration getIconDecoration() {
                    return null;
                }
            };
        }
    }

    private static final class CharacterLocation implements MapLocation {
        @Override
        public String getFeatureId() {
            return "built-in:character:marker";
        }

        @Override
        public String getCategoryId() {
            return "wynntils:players:you";
        }

        @Override
        public MapFeatureAttributes getAttributes() {
            return null;
        }

        @Override
        public List<String> getTags() {
            return List.of();
        }

        @Override
        public Location getLocation() {
            return new Location(McUtils.player().blockPosition());
        }
    }
}
