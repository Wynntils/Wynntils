/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.json;

import com.wynntils.services.mapdata.attributes.type.MapPathAttributes;
import com.wynntils.services.mapdata.type.MapPath;
import com.wynntils.utils.mc.type.Location;
import java.util.List;
import java.util.Optional;

public final class JsonMapPath implements MapPath {
    private final String featureId;
    private final String categoryId;
    private final JsonMapPathAttributes attributes;
    private final List<Location> path;

    public JsonMapPath(String featureId, String categoryId, JsonMapPathAttributes attributes, List<Location> path) {
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
