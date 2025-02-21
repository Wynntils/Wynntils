/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.usermarker;

import com.wynntils.core.components.Service;
import com.wynntils.core.components.Services;
import com.wynntils.core.mod.event.WynntilsInitEvent;
import com.wynntils.services.mapdata.attributes.DefaultMapAttributes;
import com.wynntils.services.mapdata.attributes.MapAttributesBuilder;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.features.type.MapLocation;
import com.wynntils.services.mapdata.providers.type.AbstractMapDataOverrideProvider;
import com.wynntils.services.usermarker.providers.UserMarkerProvider;
import com.wynntils.services.usermarker.type.UserMarkerLocation;
import com.wynntils.utils.mc.type.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;
import net.neoforged.bus.api.SubscribeEvent;

public class UserMarkerService extends Service {
    public static final MapAttributesBuilder MARKED_MAP_FEATURE_ATTRIBUTES = new MapAttributesBuilder()
            .setHasMarker(true)
            .setPriority(1000)
            .setIconVisibility(DefaultMapAttributes.ICON_ALWAYS)
            .setLabelVisibility(DefaultMapAttributes.LABEL_ALWAYS);

    private static final UserMarkerProvider USER_MARKER_PROVIDER = new UserMarkerProvider();

    // These are map features that already exist in the map data, and are "marked" by the user
    private final Set<MapLocation> userOverridenMapLocations = new CopyOnWriteArraySet<>();

    // There are markers that are created by the user, and do not exist in the map data, but they are still "marked"
    private final Set<UserMarkerLocation> userMarkerMapLocations = new CopyOnWriteArraySet<>();

    private static final String MARKED_OVERRIDE_PROVIDER_ID = "override:user_marked_provider";
    private final UserMarkerOverrideProvider userMarkedOverrideProvider = new UserMarkerOverrideProvider();

    public UserMarkerService() {
        super(List.of());
    }

    @SubscribeEvent
    public void onModInitFinished(WynntilsInitEvent.ModInitFinished event) {
        Services.MapData.registerOverrideProvider(MARKED_OVERRIDE_PROVIDER_ID, userMarkedOverrideProvider);
        Services.MapData.registerBuiltInProvider(USER_MARKER_PROVIDER);
    }

    public void addMarkerAtLocation(Location location, String name) {
        UserMarkerLocation userMarker = new UserMarkerLocation(location, name);
        userMarkerMapLocations.add(userMarker);
        USER_MARKER_PROVIDER.updateMarkers(userMarkerMapLocations);
    }

    public void addMarkerAtLocation(Location location) {
        addMarkerAtLocation(location, "Waypoint");
    }

    public void removeMarkerAtLocation(Location location) {
        userMarkerMapLocations.stream()
                .filter(feature -> feature instanceof UserMarkerLocation)
                .filter(feature -> feature.getLocation().equals(location))
                .findFirst()
                .ifPresent(userMarker -> {
                    userMarkerMapLocations.remove(userMarker);
                    userMarkedOverrideProvider.notifyCallbacks(userMarker);
                });
        USER_MARKER_PROVIDER.updateMarkers(userMarkerMapLocations);
    }

    public boolean isMarkerAtLocation(Location location) {
        return userMarkerMapLocations.stream()
                .filter(feature -> feature instanceof UserMarkerLocation)
                .anyMatch(feature -> feature.getLocation().equals(location));
    }

    public void addUserMarkedFeature(MapLocation mapFeature) {
        userOverridenMapLocations.add(mapFeature);
        userMarkedOverrideProvider.notifyCallbacks(mapFeature);
    }

    public void removeUserMarkedFeature(MapLocation mapFeature) {
        userOverridenMapLocations.remove(mapFeature);
        userMarkedOverrideProvider.notifyCallbacks(mapFeature);
    }

    public boolean isUserMarkedFeature(MapLocation mapFeature) {
        return userOverridenMapLocations.contains(mapFeature);
    }

    public void removeAllUserMarkedFeatures() {
        List<MapFeature> removedFeatures = new ArrayList<>(userOverridenMapLocations);
        userOverridenMapLocations.clear();
        removedFeatures.forEach(userMarkedOverrideProvider::notifyCallbacks);

        userMarkerMapLocations.clear();
        USER_MARKER_PROVIDER.updateMarkers(userMarkerMapLocations);
    }

    public boolean isFeatureMarked(MapFeature mapFeature) {
        if (!(mapFeature instanceof MapLocation mapLocation)) return false;
        return userOverridenMapLocations.contains(mapLocation)
                || (mapLocation instanceof UserMarkerLocation && userMarkerMapLocations.contains(mapLocation));
    }

    public Stream<MapLocation> getMarkedFeatures() {
        return Stream.concat(userOverridenMapLocations.stream(), userMarkerMapLocations.stream());
    }

    private final class UserMarkerOverrideProvider extends AbstractMapDataOverrideProvider {
        private static final MapAttributes BUILT_MAP_LOCATION_ATTRIBUTES =
                MARKED_MAP_FEATURE_ATTRIBUTES.asLocationAttributes().build();

        @Override
        public MapAttributes getOverrideAttributes(MapFeature mapFeature) {
            return BUILT_MAP_LOCATION_ATTRIBUTES;
        }

        @Override
        public Stream<String> getOverridenFeatureIds() {
            return userOverridenMapLocations.stream().map(MapFeature::getFeatureId);
        }

        @Override
        public Stream<String> getOverridenCategoryIds() {
            return Stream.empty();
        }
    }
}
