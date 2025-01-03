/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.marker;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Services;
import com.wynntils.models.marker.type.MarkerInfo;
import com.wynntils.models.marker.type.MarkerProvider;
import com.wynntils.services.map.pois.Poi;
import com.wynntils.services.mapdata.attributes.impl.AbstractMapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.providers.type.MapDataOverrideProvider;
import com.wynntils.services.mapdata.type.MapDataProvidedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class MarkerModel extends Model {
    public static final UserWaypointMarkerProvider USER_WAYPOINTS_PROVIDER = new UserWaypointMarkerProvider();

    private final Set<MapFeature> userMarkedMapFeatures = new CopyOnWriteArraySet<>();

    private static final String MARKED_OVERRIDE_PROVIDER_ID = "override:user_marked_provider";
    private final UserMarkerOverrideProvider userMarkedOverrideProvider = new UserMarkerOverrideProvider();

    private final List<MarkerProvider> markerProviders = new ArrayList<>();

    public MarkerModel() {
        super(List.of());

        registerMarkerProvider(USER_WAYPOINTS_PROVIDER);
    }

    @Override
    public void reloadData() {
        // FIXME: This should only be done once, in the  constructor but referencing Services in Model contructors
        //        is not alloved. This is easily fixed by making this model a service, as essentially, it is one.
        Services.MapData.registerOverrideProvider(MARKED_OVERRIDE_PROVIDER_ID, userMarkedOverrideProvider);
    }

    public void addUserMarkedFeature(MapFeature mapFeature) {
        userMarkedMapFeatures.add(mapFeature);
        userMarkedOverrideProvider.notifyCallbacks(mapFeature);
    }

    public void registerMarkerProvider(MarkerProvider provider) {
        markerProviders.add(provider);
    }

    public Stream<MarkerInfo> getAllMarkers() {
        return markerProviders.stream().filter(MarkerProvider::isEnabled).flatMap(MarkerProvider::getMarkerInfos);
    }

    public Stream<Poi> getAllPois() {
        return markerProviders.stream().filter(MarkerProvider::isEnabled).flatMap(MarkerProvider::getPois);
    }

    private final class UserMarkerOverrideProvider implements MapDataOverrideProvider {
        private final Set<Consumer<MapDataProvidedType>> callbacks = new CopyOnWriteArraySet<>();

        @Override
        public MapAttributes getOverrideAttributes() {
            return new AbstractMapAttributes() {
                @Override
                public Optional<Boolean> getHasMarker() {
                    return Optional.of(true);
                }
            };
        }

        @Override
        public Stream<String> getOverridenFeatureIds() {
            return userMarkedMapFeatures.stream().map(MapFeature::getFeatureId);
        }

        @Override
        public Stream<String> getOverridenCategoryIds() {
            return Stream.empty();
        }

        @Override
        public void onChange(Consumer<MapDataProvidedType> callback) {
            callbacks.add(callback);
        }

        public void notifyCallbacks(MapDataProvidedType type) {
            callbacks.forEach(c -> c.accept(type));
        }
    }
}
