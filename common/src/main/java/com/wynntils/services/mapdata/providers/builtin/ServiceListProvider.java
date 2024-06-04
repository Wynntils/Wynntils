/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.builtin;

import com.wynntils.services.mapdata.features.ServiceLocation;
import com.wynntils.services.mapdata.type.MapFeature;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ServiceListProvider extends BuiltInProvider {
    private static final List<MapFeature> PROVIDED_FEATURES = new ArrayList<>();

    @Override
    public String getProviderId() {
        return "service-list";
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return PROVIDED_FEATURES.stream();
    }

    public void updateServices(List<ServiceLocation> services) {
        PROVIDED_FEATURES.forEach(this::notifyCallbacks);
        PROVIDED_FEATURES.clear();
        services.forEach(ServiceListProvider::registerFeature);
    }

    private static void registerFeature(ServiceLocation location) {
        PROVIDED_FEATURES.add(location);
    }
}
