package com.wynntils.models.mapdata.providers.json;

import com.wynntils.models.mapdata.type.MapFeatureCategory;
import com.wynntils.models.mapdata.type.attributes.MapFeatureAttributes;

public class JsonCategory implements MapFeatureCategory {
    private final String id;
    private final String name;
    private final MapFeatureAttributes attributes;

    public JsonCategory(String id, String name, MapFeatureAttributes attributes) {
        this.id = id;
        this.name = name;
        this.attributes = attributes;
    }

    @Override
    public String getCategoryId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public MapFeatureAttributes getAttributes() {
        return attributes;
    }
}
