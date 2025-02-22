/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.seaskipper.providers;

import com.wynntils.models.seaskipper.type.SeaskipperDestination;
import com.wynntils.models.seaskipper.type.SeaskipperDestinationArea;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SeaskipperDestinationAreaProvider extends BuiltInProvider {
    private static final List<SeaskipperDestinationArea> PROVIDED_FEATURES = new ArrayList<>();

    @Override
    public String getProviderId() {
        return "seaskipper-destination";
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return PROVIDED_FEATURES.stream().map(feature -> feature);
    }

    @Override
    public void reloadData() {}

    public void updateDestinations(List<SeaskipperDestination> destinations) {
        PROVIDED_FEATURES.forEach(this::notifyCallbacks);
        PROVIDED_FEATURES.clear();
        destinations.stream()
                .map(SeaskipperDestinationArea::new)
                .forEach(SeaskipperDestinationAreaProvider::registerFeature);
    }

    private static void registerFeature(SeaskipperDestinationArea destination) {
        PROVIDED_FEATURES.add(destination);
    }
}
