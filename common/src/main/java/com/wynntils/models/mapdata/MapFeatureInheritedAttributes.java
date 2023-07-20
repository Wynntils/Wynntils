/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata;

import com.wynntils.core.components.Models;
import com.wynntils.models.mapdata.type.attributes.MapFeatureAttributes;
import com.wynntils.models.mapdata.type.attributes.MapFeatureDecoration;
import com.wynntils.models.mapdata.type.attributes.MapFeatureVisibility;
import com.wynntils.models.mapdata.type.features.MapFeature;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;

public class MapFeatureInheritedAttributes implements MapFeatureAttributes {
    private final MapFeature feature;

    public MapFeatureInheritedAttributes(MapFeature feature) {
        this.feature = feature;
    }

    @Override
    public String getLabel() {
        return Models.MapData.getFeatureAttribute(feature, MapFeatureAttributes::getLabel);
    }

    @Override
    public String getIconId() {
        return Models.MapData.getFeatureAttribute(feature, MapFeatureAttributes::getIconId);
    }

    @Override
    public int getPriority() {
        return Models.MapData.getFeatureAttribute(feature, MapFeatureAttributes::getPriority);
    }

    @Override
    public MapFeatureVisibility getLabelVisibility() {
        return Models.MapData.getFeatureAttribute(feature, MapFeatureAttributes::getLabelVisibility);
    }

    @Override
    public CustomColor getLabelColor() {
        return Models.MapData.getFeatureAttribute(feature, MapFeatureAttributes::getLabelColor);
    }

    @Override
    public TextShadow getLabelShadow() {
        return Models.MapData.getFeatureAttribute(feature, MapFeatureAttributes::getLabelShadow);
    }

    @Override
    public MapFeatureVisibility getIconVisibility() {
        return Models.MapData.getFeatureAttribute(feature, MapFeatureAttributes::getIconVisibility);
    }

    @Override
    public CustomColor getIconColor() {
        return Models.MapData.getFeatureAttribute(feature, MapFeatureAttributes::getIconColor);
    }

    @Override
    public MapFeatureDecoration getIconDecoration() {
        return Models.MapData.getFeatureAttribute(feature, MapFeatureAttributes::getIconDecoration);
    }
}
