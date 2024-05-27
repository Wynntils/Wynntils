/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.json;

import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.type.MapCategory;
import java.util.Optional;

public class JsonCategory implements MapCategory {
    private final String id;
    private final Optional<String> name;
    private final Optional<MapAttributes> attributes;

    public JsonCategory(String id, Optional<String> name, Optional<MapAttributes> attributes) {
        this.id = id;
        this.name = name;
        this.attributes = attributes;
    }

    @Override
    public String getCategoryId() {
        return id;
    }

    @Override
    public Optional<String> getName() {
        return name;
    }

    @Override
    public Optional<MapAttributes> getAttributes() {
        return attributes;
    }
}
