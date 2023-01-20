/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.type;

public enum BombType {
    CombatXp("Combat XP", 20),
    Dungeon("Dungeon", 10),
    Loot("Loot", 20),
    ProfessionSpeed("Profession Speed", 10),
    ProfessionXp("Profession XP", 20);

    private final String parseName;
    private final int activeMinutes;

    BombType(String parseName, int activeMinutes) {
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

    public String getName() {
        return parseName;
    }
}
