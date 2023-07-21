/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers.builtin;

import com.wynntils.models.mapdata.type.MapCategory;
import com.wynntils.models.mapdata.type.attributes.MapAttributes;
import com.wynntils.models.mapdata.type.attributes.MapDecoration;
import com.wynntils.models.mapdata.type.attributes.MapVisibility;
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
    private static final List<MapCategory> PROVIDED_CATEGORIES = List.of(new PlayersCategory());

    @Override
    public String getProviderId() {
        return "character";
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return PROVIDED_FEATURES.stream();
    }

    @Override
    public Stream<MapCategory> getCategories() {
        return PROVIDED_CATEGORIES.stream();
    }

    private static final class PlayersCategory implements MapCategory {
        @Override
        public String getCategoryId() {
            return "wynntils:players";
        }

        @Override
        public String getName() {
            return "Your position";
        }

        @Override
        public MapAttributes getAttributes() {
            return new MapAttributes() {
                @Override
                public String getLabel() {
                    return "Player position";
                }

                @Override
                public String getIconId() {
                    return "wynntils:icon:symbols:waypoint";
                }

                @Override
                public int getPriority() {
                    return 900;
                }

                @Override
                public MapVisibility getLabelVisibility() {
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
                public MapVisibility getIconVisibility() {
                    return null;
                }

                @Override
                public CustomColor getIconColor() {
                    return null;
                }

                @Override
                public MapDecoration getIconDecoration() {
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
        public MapAttributes getAttributes() {
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
