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

public class MapInheritedAttributes implements MapAttributes {
    private final MapFeature feature;

    public MapInheritedAttributes(MapFeature feature) {
        this.feature = feature;
    }

    @Override
    public String getLabel() {
        return Models.MapData.getFeatureAttribute(feature, MapAttributes::getLabel);
    }

    @Override
    public String getIconId() {
        return Models.MapData.getFeatureAttribute(feature, MapAttributes::getIconId);
    }

    @Override
    public int getPriority() {
        return Models.MapData.getFeatureAttribute(feature, MapAttributes::getPriority);
    }

    @Override
    public MapVisibility getLabelVisibility() {
        return Models.MapData.getFeatureAttribute(feature, MapAttributes::getLabelVisibility);
    }

    @Override
    public CustomColor getLabelColor() {
        return Models.MapData.getFeatureAttribute(feature, MapAttributes::getLabelColor);
    }

    @Override
    public TextShadow getLabelShadow() {
        return Models.MapData.getFeatureAttribute(feature, MapAttributes::getLabelShadow);
    }

    @Override
    public MapVisibility getIconVisibility() {
        return Models.MapData.getFeatureAttribute(feature, MapAttributes::getIconVisibility);
    }

    @Override
    public CustomColor getIconColor() {
        return Models.MapData.getFeatureAttribute(feature, MapAttributes::getIconColor);
    }

    @Override
    public MapDecoration getIconDecoration() {
        return Models.MapData.getFeatureAttribute(feature, MapAttributes::getIconDecoration);
    }
}
