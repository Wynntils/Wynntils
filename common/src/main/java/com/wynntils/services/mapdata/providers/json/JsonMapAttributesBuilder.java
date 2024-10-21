/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.json;

import com.wynntils.core.WynntilsMod;
import com.wynntils.services.mapdata.attributes.type.MapAreaAttributes;
import com.wynntils.services.mapdata.attributes.type.MapLocationAttributes;
import com.wynntils.services.mapdata.attributes.type.MapPathAttributes;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;
import java.lang.reflect.Field;

public class JsonMapAttributesBuilder {
    private String label;
    private String icon;
    private int priority;
    private int level;
    private CustomColor labelColor;
    private TextShadow labelShadow;
    private JsonMapVisibility labelVisibility;
    private CustomColor iconColor;
    private JsonMapVisibility iconVisibility;
    private JsonMarkerOptions markerOptions;

    public JsonMapAttributesBuilder setLabel(String label) {
        this.label = label;
        return this;
    }

    public JsonMapAttributesBuilder setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public JsonMapAttributesBuilder setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public JsonMapAttributesBuilder setLevel(int level) {
        this.level = level;
        return this;
    }

    public JsonMapAttributesBuilder setLabelColor(CustomColor labelColor) {
        this.labelColor = labelColor;
        return this;
    }

    public JsonMapAttributesBuilder setLabelShadow(TextShadow labelShadow) {
        this.labelShadow = labelShadow;
        return this;
    }

    public JsonMapAttributesBuilder setLabelVisibility(JsonMapVisibility labelVisibility) {
        this.labelVisibility = labelVisibility;
        return this;
    }

    public JsonMapAttributesBuilder setIconColor(CustomColor iconColor) {
        this.iconColor = iconColor;
        return this;
    }

    public JsonMapAttributesBuilder setIconVisibility(JsonMapVisibility iconVisibility) {
        this.iconVisibility = iconVisibility;
        return this;
    }

    public JsonMapAttributesBuilder setMarkerOptions(JsonMarkerOptions markerOptions) {
        this.markerOptions = markerOptions;
        return this;
    }

    public JsonMapLocationAttributesBuilder asLocationAttributes() {
        return new JsonMapLocationAttributesBuilder();
    }

    public JsonMapPathAttributesBuilder asPathAttributes() {
        return new JsonMapPathAttributesBuilder();
    }

    public JsonMapAreaAttributesBuilder asAreaAttributes() {
        return new JsonMapAreaAttributesBuilder();
    }

    protected void checkInvalidAttribute(String fieldName) {
        if (!WynntilsMod.isDevelopmentBuild()) return;

        try {
            // Use reflection to get our field given by the name
            Field field = JsonMapAttributesBuilder.class.getDeclaredField(fieldName);
            if (field.get(this) != null) {
                throw new IllegalStateException("Unsupported attribute set: " + fieldName);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Silently ignore invalid field checks
        }
    }

    public final class JsonMapLocationAttributesBuilder extends JsonMapAttributesBuilder {
        public JsonMapLocationAttributes build() {
            MapLocationAttributes.getUnsupportedAttributes().forEach(this::checkInvalidAttribute);

            return new JsonMapLocationAttributes(
                    label,
                    icon,
                    priority,
                    level,
                    labelColor,
                    labelShadow,
                    labelVisibility,
                    iconColor,
                    iconVisibility,
                    markerOptions);
        }
    }

    public final class JsonMapPathAttributesBuilder extends JsonMapAttributesBuilder {
        public JsonMapPathAttributes build() {
            MapPathAttributes.getUnsupportedAttributes().forEach(this::checkInvalidAttribute);

            return new JsonMapPathAttributes(label, priority, level, labelColor, labelShadow, labelVisibility);
        }
    }

    public final class JsonMapAreaAttributesBuilder extends JsonMapAttributesBuilder {
        public JsonMapAreaAttributes build() {
            MapAreaAttributes.getUnsupportedAttributes().forEach(this::checkInvalidAttribute);

            return new JsonMapAreaAttributes(
                    label, icon, priority, level, labelColor, labelShadow, labelVisibility, iconColor, iconVisibility);
        }
    }
}
