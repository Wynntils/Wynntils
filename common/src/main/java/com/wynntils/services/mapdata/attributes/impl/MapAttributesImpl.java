/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.impl;

import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapDecoration;
import com.wynntils.services.mapdata.attributes.type.MapMarkerOptions;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;
import java.util.Optional;

public class MapAttributesImpl implements MapAttributes {
    private final int priority;
    private final int level;
    private final String label;
    private final MapVisibilityImpl labelVisibility;
    private final CustomColor labelColor;
    private final TextShadow labelShadow;
    private final String icon;
    private final MapVisibilityImpl iconVisibility;
    private final CustomColor iconColor;
    private final Boolean hasMarker;
    private final MapMarkerOptionsImpl markerOptions;
    private final CustomColor fillColor;
    private final CustomColor borderColor;
    private final Float borderWidth;

    public MapAttributesImpl(
            int priority,
            int level,
            String label,
            MapVisibilityImpl labelVisibility,
            CustomColor labelColor,
            TextShadow labelShadow,
            String icon,
            MapVisibilityImpl iconVisibility,
            CustomColor iconColor,
            Boolean hasMarker,
            MapMarkerOptionsImpl markerOptions,
            CustomColor fillColor,
            CustomColor borderColor,
            Float borderWidth) {
        this.priority = priority;
        this.level = level;
        this.label = label;
        this.labelVisibility = labelVisibility;
        this.labelColor = labelColor;
        this.labelShadow = labelShadow;
        this.icon = icon;
        this.iconVisibility = iconVisibility;
        this.iconColor = iconColor;
        this.hasMarker = hasMarker;
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
    public Optional<Boolean> getHasMarker() {
        return Optional.ofNullable(hasMarker);
    }

    @Override
    public Optional<MapMarkerOptions> getMarkerOptions() {
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
        return Optional.ofNullable(borderWidth);
    }

    @Override
    public Optional<MapDecoration> getIconDecoration() {
        // json files can never provide icon decorations; those are only for dynamic features
        return Optional.empty();
    }
}
