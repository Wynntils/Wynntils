/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers.json;

import com.wynntils.models.mapdata.type.MapCategory;
import com.wynntils.models.mapdata.type.attributes.MapAttributes;

public class JsonCategory implements MapCategory {
    private final String categoryId;
    private final String name;
    private final MapAttributes attributes;

    public JsonCategory(String categoryId, String name, MapAttributes attributes) {
        this.categoryId = categoryId;
        this.name = name;
        this.attributes = attributes;
    }

    @Override
    public String getCategoryId() {
        return categoryId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public MapAttributes getAttributes() {
        return attributes;
    }
}
