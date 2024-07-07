/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.type;

import com.wynntils.utils.colors.CustomColor;
import java.util.Objects;

public enum ActivityType {
    RECOMMENDED("Recommended", "recommended", null),
    QUEST("Quest", "quests", CustomColor.fromHexString("#29cc96")),
    STORYLINE_QUEST("Quest", "quests", CustomColor.fromHexString("#33b33b")),
    MINI_QUEST("Mini-Quest", "mini-quests", CustomColor.fromHexString("#b38fad")),
    WORLD_EVENT("World Event", "world events", CustomColor.fromHexString("#00bdbf")),
    SECRET_DISCOVERY("Secret Discovery", "secret discoveries", CustomColor.fromHexString("#a1c3e6")),
    WORLD_DISCOVERY("World Discovery", "world discoveries", CustomColor.fromHexString("#a1c3e6")),
    TERRITORIAL_DISCOVERY("Territorial Discovery", "territorial discoveries", CustomColor.fromHexString("#a1c3e6")),
    CAVE("Cave", "caves", CustomColor.fromHexString("#ff8c19")),
    DUNGEON("Dungeon", "dungeons", CustomColor.fromHexString("#cc6677")),
    RAID("Raid", "raids", CustomColor.fromHexString("#d6401e")),
    BOSS_ALTAR("Boss Altar", "boss altars", CustomColor.fromHexString("#f2d349")),
    LOOTRUN_CAMP("Lootrun Camp", "lootrun camps", CustomColor.fromHexString("#3399cc"));

    private final String displayName;
    private final String groupName;
    private final CustomColor color;

    ActivityType(String displayName, String groupName, CustomColor color) {
        this.displayName = displayName;
        this.groupName = groupName;
        this.color = color;
    }

    public static ActivityType from(CustomColor color, String displayName) {
        for (ActivityType type : values()) {
            if (Objects.equals(type.getColor(), color) && type.getDisplayName().equals(displayName)) {
                return type;
            }
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

    public CustomColor getColor() {
        return color;
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
