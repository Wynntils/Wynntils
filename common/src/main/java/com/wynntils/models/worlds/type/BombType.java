/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.type;

import java.util.List;

public enum BombType {
    COMBAT_XP("Combat XP", "Combat Experience", 20),
    DUNGEON("Dungeon", List.of("Dungeon", "Free Dungeon Entry"), 10),
    LOOT("Loot", "Loot", 20),
    PROFESSION_SPEED("Profession Speed", "Profession Speed", 10),
    PROFESSION_XP("Profession XP", "Profession Experience", 20),
    LOOT_CHEST("Loot Chest", List.of("Loot Chest", "More Chest Loot"), 20);

    private final String displayName;
    private final List<String> parseNames;
    private final int activeMinutes;

    BombType(String displayName, List<String> parseNames, int activeMinutes) {
        this.displayName = displayName;
        this.parseNames = parseNames;
        this.activeMinutes = activeMinutes;
    }

    BombType(String displayName, String parseName, int activeMinutes) {
        this(displayName, List.of(parseName), activeMinutes);
    }

    public static BombType fromString(String name) {
        for (BombType type : values()) {
            for (String parseName : type.parseNames) {
                if (parseName.equalsIgnoreCase(name)) {
                    return type;
                }
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
