package com.wynntils.models.mapdata.providers.json;

import com.wynntils.models.mapdata.type.attributes.MapFeatureAttributes;
import com.wynntils.models.mapdata.type.attributes.MapFeatureDecoration;
import com.wynntils.models.mapdata.type.attributes.MapFeatureVisibility;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;

public class JsonAttributes implements MapFeatureAttributes {
    private final String label;
    private final String icon;
    private final int priority;
    private final CustomColor labelColor;
    private final TextShadow labelShadow;
    private final MapFeatureVisibility labelVisibility;
    private final CustomColor iconColor;
    private final MapFeatureVisibility iconVisibility;

    public JsonAttributes(String label, String icon, int priority, CustomColor labelColor, TextShadow labelShadow, MapFeatureVisibility labelVisibility, CustomColor iconColor, MapFeatureVisibility iconVisibility) {
        this.label = label;
        this.icon = icon;
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
        return icon;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public MapFeatureVisibility getLabelVisibility() {
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
    public MapFeatureVisibility getIconVisibility() {
        return iconVisibility;
    }

    @Override
    public CustomColor getIconColor() {
        return iconColor;
    }

    @Override
    public MapFeatureDecoration getIconDecoration() {
        // json files can never provide icon decorations; those are only for dynamic features
        return null;
    }
}
