/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.type;

import java.util.Locale;

public enum GearDropType {
    NEVER, // quests or merchants
    LOOTCHEST, // lootchests (implies t3 or t4, afaict)
    DUNGEON, // drop on dungeon completion or forgery chest
    NORMAL; // mobs

    public static GearDropType fromString(String typeStr) {
        try {
            return GearDropType.valueOf(typeStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
