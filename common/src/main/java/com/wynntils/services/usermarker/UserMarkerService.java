/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.usermarker;

import com.wynntils.core.components.Service;
import com.wynntils.core.components.Services;
import com.wynntils.services.mapdata.MapDataService;
import com.wynntils.services.mapdata.attributes.DefaultMapAttributes;
import com.wynntils.services.mapdata.attributes.MapAttributesBuilder;
import com.wynntils.services.mapdata.attributes.MapMarkerOptionsBuilder;
import com.wynntils.services.mapdata.attributes.impl.AbstractMapAttributes;
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

public class UserMarkerService extends Service {
    // These are map features that already exist in the map data, and are "marked" by the user
    private final Set<MapLocation> userOverridenMapFeatures = new CopyOnWriteArraySet<>();

    // There are markers that are created by the user, and do not exist in the map data, but they are still "marked"
    private final Set<MapLocation> userMarkerMapFeatures = new CopyOnWriteArraySet<>();

    private static final String MARKED_OVERRIDE_PROVIDER_ID = "override:user_marked_provider";
    private final UserMarkerOverrideProvider userMarkedOverrideProvider = new UserMarkerOverrideProvider();

    private final UserMarkerProvider userMarkerProvider = new UserMarkerProvider();

    public UserMarkerService(MapDataService mapData) {
        super(List.of(mapData));

        Services.MapData.registerOverrideProvider(MARKED_OVERRIDE_PROVIDER_ID, userMarkedOverrideProvider);
        Services.MapData.registerBuiltInProvider(userMarkerProvider);
    }

    public void addMarkerAtLocation(Location location, String name) {
        MapLocation userMarker = new UserMarker(location, name);
        userMarkerMapFeatures.add(userMarker);
        userMarkedOverrideProvider.notifyCallbacks(userMarker);
    }

    public void addMarkerAtLocation(Location location) {
        addMarkerAtLocation(location, "Waypoint");
    }

    public void removeMarkerAtLocation(Location location) {
        userMarkerMapFeatures.stream()
                .filter(feature -> feature instanceof UserMarker)
                .filter(feature -> feature.getLocation().equals(location))
                .findFirst()
                .ifPresent(userMarker -> {
                    userMarkerMapFeatures.remove(userMarker);
                    userMarkedOverrideProvider.notifyCallbacks(userMarker);
                });
    }

    public void addUserMarkedFeature(MapLocation mapFeature) {
        userOverridenMapFeatures.add(mapFeature);
        userMarkedOverrideProvider.notifyCallbacks(mapFeature);
    }

    public void removeUserMarkedFeature(MapLocation mapFeature) {
        userOverridenMapFeatures.remove(mapFeature);
        userMarkedOverrideProvider.notifyCallbacks(mapFeature);
    }

    public void removeAllUserMarkedFeatures() {
        List<MapFeature> removedFeatures = new ArrayList<>(userOverridenMapFeatures);
        userOverridenMapFeatures.clear();
        removedFeatures.forEach(userMarkedOverrideProvider::notifyCallbacks);

        removedFeatures = new ArrayList<>(userMarkerMapFeatures);
        userMarkerMapFeatures.clear();
        removedFeatures.forEach(userMarkedOverrideProvider::notifyCallbacks);
    }

    public Stream<MapLocation> getMarkedFeatures() {
        return Stream.concat(userOverridenMapFeatures.stream(), userMarkerMapFeatures.stream());
    }

    private static final class UserMarker extends MapLocationImpl {
        private final String name;
        private final MapLocationAttributesImpl userMarkerAttributes;

        private UserMarker(Location location, String name) {
            super("user-marker-" + location.hashCode(), "wynntils:personal:waypoint:user-marker", null, location);
            this.name = name;
            this.userMarkerAttributes = new MapAttributesBuilder()
                    .setHasMarker(true)
                    .setIcon(MapIconsProvider.getIconIdFromTexture(Texture.WAYPOINT))
                    .setIconVisibility(DefaultMapAttributes.ICON_ALWAYS)
                    .setLabel(name)
                    .setLabelVisibility(DefaultMapAttributes.LABEL_ALWAYS)
                    .setLabelColor(CommonColors.WHITE)
                    .setMarkerOptions(
                            new MapMarkerOptionsBuilder().withHasLabel(false).build())
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
            return userMarkerMapFeatures.stream().map(f -> f);
        }
    }

    private final class UserMarkerOverrideProvider extends AbstractMapDataOverrideProvider {
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
            return userOverridenMapFeatures.stream().map(MapFeature::getFeatureId);
        }

        @Override
        public Stream<String> getOverridenCategoryIds() {
            return Stream.empty();
        }
    }
}
