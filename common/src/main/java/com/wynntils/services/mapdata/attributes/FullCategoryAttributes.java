/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes;

import com.wynntils.core.components.Services;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.type.MapCategory;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class FullCategoryAttributes extends DerivedAttributes {
    private final String categoryId;

    public FullCategoryAttributes(String categoryId) {
        this.categoryId = categoryId;
    }

    protected <T> T getAttribute(Function<MapAttributes, T> getter) {
        for (String id = categoryId; id != null; id = getParentCategoryId(id)) {
            Stream<MapAttributes> allAttributes = Services.MapData.getCategoryDefinitions(id)
                    .map(MapCategory::getAttributes)
                    .filter(Objects::nonNull);
            Optional<T> attribute =
                    allAttributes.map(getter).filter(Objects::nonNull).findFirst();
            if (attribute.isPresent() && !(attribute.get() instanceof Integer i && i == 0)) {
                return attribute.get();
            }
        }

        return null;
    }

    private String getParentCategoryId(String categoryId) {
        int index = categoryId.lastIndexOf(':');
        if (index == -1) return null;
        return categoryId.substring(0, index);
    }
}
