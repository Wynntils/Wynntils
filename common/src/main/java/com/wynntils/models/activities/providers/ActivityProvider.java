/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.providers;

import com.google.common.base.CaseFormat;
import com.wynntils.models.activities.type.ActivityType;
import com.wynntils.services.mapdata.attributes.MapMarkerOptionsBuilder;
import com.wynntils.services.mapdata.attributes.impl.AbstractMapLocationAttributes;
import com.wynntils.services.mapdata.attributes.type.MapLocationAttributes;
import com.wynntils.services.mapdata.attributes.type.MapMarkerOptions;
import com.wynntils.services.mapdata.features.impl.MapLocationImpl;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import com.wynntils.services.mapdata.providers.builtin.MapIconsProvider;
import com.wynntils.utils.MapDataUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.Location;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ActivityProvider extends BuiltInProvider {
    private ActivityLocation spawnLocation;
    private ActivityLocation trackedActivityLocation;

    public void setSpawnLocation(ActivityType activityType, Location spawnLocation, String activityName) {
        if (spawnLocation == null) {
            this.spawnLocation = null;
        } else {
            this.spawnLocation = new ActivityLocation(activityName, activityType, spawnLocation);
        }
    }

    public void setTrackedActivityLocation(
            ActivityType activityType, Location trackedActivityLocation, String activityName) {
        if (trackedActivityLocation == null) {
            this.trackedActivityLocation = null;
        } else {
            this.trackedActivityLocation = new ActivityLocation(activityName, activityType, trackedActivityLocation);
        }
    }

    @Override
    public String getProviderId() {
        return "activities";
    }

    @Override
    public void reloadData() {
        // This built-in provider does not load any external data, so no explicit reload is needed.
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        Stream.Builder<MapFeature> features = Stream.builder();

        if (spawnLocation != null) {
            features.add(spawnLocation);
        }

        if (trackedActivityLocation != null) {
            features.add(trackedActivityLocation);
        }

        return features.build().filter(Objects::nonNull);
    }

    public static class ActivityLocation extends MapLocationImpl {
        private final String name;
        private final ActivityType activityType;

        public ActivityLocation(String name, ActivityType activityType, Location location) {
            super(
                    MapDataUtils.sanitizeFeatureId("activity-" + (name.isBlank() ? location.hashCode() : name)),
                    "wynntils:activity:" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, activityType.name()),
                    null,
                    location);
            this.name = name;
            this.activityType = activityType;
        }

        @Override
        public Optional<MapLocationAttributes> getAttributes() {
            return Optional.of(new AbstractMapLocationAttributes() {
                @Override
                public Optional<String> getLabel() {
                    return Optional.of(name);
                }

                @Override
                public Optional<CustomColor> getLabelColor() {
                    return Optional.of(activityType.getColor());
                }

                @Override
                public Optional<String> getIconId() {
                    return Optional.of(MapIconsProvider.getIconIdFromTexture(activityType.getTexture()));
                }

                @Override
                public Optional<MapMarkerOptions> getMarkerOptions() {
                    return Optional.of(new MapMarkerOptionsBuilder().withBeaconColor(activityType.getColor()));
                }
            });
        }
    }
}
