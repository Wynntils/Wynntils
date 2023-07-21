/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.attributes;

import com.wynntils.models.mapdata.attributes.type.MapAttributes;
import com.wynntils.models.mapdata.attributes.type.MapDecoration;
import com.wynntils.models.mapdata.attributes.type.MapVisibility;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;

public abstract class AbstractMapAttributes implements MapAttributes {
    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public String getIconId() {
        return null;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public MapVisibility getLabelVisibility() {
        return null;
    }

    @Override
    public CustomColor getLabelColor() {
        return null;
    }

    @Override
    public TextShadow getLabelShadow() {
        return null;
    }

    @Override
    public MapVisibility getIconVisibility() {
        return null;
    }

    @Override
    public CustomColor getIconColor() {
        return null;
    }

    @Override
    public MapDecoration getIconDecoration() {
        return null;
    }
}
