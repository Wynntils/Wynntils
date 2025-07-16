/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.providers;

import com.google.common.base.CaseFormat;
import com.wynntils.core.components.Models;
import com.wynntils.models.activities.type.ActivityType;
import com.wynntils.services.mapdata.attributes.impl.AbstractMapLocationAttributes;
import com.wynntils.services.mapdata.attributes.type.MapLocationAttributes;
import com.wynntils.services.mapdata.features.impl.MapLocationImpl;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import com.wynntils.utils.MapDataUtils;
import com.wynntils.utils.mc.type.Location;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ActivityProvider extends BuiltInProvider {
    private static final String ACTIVITY_LOCATION_NAME = "Activity Location";

    private ActivityLocation spawnLocation;
    private ActivityLocation trackedActivityLocation;

    public void setSpawnLocation(ActivityType activityType, Location spawnLocation) {
        if (spawnLocation == null) {
            this.spawnLocation = null;
        } else {
            this.spawnLocation = new ActivityLocation(ACTIVITY_LOCATION_NAME, activityType, spawnLocation);
        }
    }

    public void setTrackedActivityLocation(ActivityType activityType, Location trackedActivityLocation) {
        if (trackedActivityLocation == null) {
            this.trackedActivityLocation = null;
        } else {
            this.trackedActivityLocation =
                    new ActivityLocation(Models.Activity.getTrackedName(), activityType, trackedActivityLocation);
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

        if (trackedActivityLocation != null) {
            features.add(trackedActivityLocation);
        }

        if (spawnLocation != null
                && (trackedActivityLocation != null
                        && !spawnLocation.getLocation().equals(trackedActivityLocation.getLocation()))) {
            features.add(spawnLocation);
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
                    return ACTIVITY_LOCATION_NAME.equals(name) ? Optional.empty() : Optional.of(name);
                }
            });
        }
    }
}
