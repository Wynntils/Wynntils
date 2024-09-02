/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards;

public enum TomeType {
    GUILD_TOME,
    WEAPON_TOME,
    ARMOUR_TOME,
    MYSTICISM_TOME,
    MARATHON_TOME,
    LOOTRUN_TOME,
    EXPERTISE_TOME;

    public static TomeType fromString(String str) {
        for (TomeType value : TomeType.values()) {
            if (value.name().equalsIgnoreCase(str)) {
                return value;
            }
        }

        return null;
    }
}
