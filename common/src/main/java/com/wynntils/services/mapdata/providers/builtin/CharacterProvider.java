/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.builtin;

import com.wynntils.services.mapdata.attributes.AbstractMapAttributes;
import com.wynntils.services.mapdata.attributes.FixedMapVisibility;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.services.mapdata.type.MapCategory;
import com.wynntils.services.mapdata.type.MapFeature;
import com.wynntils.services.mapdata.type.MapLocation;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import java.util.List;
import java.util.Optional;
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
                public Optional<MapVisibility> getIconVisibility() {
                    return Optional.of(FixedMapVisibility.ICON_ALWAYS);
                }
            });
        }
    }

    private static final class CharacterLocation implements MapLocation {
        @Override
        public String getFeatureId() {
            return "built-in:character:location";
        }

        @Override
        public String getCategoryId() {
            return "wynntils:players:you";
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<MapVisibility> getIconVisibility() {
                    return Optional.of(FixedMapVisibility.ICON_NEVER);
                }
            });
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
