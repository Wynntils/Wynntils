/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers.json;

import com.wynntils.models.mapdata.type.attributes.MapAttributes;
import com.wynntils.models.mapdata.type.features.MapLocation;
import com.wynntils.utils.mc.type.Location;
import java.util.List;

public class JsonMapLocation implements MapLocation {
    private final String id;
    private final String category;
    private final MapAttributes attributes;
    private final Location location;

    public JsonMapLocation(String id, String category, MapAttributes attributes, Location location) {
        this.id = id;
        this.category = category;
        this.attributes = attributes;
        this.location = location;
    }

    @Override
    public String getFeatureId() {
        return id;
    }

    @Override
    public String getCategoryId() {
        return category;
    }

    @Override
    public MapAttributes getAttributes() {
        return attributes;
    }

    @Override
    public List<String> getTags() {
        return List.of();
    }

    @Override
    public Location getLocation() {
        return location;
    }
}
