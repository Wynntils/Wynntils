/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.territory.objects;

import net.minecraft.ChatFormatting;

public enum GuildResourceValues {
    VeryLow("Very Low", ChatFormatting.DARK_GREEN),
    Low("Low", ChatFormatting.GREEN),
    Medium("Medium", ChatFormatting.YELLOW),
    High("High", ChatFormatting.RED),
    VeryHigh("Very High", ChatFormatting.DARK_RED);

    private final String asString;
    private final ChatFormatting color;

    GuildResourceValues(String asString, ChatFormatting color) {
        this.asString = asString;
        this.color = color;
    }

    public static GuildResourceValues fromString(String string) {
        for (GuildResourceValues value : values()) {
            if (value.asString.equals(string)) {
                return value;
            }
        }

        return null;
    }

    public String asColoredString() {
        return color + asString;
    }
}
