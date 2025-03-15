/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.usermarker.providers;

import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import com.wynntils.services.usermarker.type.UserMarkerLocation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class UserMarkerProvider extends BuiltInProvider {
    private static final List<MapFeature> PROVIDED_FEATURES = new ArrayList<>();

    @Override
    public String getProviderId() {
        return "user-markers";
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return PROVIDED_FEATURES.stream();
    }

    @Override
    public void reloadData() {
        // This built-in provider does not load any external data, so no explicit reload is needed.
    }

    public void updateMarkers(Set<UserMarkerLocation> userMarkers) {
        PROVIDED_FEATURES.forEach(this::notifyCallbacks);
        PROVIDED_FEATURES.clear();
        userMarkers.forEach(UserMarkerProvider::registerFeature);
    }

    private static void registerFeature(UserMarkerLocation userMarker) {
        PROVIDED_FEATURES.add(userMarker);
    }
}
