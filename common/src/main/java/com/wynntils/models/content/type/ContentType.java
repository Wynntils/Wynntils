/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.content.type;

public enum ContentType {
    QUEST("Quest", "b"),
    STORYLINE_QUEST("Quest", "a"),
    MINI_QUEST("Mini-Quest", "5"),
    CAVE("Cave", "6"),
    SECRET_DISCOVERY("Secret Discovery", "b"),
    WORLD_DISCOVERY("World Discovery", "e"),
    TERRITORIAL_DISCOVERY("Territorial Discovery", "f"),
    DUNGEON("Dungeon", "c"),
    RAID("Raid", "e"),
    BOSS_ALTAR("Boss Altar", "d");

    private final String displayName;
    private final String colorCode;

    ContentType(String displayName, String colorCode) {
        this.displayName = displayName;
        this.colorCode = colorCode;
    }

    public static ContentType from(String colorCode, String displayName) {
        for (ContentType type : values()) {
            if (type.getColorCode().equals(colorCode) && type.getDisplayName().equals(displayName)) return type;
        }

        return null;
    }

    /** This version cannot distinguish between QUEST and STORYLINE_QUEST */
    public static ContentType from(String displayName) {
        for (ContentType type : values()) {
            if (type.getDisplayName().equals(displayName)) return type;
        }

        return null;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public boolean matchesTracking(ContentType contentType) {
        // When tracking content, storyline quests and mini-quests cannot
        // be distinguished from quests
        return switch (this) {
            case STORYLINE_QUEST, MINI_QUEST -> contentType == this || contentType == QUEST;
            default -> contentType == this;
        };
    }
}
