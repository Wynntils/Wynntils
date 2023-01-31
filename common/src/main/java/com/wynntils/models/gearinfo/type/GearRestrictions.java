/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.type;

import java.util.Locale;

public enum GearRestrictions {
    NONE(""),
    UNTRADABLE("Untradable Item"),
    QUEST_ITEM("Quest Item");

    private final String description;

    GearRestrictions(String description) {
        this.description = description;
    }

    public static GearRestrictions fromString(String typeStr) {
        try {
            return GearRestrictions.valueOf(typeStr.toUpperCase(Locale.ROOT).replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getDescription() {
        return description;
    }
}
