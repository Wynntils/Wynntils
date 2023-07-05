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

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }
}
