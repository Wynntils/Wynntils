/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import java.util.Locale;

public enum GearDropRestrictions {
    NORMAL(""),
    NEVER("Never"),
    LOOT_CHEST("Loot Chest"),
    DUNGEON("Dungeon");

    private final String description;

    GearDropRestrictions(String description) {
        this.description = description;
    }

    public static GearDropRestrictions fromString(String typeStr) {
        for (GearDropRestrictions type : GearDropRestrictions.values()) {
            if (type.name().toLowerCase(Locale.ROOT).equals(typeStr.toLowerCase(Locale.ROOT))) {
                return type;
            }
        }

        return null;
    }

    public String getDescription() {
        return description;
    }
}
