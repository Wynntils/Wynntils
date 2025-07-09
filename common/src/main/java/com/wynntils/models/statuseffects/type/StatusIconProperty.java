/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.statuseffects.type;

import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.Texture;
import java.util.Locale;

public enum StatusIconProperty {
    VANISH("Vanish", Texture.STATUS_ICON_PLACEHOLDER, CommonColors.RED);

    private final String name;
    private final Texture texture;
    private final CustomColor color;

    StatusIconProperty(String name, Texture texture, CustomColor color) {
        this.name = name;
        this.texture = texture;
        this.color = color;
    }

    public static StatusIconProperty fromString(String name) {
        String formattedName = name.replace(' ', '_').toUpperCase(Locale.ROOT);
        try {
            return valueOf(formattedName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public CustomColor getColor() {
        return color;
    }

    public Texture getTexture() {
        return texture;
    }

    public String getName() {
        return name;
    }
}
