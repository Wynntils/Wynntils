/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.features.impl;

import com.wynntils.services.mapdata.attributes.impl.MapAreaAttributesImpl;
import com.wynntils.services.mapdata.attributes.type.MapAreaAttributes;
import com.wynntils.services.mapdata.features.type.MapArea;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.BoundingPolygon;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link MapArea}. Extend this class, if a serializable subclass is desired.
 */
public class MapAreaImpl implements MapArea {
    private final String featureId;
    private final String categoryId;
    private final MapAreaAttributesImpl attributes;
    private final List<Location> polygonArea;

    private transient BoundingPolygon boundingPolygon;

    public MapAreaImpl(
            String featureId, String categoryId, MapAreaAttributesImpl attributes, List<Location> polygonArea) {
        this.featureId = featureId;
        this.categoryId = categoryId;
        this.attributes = attributes;
        this.polygonArea = polygonArea;

        // Compute the bounding polygon
        // (the caching happens in the getter for json deserialization)
        getBoundingPolygon();
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
    public Optional<MapAreaAttributes> getAttributes() {
        return Optional.ofNullable(attributes);
    }

    @Override
    public List<String> getTags() {
        return List.of();
    }

    @Override
    public List<Location> getPolygonArea() {
        return polygonArea;
    }

    @Override
    public BoundingPolygon getBoundingPolygon() {
        return boundingPolygon != null
                ? boundingPolygon
                : (boundingPolygon = BoundingPolygon.fromLocations(polygonArea));
    }

    public boolean validate() {
        return featureId != null && categoryId != null && polygonArea != null && !polygonArea.isEmpty();
    }
}
