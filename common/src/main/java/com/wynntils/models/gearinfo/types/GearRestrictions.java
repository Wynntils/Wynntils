/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.types;

import java.util.Locale;

public enum GearRestrictions {
    NONE,
    UNTRADABLE,
    QUEST_ITEM;

    public static GearRestrictions fromString(String typeStr) {
        try {
            return GearRestrictions.valueOf(typeStr.toUpperCase(Locale.ROOT).replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
