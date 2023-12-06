/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import java.util.Locale;

public enum GearRestrictions {
    NONE(""),
    UNTRADABLE("Untradable Item"),
    QUEST_ITEM("Quest Item"),
    SOULBOUND("Soulbound");

    private final String description;

    GearRestrictions(String description) {
        this.description = description;
    }

    public static GearRestrictions fromString(String typeStr) {
        for (GearRestrictions type : GearRestrictions.values()) {
            if (type.name().replaceAll("_", " ").toLowerCase(Locale.ROOT).equals(typeStr.toLowerCase(Locale.ROOT))) {
                return type;
            }
        }

        return null;
    }

    public String getDescription() {
        return description;
    }
}
