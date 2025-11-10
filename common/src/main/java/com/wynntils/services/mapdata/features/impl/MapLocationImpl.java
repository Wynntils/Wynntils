/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.features.impl;

import com.wynntils.services.mapdata.attributes.impl.MapLocationAttributesImpl;
import com.wynntils.services.mapdata.attributes.type.MapLocationAttributes;
import com.wynntils.services.mapdata.features.type.MapLocation;
import com.wynntils.utils.mc.type.Location;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link MapLocation}. Extend this class, if a serializable subclass is desired.
 */
public class MapLocationImpl implements MapLocation {
    private final String featureId;
    private final String categoryId;
    private final MapLocationAttributesImpl attributes;
    private final Location location;

    public MapLocationImpl(
            String featureId, String categoryId, MapLocationAttributesImpl attributes, Location location) {
        this.featureId = featureId;
        this.categoryId = categoryId;
        this.attributes = attributes;
        this.location = location;
    }

    @Override
    public String getFeatureId() {
        return featureId;
    }

    @Override
    public String getCategoryId() {
        return categoryId;
    }

    @Override
    public Optional<MapLocationAttributes> getAttributes() {
        return Optional.ofNullable(attributes);
    }

    @Override
    public List<String> getTags() {
        return List.of();
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public boolean validate() {
        return featureId != null && categoryId != null && location != null;
    }
}
