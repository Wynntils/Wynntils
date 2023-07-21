/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers.builtin;

import com.wynntils.models.mapdata.attributes.type.MapAttributes;
import com.wynntils.models.mapdata.type.MapFeature;
import com.wynntils.models.mapdata.type.MapLocation;
import com.wynntils.utils.mc.type.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ServiceListProvider extends BuiltInProvider {
    private static final List<MapFeature> PROVIDED_FEATURES = new ArrayList<>();
    private static int counter;

    @Override
    public String getProviderId() {
        return "service-list";
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return PROVIDED_FEATURES.stream();
    }

    public static void registerFeature(Location location, String categoryId) {
        PROVIDED_FEATURES.add(new ServiceLocation(location, categoryId));
    }

    private static final class ServiceLocation implements MapLocation {
        private final Location location;
        private final String categoryId;
        private final int number;

        private ServiceLocation(Location location, String categoryId) {
            this.location = location;
            this.categoryId = categoryId;
            this.number = ServiceListProvider.counter++;
        }

        @Override
        public String getFeatureId() {
            return categoryId + "-" + number;
        }

        @Override
        public String getCategoryId() {
            return categoryId;
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
            return location;
        }
    }
}
