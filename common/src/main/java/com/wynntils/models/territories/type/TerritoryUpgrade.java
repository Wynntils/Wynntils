/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.territories.type;

import com.wynntils.utils.StringUtils;
import java.util.Arrays;
import java.util.Locale;

public enum TerritoryUpgrade {
    // Guild Tower
    DAMAGE,
    ATTACK,
    HEALTH,
    DEFENCE,

    // Bonuses
    STRONGER_MINIONS,
    TOWER_MULTI_ATTACKS("Tower Multi-Attacks"),
    TOWER_AURA,
    TOWER_VOLLEY,
    GATHERING_EXPERIENCE,
    MOB_EXPERIENCE,
    MOB_DAMAGE,
    PVP_DAMAGE,
    XP_SEEKING,
    TOME_SEEKING,
    EMERALD_SEEKING,
    LARGER_RESOURCE_STORAGE,
    LARGER_EMERALD_STORAGE,
    EFFICIENT_RESOURCES,
    EFFICIENT_EMERALDS,
    RESOURCE_RATE,
    EMERALD_RATE;

    private final String name;

    TerritoryUpgrade() {
        this.name = Arrays.stream(this.name().split("_"))
                .map(s -> StringUtils.capitalizeFirst(s.toLowerCase(Locale.ROOT)))
                .reduce((s1, s2) -> s1 + " " + s2)
                .orElse("");
    }

    TerritoryUpgrade(String name) {
        this.name = name;
    }

    public static TerritoryUpgrade fromName(String name) {
        for (TerritoryUpgrade upgrade : values()) {
            if (upgrade.getName().equalsIgnoreCase(name)) {
                return upgrade;
            }
        }

        return null;
    }

    public String getName() {
        return name;
    }
}
