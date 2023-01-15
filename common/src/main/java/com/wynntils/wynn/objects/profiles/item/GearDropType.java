/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.profiles.item;

import java.util.Locale;

public enum GearDropType {
    NEVER, // quests or merchants
    LOOTCHEST, // lootchests
    NORMAL; // mobs

    public static GearDropType fromString(String typeStr) {
        try {
            return GearDropType.valueOf(typeStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
