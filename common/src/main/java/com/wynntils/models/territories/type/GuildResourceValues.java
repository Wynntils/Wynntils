/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.territories.type;

import net.minecraft.ChatFormatting;

public enum GuildResourceValues {
    VERY_LOW("Very Low", ChatFormatting.DARK_GREEN, 1),
    LOW("Low", ChatFormatting.GREEN, 2),
    MEDIUM("Medium", ChatFormatting.YELLOW, 3),
    HIGH("High", ChatFormatting.RED, 4),
    VERY_HIGH("Very High", ChatFormatting.DARK_RED, ChatFormatting.AQUA, 5);

    private final String asString;
    private final ChatFormatting defenceColor;
    private final ChatFormatting treasuryColor;
    private final int level;

    GuildResourceValues(String asString, ChatFormatting color, int level) {
        this.asString = asString;
        this.defenceColor = color;
        this.treasuryColor = color;
        this.level = level;
    }

    GuildResourceValues(String asString, ChatFormatting defenceColor, ChatFormatting treasuryColor, int level) {
        this.asString = asString;
        this.defenceColor = defenceColor;
        this.treasuryColor = treasuryColor;
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

    public String getAsString() {
        return asString;
    }

    public ChatFormatting getDefenceColor() {
        return defenceColor;
    }

    public ChatFormatting getTreasuryColor() {
        return treasuryColor;
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
