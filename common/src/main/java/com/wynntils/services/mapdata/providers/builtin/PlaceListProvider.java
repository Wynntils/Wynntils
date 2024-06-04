/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.builtin;

import com.wynntils.services.mapdata.providers.json.JsonMapAttributes;
import com.wynntils.services.mapdata.providers.json.JsonMapLocation;
import com.wynntils.services.mapdata.type.MapFeature;
import com.wynntils.utils.mc.type.Location;
import java.util.ArrayList;
import java.util.List;
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

    public void updatePlaces(List<PlaceLocation> places) {
        PROVIDED_FEATURES.forEach(this::notifyCallbacks);
        PROVIDED_FEATURES.clear();
        places.forEach(PlaceListProvider::registerFeatures);
    }

    private static void registerFeatures(PlaceLocation location) {
        PROVIDED_FEATURES.add(location);
    }

    public static final class PlaceLocation extends JsonMapLocation {
        private PlaceLocation(String featureId, String categoryId, JsonMapAttributes attributes, Location location) {
            super(featureId, categoryId, attributes, location);
        }
    }
}
