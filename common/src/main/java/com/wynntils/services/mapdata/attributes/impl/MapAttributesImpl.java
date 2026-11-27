/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.impl;

import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapDecoration;
import com.wynntils.services.mapdata.attributes.type.MapMarkerOptions;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;
import java.util.List;
import java.util.Optional;

public class MapAttributesImpl implements MapAttributes {
    private final Integer priority;
    private final Integer level;
    private final String label;
    private final String description;
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
    private final String pathIcon;
    private final CustomColor pathFillColor;
    private final CustomColor pathBorderColor;
    private final Float pathBorderWidth;
    private final Float pathWidth;
    private final CustomColor pathColor;
    private final Boolean hasPath;

    public MapAttributesImpl(
            Integer priority,
            Integer level,
            String label,
            String description,
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
            Float borderWidth,
            String pathIcon,
            CustomColor pathFillColor,
            CustomColor pathBorderColor,
            Float pathBorderWidth,
            Float pathWidth,
            CustomColor pathColor,
            Boolean hasPath) {
        this.priority = priority;
        this.level = level;
        this.label = label;
        this.description = description;
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
        this.pathIcon = pathIcon;
        this.pathFillColor = pathFillColor;
        this.pathBorderColor = pathBorderColor;
        this.pathBorderWidth = pathBorderWidth;
        this.pathWidth = pathWidth;
        this.pathColor = pathColor;
        this.hasPath = hasPath;
    }

    public MapAttributesImpl(MapAttributesImpl attributes) {
        this(
                attributes.priority,
                attributes.level,
                attributes.label,
                attributes.description,
                attributes.labelVisibility,
                attributes.labelColor,
                attributes.labelShadow,
                attributes.icon,
                attributes.iconVisibility,
                attributes.iconColor,
                attributes.hasMarker,
                attributes.markerOptions,
                attributes.fillColor,
                attributes.borderColor,
                attributes.borderWidth,
                attributes.pathIcon,
                attributes.pathFillColor,
                attributes.pathBorderColor,
                attributes.pathBorderWidth,
                attributes.pathWidth,
                attributes.pathColor,
                attributes.hasPath);
    }

    @Override
    public Optional<String> getIconId() {
        return Optional.ofNullable(icon);
    }

    @Override
    public Optional<Integer> getPriority() {
        return Optional.ofNullable(priority);
    }

    @Override
    public Optional<Integer> getLevel() {
        return Optional.ofNullable(level);
    }

    @Override
    public Optional<String> getLabel() {
        return Optional.ofNullable(label);
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
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
    public Optional<List<CustomColor>> getFillColors() {
        return Optional.ofNullable(fillColor).map(List::of);
    }

    @Override
    public Optional<List<CustomColor>> getBorderColors() {
        return Optional.ofNullable(borderColor).map(List::of);
    }

    @Override
    public Optional<Float> getBorderWidth() {
        return Optional.ofNullable(borderWidth);
    }

    @Override
    public Optional<String> getPathIconId() {
        return Optional.ofNullable(pathIcon);
    }

    @Override
    public Optional<CustomColor> getPathFillColor() {
        return Optional.ofNullable(pathFillColor);
    }

    @Override
    public Optional<CustomColor> getPathBorderColor() {
        return Optional.ofNullable(pathBorderColor);
    }

    @Override
    public Optional<Float> getPathBorderWidth() {
        return Optional.ofNullable(pathBorderWidth);
    }

    @Override
    public Optional<Float> getPathWidth() {
        return Optional.ofNullable(pathWidth);
    }

    @Override
    public Optional<MapDecoration> getIconDecoration() {
        // json files can never provide icon decorations; those are only for dynamic features
        return Optional.empty();
    }

    @Override
    public Optional<CustomColor> getPathColor() {
        return Optional.ofNullable(pathColor);
    }

    @Override
    public Optional<Boolean> getHasPath() {
        return Optional.ofNullable(hasPath);
    }
}
