/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.usermarker;

import com.wynntils.core.components.Service;
import com.wynntils.core.components.Services;
import com.wynntils.core.mod.event.WynntilsInitEvent;
import com.wynntils.services.mapdata.MapDataService;
import com.wynntils.services.mapdata.attributes.DefaultMapAttributes;
import com.wynntils.services.mapdata.attributes.MapAttributesBuilder;
import com.wynntils.services.mapdata.attributes.impl.MapLocationAttributesImpl;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapLocationAttributes;
import com.wynntils.services.mapdata.features.impl.MapLocationImpl;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.features.type.MapLocation;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import com.wynntils.services.mapdata.providers.builtin.MapIconsProvider;
import com.wynntils.services.mapdata.providers.type.AbstractMapDataOverrideProvider;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.Texture;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;
import net.neoforged.bus.api.SubscribeEvent;

public class UserMarkerService extends Service {
    private static final MapAttributesBuilder MARKED_MAP_FEATURE_ATTRIBUTES = new MapAttributesBuilder()
            .setHasMarker(true)
            .setPriority(1000)
            .setIconVisibility(DefaultMapAttributes.ICON_ALWAYS)
            .setLabelVisibility(DefaultMapAttributes.LABEL_ALWAYS);

    // These are map features that already exist in the map data, and are "marked" by the user
    private final Set<MapLocation> userOverridenMapLocations = new CopyOnWriteArraySet<>();

    // There are markers that are created by the user, and do not exist in the map data, but they are still "marked"
    private final Set<MapLocation> userMarkerMapLocations = new CopyOnWriteArraySet<>();

    private static final String MARKED_OVERRIDE_PROVIDER_ID = "override:user_marked_provider";
    private final UserMarkerOverrideProvider userMarkedOverrideProvider = new UserMarkerOverrideProvider();

    private final UserMarkerProvider userMarkerProvider = new UserMarkerProvider();

    public UserMarkerService(MapDataService mapData) {
        super(List.of(mapData));

        Services.MapData.registerOverrideProvider(MARKED_OVERRIDE_PROVIDER_ID, userMarkedOverrideProvider);
    }

    @SubscribeEvent
    public void onModInitFinished(WynntilsInitEvent.ModInitFinished event) {
        Services.MapData.registerBuiltInProvider(userMarkerProvider);
    }

    public void addMarkerAtLocation(Location location, String name) {
        MapLocation userMarker = new UserMarker(location, name);
        userMarkerMapLocations.add(userMarker);
        userMarkedOverrideProvider.notifyCallbacks(userMarker);
    }

    public void addMarkerAtLocation(Location location) {
        addMarkerAtLocation(location, "Waypoint");
    }

    public void removeMarkerAtLocation(Location location) {
        userMarkerMapLocations.stream()
                .filter(feature -> feature instanceof UserMarker)
                .filter(feature -> feature.getLocation().equals(location))
                .findFirst()
                .ifPresent(userMarker -> {
                    userMarkerMapLocations.remove(userMarker);
                    userMarkedOverrideProvider.notifyCallbacks(userMarker);
                });
    }

    public void addUserMarkedFeature(MapLocation mapFeature) {
        userOverridenMapLocations.add(mapFeature);
        userMarkedOverrideProvider.notifyCallbacks(mapFeature);
    }

    public void removeUserMarkedFeature(MapLocation mapFeature) {
        userOverridenMapLocations.remove(mapFeature);
        userMarkedOverrideProvider.notifyCallbacks(mapFeature);
    }

    public void removeAllUserMarkedFeatures() {
        List<MapFeature> removedFeatures = new ArrayList<>(userOverridenMapLocations);
        userOverridenMapLocations.clear();
        removedFeatures.forEach(userMarkedOverrideProvider::notifyCallbacks);

        removedFeatures = new ArrayList<>(userMarkerMapLocations);
        userMarkerMapLocations.clear();
        removedFeatures.forEach(userMarkedOverrideProvider::notifyCallbacks);
    }

    public boolean isFeatureMarked(MapFeature mapFeature) {
        if (!(mapFeature instanceof MapLocation mapLocation)) return false;
        return userOverridenMapLocations.contains(mapLocation) || userMarkerMapLocations.contains(mapLocation);
    }

    public Stream<MapLocation> getMarkedFeatures() {
        return Stream.concat(userOverridenMapLocations.stream(), userMarkerMapLocations.stream());
    }

    private static final class UserMarker extends MapLocationImpl {
        private final String name;
        private final MapLocationAttributesImpl userMarkerAttributes;

        private UserMarker(Location location, String name) {
            super("user-marker-" + location.hashCode(), "wynntils:personal:waypoint:user-marker", null, location);
            this.name = name;
            this.userMarkerAttributes = MARKED_MAP_FEATURE_ATTRIBUTES
                    .setIcon(MapIconsProvider.getIconIdFromTexture(Texture.WAYPOINT))
                    .setLabel(name)
                    .setLabelColor(CommonColors.WHITE)
                    .asLocationAttributes()
                    .build();
        }

        @Override
        public Optional<MapLocationAttributes> getAttributes() {
            return Optional.of(userMarkerAttributes);
        }
    }

    private final class UserMarkerProvider extends BuiltInProvider {
        @Override
        public String getProviderId() {
            return "user-markers";
        }

        @Override
        public void reloadData() {
            // This built-in provider does not load any external data, so no explicit reload is needed.
        }

        @Override
        public Stream<MapFeature> getFeatures() {
            return userMarkerMapLocations.stream().map(f -> f);
        }
    }

    private final class UserMarkerOverrideProvider extends AbstractMapDataOverrideProvider {
        private static final MapAttributes BUILT_MAP_LOCATION_ATTRIBUTES =
                MARKED_MAP_FEATURE_ATTRIBUTES.asLocationAttributes().build();

        @Override
        public MapAttributes getOverrideAttributes() {
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
