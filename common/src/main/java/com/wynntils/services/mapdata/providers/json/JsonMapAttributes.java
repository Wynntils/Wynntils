/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.json;

import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapDecoration;
import com.wynntils.services.mapdata.attributes.type.MapMarkerOptions;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;
import java.util.Optional;

// TODO: This class is to be removed
@Deprecated
public class JsonMapAttributes implements MapAttributes {
    private final String label;
    private final String icon;
    private final int priority;
    private final int level;
    private final CustomColor labelColor;
    private final TextShadow labelShadow;
    private final JsonMapVisibility labelVisibility;
    private final CustomColor iconColor;
    private final JsonMapVisibility iconVisibility;

    public JsonMapAttributes(
            String label,
            String icon,
            int priority,
            int level,
            CustomColor labelColor,
            TextShadow labelShadow,
            JsonMapVisibility labelVisibility,
            CustomColor iconColor,
            JsonMapVisibility iconVisibility) {
        this.label = label;
        this.icon = icon;
        this.priority = priority;
        this.level = level;
        this.labelColor = labelColor;
        this.labelShadow = labelShadow;
        this.labelVisibility = labelVisibility;
        this.iconColor = iconColor;
        this.iconVisibility = iconVisibility;
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
    public Optional<MapDecoration> getIconDecoration() {
        // json files can never provide icon decorations; those are only for dynamic features
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> getHasMarker() {
        // json files cannot provide marker information
        return Optional.empty();
    }

    @Override
    public Optional<MapMarkerOptions> getMarkerOptions() {
        // json files cannot provide marker options
        return Optional.empty();
    }

    @Override
    public Optional<Float> getBorderWidth() {
        // json files cannot provide border width
        return Optional.empty();
    }

    @Override
    public Optional<CustomColor> getBorderColor() {
        // json files cannot provide border color
        return Optional.empty();
    }

    @Override
    public Optional<CustomColor> getFillColor() {
        // json files cannot provide fill color
        return Optional.empty();
    }

    @Override
    public Optional<String> getDescription() {
        // json files cannot provide descriptions
        return Optional.empty();
    }
}
