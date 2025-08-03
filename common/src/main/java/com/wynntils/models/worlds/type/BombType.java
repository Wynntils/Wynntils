/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.type;

public enum BombType {
    COMBAT_XP("Combat XP", "Combat Experience", 20),
    DUNGEON("Dungeon", "Dungeon", 10),
    LOOT("Loot", "Loot", 20),
    PROFESSION_SPEED("Profession Speed", "Profession Speed", 10),
    PROFESSION_XP("Profession XP", "Profession Experience", 20);

    private final String displayName;
    private final String parseName;
    private final int activeMinutes;

    BombType(String displayName, String parseName, int activeMinutes) {
        this.displayName = displayName;
        this.parseName = parseName;
        this.activeMinutes = activeMinutes;
    }

    public static BombType fromString(String name) {
        for (BombType type : values()) {
            if (type.parseName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public int getActiveMinutes() {
        return activeMinutes;
    }

    public String getDisplayName() {
        return displayName;
    }
}
