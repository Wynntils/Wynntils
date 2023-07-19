/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.style;

import com.wynntils.core.components.Models;
import com.wynntils.models.mapdata.type.MapFeature;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;

public class MapFeatureConcreteAttributes implements MapFeatureAttributes {
    private final MapFeature feature;

    public MapFeatureConcreteAttributes(MapFeature feature) {
        this.feature = feature;
    }

    @Override
    public String getLabel() {
        return Models.MapData.getFeatureAttribute(feature, MapFeatureAttributes::getLabel);
    }

    @Override
    public MapIcon getIcon() {
        return Models.MapData.getFeatureAttribute(feature, MapFeatureAttributes::getIcon);
    }

    @Override
    public Integer getPriority() {
        return Models.MapData.getFeatureAttribute(feature, MapFeatureAttributes::getPriority);
    }

    @Override
    public MapVisibility getLabelVisibility() {
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
    public MapVisibility getIconVisibility() {
        return Models.MapData.getFeatureAttribute(feature, MapFeatureAttributes::getIconVisibility);
    }

    @Override
    public CustomColor getIconColor() {
        return Models.MapData.getFeatureAttribute(feature, MapFeatureAttributes::getIconColor);
    }

    @Override
    public MapIconDecoration getIconDecoration() {
        return Models.MapData.getFeatureAttribute(feature, MapFeatureAttributes::getIconDecoration);
    }
}
