/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import java.util.Locale;

public enum GearRestrictions {
    NONE("", ""),
    UNTRADABLE("Untradable", "This item cannot be traded with other players"),
    QUEST_ITEM("Quest Item", "This item is used for a quest and cannot be dropped or traded");

    private final String name;
    private final String description;

    GearRestrictions(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static GearRestrictions fromString(String typeStr) {
        for (GearRestrictions type : GearRestrictions.values()) {
            if (type.name().toLowerCase(Locale.ROOT).equals(typeStr.toLowerCase(Locale.ROOT))) {
                return type;
            }
        }

        return null;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
