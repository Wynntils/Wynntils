/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.type;

import java.util.Optional;
import net.minecraft.ChatFormatting;

public enum ActivityType {
    RECOMMENDED("Recommended", "recommended", null),
    QUEST("Quest", "quests", ChatFormatting.AQUA),
    STORYLINE_QUEST("Quest", "quests", ChatFormatting.GREEN),
    MINI_QUEST("Mini-Quest", "mini-quests", ChatFormatting.DARK_PURPLE),
    CAVE("Cave", "caves", ChatFormatting.GOLD),
    SECRET_DISCOVERY("Secret Discovery", "secret discoveries", ChatFormatting.AQUA),
    WORLD_DISCOVERY("World Discovery", "world discoveries", ChatFormatting.YELLOW),
    TERRITORIAL_DISCOVERY("Territorial Discovery", "territorial discoveries", ChatFormatting.WHITE),
    DUNGEON("Dungeon", "dungeons", ChatFormatting.RED),
    RAID("Raid", "raids", ChatFormatting.YELLOW),
    BOSS_ALTAR("Boss Altar", "boss altars", ChatFormatting.LIGHT_PURPLE),
    LOOTRUN_CAMP("Lootrun Camp", "lootrun camps", ChatFormatting.BLUE);

    private final String displayName;
    private final String groupName;
    private final Optional<ChatFormatting> color;

    ActivityType(String displayName, String groupName, ChatFormatting color) {
        this.displayName = displayName;
        this.groupName = groupName;
        this.color = Optional.ofNullable(color);
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
        return color.isEmpty() ? "" : String.valueOf(color.get().getChar());
    }

    public boolean isQuest() {
        return this == QUEST || this == STORYLINE_QUEST || this == MINI_QUEST;
    }

    public boolean isDiscovery() {
        return this == SECRET_DISCOVERY || this == WORLD_DISCOVERY || this == TERRITORIAL_DISCOVERY;
    }

    public boolean matchesTracking(ActivityType activityType) {
        // When tracking activities, storyline quests and mini-quests cannot
        // be distinguished from quests
        return switch (this) {
            case STORYLINE_QUEST, MINI_QUEST -> activityType == this || activityType == QUEST;
            default -> activityType == this;
        };
    }
}
