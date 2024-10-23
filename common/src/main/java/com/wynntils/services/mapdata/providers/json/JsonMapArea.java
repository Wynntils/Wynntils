/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.json;

import com.wynntils.services.mapdata.attributes.type.MapAreaAttributes;
import com.wynntils.services.mapdata.type.MapArea;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.BoundingPolygon;
import java.util.List;
import java.util.Optional;

public final class JsonMapArea implements MapArea {
    private final String featureId;
    private final String categoryId;
    private final JsonMapAreaAttributes attributes;
    private final List<Location> polygonArea;

    private final transient BoundingPolygon boundingPolygon;

    public JsonMapArea(
            String featureId, String categoryId, JsonMapAreaAttributes attributes, List<Location> polygonArea) {
        this.featureId = featureId;
        this.categoryId = categoryId;
        this.attributes = attributes;
        this.polygonArea = polygonArea;

        this.boundingPolygon = BoundingPolygon.fromLocations(polygonArea);
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
        return boundingPolygon;
    }
}
