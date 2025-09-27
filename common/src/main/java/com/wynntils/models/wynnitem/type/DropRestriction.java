/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.type;

public enum DropRestriction {
    NEVER("never"),
    NORMAL("normal"), // Hostile mobs and loot chests in item level +/- 4  (if level 100 then 96-104)
    LOOT_CHEST("lootchest"), // Tier 3/4 loot chests in item level +/- 4
    DUNGEON("dungeon");

    private final String apiName;

    DropRestriction(String apiName) {
        this.apiName = apiName;
    }

    public static DropRestriction fromApiName(String apiName) {
        for (DropRestriction source : DropRestriction.values()) {
            if (source.apiName.equals(apiName)) return source;
        }

        return null;
    }
}
