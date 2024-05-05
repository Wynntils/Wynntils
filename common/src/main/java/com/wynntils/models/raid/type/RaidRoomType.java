/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.type;

public enum RaidRoomType {
    INTRO,
    CHALLENGE_1,
    POWERUP_1,
    CHALLENGE_2,
    POWERUP_2,
    CHALLENGE_3,
    POWERUP_3,
    BOSS_INTERMISSION,
    BOSS_FIGHT;

    public static RaidRoomType fromName(String name) {
        for (RaidRoomType roomType : RaidRoomType.values()) {
            if (roomType.name().equalsIgnoreCase(name)) {
                return roomType;
            }
        }

        return null;
    }
}
