/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.features.impl;

import com.wynntils.services.mapdata.attributes.impl.MapPathAttributesImpl;
import com.wynntils.services.mapdata.attributes.type.MapPathAttributes;
import com.wynntils.services.mapdata.features.type.MapPath;
import com.wynntils.utils.mc.type.Location;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link MapPath}. Extend this class, if a serializable subclass is desired.
 */
public class MapPathImpl implements MapPath {
    private final String featureId;
    private final String categoryId;
    private final MapPathAttributesImpl attributes;
    private final List<Location> path;

    public MapPathImpl(String featureId, String categoryId, MapPathAttributesImpl attributes, List<Location> path) {
        this.featureId = featureId;
        this.categoryId = categoryId;
        this.attributes = attributes;
        this.path = path;
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
    public Optional<MapPathAttributes> getAttributes() {
        return Optional.ofNullable(attributes);
    }

    @Override
    public List<String> getTags() {
        return List.of();
    }

    @Override
    public List<Location> getPath() {
        return path;
    }
}
