/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.content.type;

import net.minecraft.ChatFormatting;

public enum ContentType {
    QUEST("Quest", ChatFormatting.AQUA.getChar()),
    STORYLINE_QUEST("Quest", ChatFormatting.GREEN.getChar()),
    MINI_QUEST("Mini-Quest", ChatFormatting.DARK_PURPLE.getChar()),
    CAVE("Cave", ChatFormatting.GOLD.getChar()),
    SECRET_DISCOVERY("Secret Discovery", ChatFormatting.AQUA.getChar()),
    WORLD_DISCOVERY("World Discovery", ChatFormatting.YELLOW.getChar()),
    TERRITORIAL_DISCOVERY("Territorial Discovery", ChatFormatting.WHITE.getChar()),
    DUNGEON("Dungeon", ChatFormatting.RED.getChar()),
    RAID("Raid", ChatFormatting.YELLOW.getChar()),
    BOSS_ALTAR("Boss Altar", ChatFormatting.LIGHT_PURPLE.getChar()),
    LOOTRUN_CAMP("Lootrun Camp", ChatFormatting.BLUE.getChar());

    private final String displayName;
    private final char colorCode;

    ContentType(String displayName, char colorCode) {
        this.displayName = displayName;
        this.colorCode = colorCode;
    }

    public static ContentType from(String colorCode, String displayName) {
        for (ContentType type : values()) {
            if (type.getColorCode().equals(colorCode) && type.getDisplayName().equals(displayName)) return type;
        }

        return null;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return String.valueOf(colorCode);
    }
}
