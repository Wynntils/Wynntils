/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata;

import com.wynntils.core.components.Models;
import com.wynntils.models.mapdata.type.attributes.MapAttributes;
import com.wynntils.models.mapdata.type.attributes.MapDecoration;
import com.wynntils.models.mapdata.type.attributes.MapVisibility;
import com.wynntils.models.mapdata.type.features.MapFeature;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;
import java.util.function.Function;

public class MapInheritedAttributes implements MapAttributes {
    private final MapFeature feature;
    private final MapAttributes attributes;

    public MapInheritedAttributes(MapFeature feature) {
        this.feature = feature;
        this.attributes = feature.getAttributes();
    }

    private <T> T getFeatureAttribute(MapFeature feature, Function<MapAttributes, T> getter) {
        if (attributes != null) {
            T attribute = getter.apply(attributes);
            if (attribute != null) {
                return attribute;
            }
        }

        return getCategoryAttribute(feature.getCategoryId(), getter);
    }

    private <T> T getCategoryAttribute(String categoryId, Function<MapAttributes, T> getter) {
        if (categoryId == null) {
            // FIXME: proper detection for root, proper root style
            return null;
        }

        MapAttributes categoryAttributes = Models.MapData.getCategoryAttributes(categoryId);
        if (categoryAttributes != null) {
            T attribute = getter.apply(categoryAttributes);
            if (attribute != null) {
                return attribute;
            }
        }

        String parentId = getParentCategoryId(categoryId);
        return getCategoryAttribute(parentId, getter);
    }

    private String getParentCategoryId(String categoryId) {
        int index = categoryId.lastIndexOf(':');
        if (index == -1) return null;
        return categoryId.substring(0, index);
    }

    @Override
    public String getLabel() {
        return getFeatureAttribute(feature, MapAttributes::getLabel);
    }

    @Override
    public String getIconId() {
        return getFeatureAttribute(feature, MapAttributes::getIconId);
    }

    @Override
    public int getPriority() {
        return getFeatureAttribute(feature, MapAttributes::getPriority);
    }

    @Override
    public MapVisibility getLabelVisibility() {
        return getFeatureAttribute(feature, MapAttributes::getLabelVisibility);
    }

    @Override
    public CustomColor getLabelColor() {
        return getFeatureAttribute(feature, MapAttributes::getLabelColor);
    }

    @Override
    public TextShadow getLabelShadow() {
        return getFeatureAttribute(feature, MapAttributes::getLabelShadow);
    }

    @Override
    public MapVisibility getIconVisibility() {
        return getFeatureAttribute(feature, MapAttributes::getIconVisibility);
    }

    @Override
    public CustomColor getIconColor() {
        return getFeatureAttribute(feature, MapAttributes::getIconColor);
    }

    @Override
    public MapDecoration getIconDecoration() {
        return getFeatureAttribute(feature, MapAttributes::getIconDecoration);
    }
}
