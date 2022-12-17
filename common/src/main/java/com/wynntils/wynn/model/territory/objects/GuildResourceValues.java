/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.territory.objects;

import net.minecraft.ChatFormatting;

public enum GuildResourceValues {
    VeryLow("Very Low", ChatFormatting.DARK_GREEN, 1),
    Low("Low", ChatFormatting.GREEN, 2),
    Medium("Medium", ChatFormatting.YELLOW, 3),
    High("High", ChatFormatting.RED, 4),
    VeryHigh("Very High", ChatFormatting.DARK_RED, 5);

    private final String asString;
    private final ChatFormatting color;
    private final int level;

    GuildResourceValues(String asString, ChatFormatting color, int level) {
        this.asString = asString;
        this.color = color;
        this.level = level;
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

    public GuildResourceValues getFilterNext(boolean limited) {
        if (limited) {
            // If is HIGH, go back to LOW since limited
            return ordinal() == 3 ? values()[1] : values()[(ordinal() + 1) % values().length];
        } else {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    public GuildResourceValues getFilterPrevious(boolean limited) {
        if (limited) {
            // If is LOW, go back to HIGH since limited
            return ordinal() == 1 ? values()[3] : values()[(ordinal() + values().length - 1) % values().length];
        } else {
            return values()[(ordinal() + values().length - 1) % values().length];
        }
    }

    public int getLevel() {
        return level;
    }
}
