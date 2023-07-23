/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.attributes;

import com.wynntils.core.components.Models;
import com.wynntils.models.mapdata.attributes.type.MapAttributes;
import com.wynntils.models.mapdata.type.MapFeature;
import java.util.function.Function;

public class FullFeatureAttributes extends DerivedAttributes {
    private final MapFeature feature;
    private final MapAttributes attributes;

    public FullFeatureAttributes(MapFeature feature) {
        this.feature = feature;
        this.attributes = feature.getAttributes();
    }

    protected <T> T getAttribute(Function<MapAttributes, T> getter) {
        // Check if the feature has overridden this attribute
        if (attributes != null) {
            T attribute = getter.apply(attributes);
            if (attribute != null && !(attribute instanceof Integer i && i == 0)) {
                return attribute;
            }
        }

        // Otherwise get it from the category
        MapAttributes categoryAttributes = Models.MapData.getFullCategoryAttributes(feature.getCategoryId());
        if (categoryAttributes == null) return null;

        return getter.apply(categoryAttributes);
    }
}
