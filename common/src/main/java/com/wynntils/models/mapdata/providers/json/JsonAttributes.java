/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers.json;

import com.wynntils.models.mapdata.type.attributes.MapAttributes;
import com.wynntils.models.mapdata.type.attributes.MapDecoration;
import com.wynntils.models.mapdata.type.attributes.MapVisibility;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;

public class JsonAttributes implements MapAttributes {
    private final String label;
    private final String iconId;
    private final int priority;
    private final CustomColor labelColor;
    private final TextShadow labelShadow;
    private final MapVisibility labelVisibility;
    private final CustomColor iconColor;
    private final MapVisibility iconVisibility;

    public JsonAttributes(
            String label,
            String iconId,
            int priority,
            CustomColor labelColor,
            TextShadow labelShadow,
            MapVisibility labelVisibility,
            CustomColor iconColor,
            MapVisibility iconVisibility) {
        this.label = label;
        this.iconId = iconId;
        this.priority = priority;
        this.labelColor = labelColor;
        this.labelShadow = labelShadow;
        this.labelVisibility = labelVisibility;
        this.iconColor = iconColor;
        this.iconVisibility = iconVisibility;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getIconId() {
        return iconId;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public MapVisibility getLabelVisibility() {
        return labelVisibility;
    }

    @Override
    public CustomColor getLabelColor() {
        return labelColor;
    }

    @Override
    public TextShadow getLabelShadow() {
        return labelShadow;
    }

    @Override
    public MapVisibility getIconVisibility() {
        return iconVisibility;
    }

    @Override
    public CustomColor getIconColor() {
        return iconColor;
    }

    @Override
    public MapDecoration getIconDecoration() {
        // json files can never provide icon decorations; those are only for dynamic features
        return null;
    }
}
