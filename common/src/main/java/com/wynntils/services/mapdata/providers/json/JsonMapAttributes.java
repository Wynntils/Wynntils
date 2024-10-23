/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.json;

import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapDecoration;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.services.mapdata.attributes.type.MarkerOptions;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;
import java.util.Optional;

public class JsonMapAttributes implements MapAttributes {
    private final int priority;
    private final int level;
    private final String label;
    private final JsonMapVisibility labelVisibility;
    private final CustomColor labelColor;
    private final TextShadow labelShadow;
    private final String icon;
    private final JsonMapVisibility iconVisibility;
    private final CustomColor iconColor;
    private final JsonMarkerOptions markerOptions;
    private final CustomColor fillColor;
    private final CustomColor borderColor;
    private final float borderWidth;

    public JsonMapAttributes(
            int priority,
            int level,
            String label,
            JsonMapVisibility labelVisibility,
            CustomColor labelColor,
            TextShadow labelShadow,
            String icon,
            JsonMapVisibility iconVisibility,
            CustomColor iconColor,
            JsonMarkerOptions markerOptions,
            CustomColor fillColor,
            CustomColor borderColor,
            float borderWidth) {
        this.priority = priority;
        this.level = level;
        this.label = label;
        this.labelVisibility = labelVisibility;
        this.labelColor = labelColor;
        this.labelShadow = labelShadow;
        this.icon = icon;
        this.iconVisibility = iconVisibility;
        this.iconColor = iconColor;
        this.markerOptions = markerOptions;
        this.fillColor = fillColor;
        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
    }

    @Override
    public Optional<String> getLabel() {
        return Optional.ofNullable(label);
    }

    @Override
    public Optional<String> getIconId() {
        return Optional.ofNullable(icon);
    }

    @Override
    public Optional<Integer> getPriority() {
        return Optional.of(priority);
    }

    @Override
    public Optional<Integer> getLevel() {
        return Optional.of(level);
    }

    @Override
    public Optional<MapVisibility> getLabelVisibility() {
        return Optional.ofNullable(labelVisibility);
    }

    @Override
    public Optional<CustomColor> getLabelColor() {
        return Optional.ofNullable(labelColor);
    }

    @Override
    public Optional<TextShadow> getLabelShadow() {
        return Optional.ofNullable(labelShadow);
    }

    @Override
    public Optional<MapVisibility> getIconVisibility() {
        return Optional.ofNullable(iconVisibility);
    }

    @Override
    public Optional<CustomColor> getIconColor() {
        return Optional.ofNullable(iconColor);
    }

    @Override
    public Optional<MarkerOptions> getMarkerOptions() {
        return Optional.ofNullable(markerOptions);
    }

    @Override
    public Optional<CustomColor> getFillColor() {
        return Optional.ofNullable(fillColor);
    }

    @Override
    public Optional<CustomColor> getBorderColor() {
        return Optional.ofNullable(borderColor);
    }

    @Override
    public Optional<Float> getBorderWidth() {
        return Optional.of(borderWidth);
    }

    @Override
    public Optional<MapDecoration> getIconDecoration() {
        // json files can never provide icon decorations; those are only for dynamic features
        return Optional.empty();
    }
}
