/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards;

public enum TomeType {
    DUNGEON_XP,
    GATHERING_XP,
    GUILD_TOME,
    LOOTRUN,
    MOB_DAMAGE,
    MOB_DEFENCE,
    SLAYING_XP;

    public static TomeType fromString(String str) {
        for (TomeType value : TomeType.values()) {
            if (value.name().replaceAll("_", "").equalsIgnoreCase(str)) {
                return value;
            }
        }

        return null;
    }
}
