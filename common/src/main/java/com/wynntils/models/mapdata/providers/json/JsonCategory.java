/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers.json;

import com.wynntils.models.mapdata.type.MapCategory;
import com.wynntils.models.mapdata.type.attributes.MapAttributes;

public class JsonCategory implements MapCategory {
    private final String id;
    private final String name;
    private final MapAttributes attributes;

    public JsonCategory(String id, String name, MapAttributes attributes) {
        this.id = id;
        this.name = name;
        this.attributes = attributes;
    }

    @Override
    public String getCategoryId() {
        return id;
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
