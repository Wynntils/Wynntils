/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.type;

import net.minecraft.ChatFormatting;

public enum ActivityType {
    QUEST("Quest", "quests", ChatFormatting.AQUA.getChar()),
    STORYLINE_QUEST("Quest", "quests", ChatFormatting.GREEN.getChar()),
    MINI_QUEST("Mini-Quest", "mini-quests", ChatFormatting.DARK_PURPLE.getChar()),
    CAVE("Cave", "caves", ChatFormatting.GOLD.getChar()),
    SECRET_DISCOVERY("Secret Discovery", "secret discoveries", ChatFormatting.AQUA.getChar()),
    WORLD_DISCOVERY("World Discovery", "world discoveries", ChatFormatting.YELLOW.getChar()),
    TERRITORIAL_DISCOVERY("Territorial Discovery", "territorial discoveries", ChatFormatting.WHITE.getChar()),
    DUNGEON("Dungeon", "dungeons", ChatFormatting.RED.getChar()),
    RAID("Raid", "raids", ChatFormatting.YELLOW.getChar()),
    BOSS_ALTAR("Boss Altar", "boss altars", ChatFormatting.LIGHT_PURPLE.getChar()),
    LOOTRUN_CAMP("Lootrun Camp", "lootrun camps", ChatFormatting.BLUE.getChar());

    private final String displayName;
    private final String groupName;
    private final char colorCode;

    ActivityType(String displayName, String groupName, char colorCode) {
        this.displayName = displayName;
        this.groupName = groupName;
        this.colorCode = colorCode;
    }

    public static ActivityType from(String colorCode, String displayName) {
        for (ActivityType type : values()) {
            if (type.getColorCode().equals(colorCode) && type.getDisplayName().equals(displayName)) return type;
        }

        return null;
    }

    /** This version cannot distinguish between QUEST and STORYLINE_QUEST */
    public static ActivityType from(String displayName) {
        for (ActivityType type : values()) {
            if (type.getDisplayName().equals(displayName)) return type;
        }

        // FIXME: Workaround to stop crashes
        if (displayName.equals("Discovery")) return WORLD_DISCOVERY;

        return null;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getColorCode() {
        return String.valueOf(colorCode);
    }

    public boolean matchesTracking(ActivityType activityType) {
        // When tracking content, storyline quests and mini-quests cannot
        // be distinguished from quests
        return switch (this) {
            case STORYLINE_QUEST, MINI_QUEST -> activityType == this || activityType == QUEST;
            default -> activityType == this;
        };
    }
}
