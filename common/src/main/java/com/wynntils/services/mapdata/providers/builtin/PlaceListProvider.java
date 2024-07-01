/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.builtin;

import com.wynntils.services.map.Label;
import com.wynntils.services.mapdata.attributes.AbstractMapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.type.MapFeature;
import com.wynntils.services.mapdata.type.MapLocation;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.type.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class PlaceListProvider extends BuiltInProvider {
    private static final List<MapFeature> PROVIDED_FEATURES = new ArrayList<>();

    @Override
    public String getProviderId() {
        return "place-list";
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return PROVIDED_FEATURES.stream();
    }

    public static void registerFeature(Label label) {
        PROVIDED_FEATURES.add(new PlaceLocation(label));
    }

    private static final class PlaceLocation implements MapLocation {
        private final Label label;

        private PlaceLocation(Label label) {
            this.label = label;
        }

        @Override
        public String getFeatureId() {
            return StringUtils.createSlug(label.getName());
        }

        @Override
        public String getCategoryId() {
            return "wynntils:place:" + label.getLayer().getMapDataId();
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<String> getLabel() {
                    return Optional.of(label.getName());
                }

                @Override
                public Optional<Integer> getLevel() {
                    return label.getLevel();
                }
            });
        }

        @Override
        public List<String> getTags() {
            return List.of();
        }

        @Override
        public Location getLocation() {
            return label.getLocation();
        }
    }
}
