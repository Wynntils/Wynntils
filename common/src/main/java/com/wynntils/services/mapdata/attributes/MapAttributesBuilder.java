/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes;

import com.wynntils.core.WynntilsMod;
import com.wynntils.services.mapdata.attributes.impl.MapAreaAttributesImpl;
import com.wynntils.services.mapdata.attributes.impl.MapLocationAttributesImpl;
import com.wynntils.services.mapdata.attributes.impl.MapMarkerOptionsImpl;
import com.wynntils.services.mapdata.attributes.impl.MapPathAttributesImpl;
import com.wynntils.services.mapdata.attributes.impl.MapVisibilityImpl;
import com.wynntils.services.mapdata.attributes.type.MapAreaAttributes;
import com.wynntils.services.mapdata.attributes.type.MapLocationAttributes;
import com.wynntils.services.mapdata.attributes.type.MapPathAttributes;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;
import java.lang.reflect.Field;

public class MapAttributesBuilder {
    private Integer priority;
    private Integer level;
    private String label;
    private String secondaryLabel;
    private MapVisibilityImpl labelVisibility;
    private CustomColor labelColor;
    private TextShadow labelShadow;
    private String icon;
    private MapVisibilityImpl iconVisibility;
    private CustomColor iconColor;
    private Boolean hasMarker;
    private MapMarkerOptionsImpl markerOptions;
    private CustomColor fillColor;
    private CustomColor borderColor;
    private Float borderWidth;

    public MapAttributesBuilder setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    public MapAttributesBuilder setLevel(Integer level) {
        this.level = level;
        return this;
    }

    public MapAttributesBuilder setLabel(String label) {
        this.label = label;
        return this;
    }

    public MapAttributesBuilder setSecondaryLabel(String secondaryLabel) {
        this.secondaryLabel = secondaryLabel;
        return this;
    }

    public MapAttributesBuilder setLabelVisibility(MapVisibilityImpl labelVisibility) {
        this.labelVisibility = labelVisibility;
        return this;
    }

    public MapAttributesBuilder setLabelColor(CustomColor labelColor) {
        this.labelColor = labelColor;
        return this;
    }

    public MapAttributesBuilder setLabelShadow(TextShadow labelShadow) {
        this.labelShadow = labelShadow;
        return this;
    }

    public MapAttributesBuilder setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public MapAttributesBuilder setIconVisibility(MapVisibilityImpl iconVisibility) {
        this.iconVisibility = iconVisibility;
        return this;
    }

    public MapAttributesBuilder setIconColor(CustomColor iconColor) {
        this.iconColor = iconColor;
        return this;
    }

    public MapAttributesBuilder setHasMarker(Boolean hasMarker) {
        this.hasMarker = hasMarker;
        return this;
    }

    public MapAttributesBuilder setMarkerOptions(MapMarkerOptionsImpl markerOptions) {
        this.markerOptions = markerOptions;
        return this;
    }

    public void setFillColor(CustomColor fillColor) {
        this.fillColor = fillColor;
    }

    public void setBorderColor(CustomColor borderColor) {
        this.borderColor = borderColor;
    }

    public void setBorderWidth(Float borderWidth) {
        this.borderWidth = borderWidth;
    }

    public MapLocationAttributesBuilder asLocationAttributes() {
        return new MapLocationAttributesBuilder();
    }

    public MapPathAttributesBuilder asPathAttributes() {
        return new MapPathAttributesBuilder();
    }

    public MapAreaAttributesBuilder asAreaAttributes() {
        return new MapAreaAttributesBuilder();
    }

    protected void checkInvalidAttribute(String fieldName) {
        if (!WynntilsMod.isDevelopmentBuild()) return;

        try {
            // Use reflection to get our field given by the name
            Field field = MapAttributesBuilder.class.getDeclaredField(fieldName);
            if (field.get(this) != null) {
                throw new IllegalStateException("Unsupported attribute set: " + fieldName);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Silently ignore invalid field checks
        }
    }

    public final class MapLocationAttributesBuilder extends MapAttributesBuilder {
        public MapLocationAttributesImpl build() {
            MapLocationAttributes.getUnsupportedAttributes().forEach(this::checkInvalidAttribute);

            return new MapLocationAttributesImpl(
                    priority,
                    level,
                    label,
                    secondaryLabel,
                    labelVisibility,
                    labelColor,
                    labelShadow,
                    icon,
                    iconVisibility,
                    iconColor,
                    hasMarker,
                    markerOptions);
        }
    }

    public final class MapPathAttributesBuilder extends MapAttributesBuilder {
        public MapPathAttributesImpl build() {
            MapPathAttributes.getUnsupportedAttributes().forEach(this::checkInvalidAttribute);

            return new MapPathAttributesImpl(
                    priority, level, label, secondaryLabel, labelVisibility, labelColor, labelShadow);
        }
    }

    public final class MapAreaAttributesBuilder extends MapAttributesBuilder {
        public MapAreaAttributesImpl build() {
            MapAreaAttributes.getUnsupportedAttributes().forEach(this::checkInvalidAttribute);

            return new MapAreaAttributesImpl(
                    priority,
                    level,
                    label,
                    secondaryLabel,
                    labelVisibility,
                    labelColor,
                    labelShadow,
                    fillColor,
                    borderColor,
                    borderWidth);
        }
    }
}
