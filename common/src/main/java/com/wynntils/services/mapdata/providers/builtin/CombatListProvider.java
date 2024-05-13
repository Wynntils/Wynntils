/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.builtin;

import com.wynntils.services.map.type.CombatKind;
import com.wynntils.services.mapdata.attributes.AbstractMapAttributes;
import com.wynntils.services.mapdata.attributes.impl.FadingMapVisiblity;
import com.wynntils.services.mapdata.attributes.impl.NeverMapVisibility;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.services.mapdata.type.MapFeature;
import com.wynntils.services.mapdata.type.MapLocation;
import com.wynntils.utils.mc.type.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CombatListProvider extends BuiltInProvider {
    private static final List<MapFeature> PROVIDED_FEATURES = new ArrayList<>();
    private static int counter;

    @Override
    public String getProviderId() {
        return "combat-list";
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return PROVIDED_FEATURES.stream();
    }

    public static void registerFeature(Location location, CombatKind kind, String name) {
        PROVIDED_FEATURES.add(new CombatLocation(location, kind, name));
    }

    private static final class CombatLocation implements MapLocation {
        private final Location location;
        private final CombatKind kind;
        private final String name;
        private final int number;

        private CombatLocation(Location location, CombatKind kind, String name) {
            this.location = location;
            this.kind = kind;
            this.name = name;
            this.number = CombatListProvider.counter++;
        }

        @Override
        public String getFeatureId() {
            return kind.getMapDataId() + "-" + number;
        }

        @Override
        public String getCategoryId() {
            return "wynntils:content:" + kind.getMapDataId();
        }

        @Override
        public MapAttributes getAttributes() {
            return new AbstractMapAttributes() {
                @Override
                public String getLabel() {
                    return name;
                }

                @Override
                public MapVisibility getIconVisibility() {
                    if (kind == CombatKind.CAVES) {
                        return new FadingMapVisiblity(31, 100, 6);
                    } else {
                        return new FadingMapVisiblity(19, 100, 6);
                    }
                }

                @Override
                public MapVisibility getLabelVisibility() {
                    return new NeverMapVisibility();
                }
            };
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
