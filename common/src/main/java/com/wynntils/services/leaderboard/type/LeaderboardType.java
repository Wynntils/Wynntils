/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.leaderboard.type;

public enum LeaderboardType {
    // Gathering Professions
    WOODCUTTING("woodcuttingLevel"),
    MINING("miningLevel"),
    FISHING("fishingLevel"),
    FARMING("farmingLevel"),
    // Crafting Professions
    ALCHEMISM("alchemismLevel"),
    ARMOURING("armouringLevel"),
    COOKING("cookingLevel"),
    JEWELING("jewelingLevel"),
    SCRIBING("scribingLevel"),
    TAILORING("tailoringLevel"),
    WEAPONSMITHING("weaponsmithingLevel"),
    WOODWORKING("woodworkingLevel"),
    // Total Professions (currently no difference between their badge texture)
    PROFESSIONS_GLOBAL("professionsGlobalLevel"),
    PROFESSIONS_SOLO("professionsSoloLevel"),
    // Total Combat (currently no difference between their badge texture)
    COMBAT_GLOBAL("combatGlobalLevel"),
    COMBAT_SOLO("combatSoloLevel"),
    // Total Overall (currently no difference between their badge texture)
    TOTAL_GLOBAL("totalGlobalLevel"),
    TOTAL_SOLO("totalSoloLevel"),
    // Content Completion (currently no difference between their badge texture)
    CONTENT_SOLO("playerContent"),
    CONTENT_GLOBAL("globalPlayerContent"),
    // Raids
    NEST_OF_THE_GROOTSLANGS("nogCompletion"),
    ORPHIONS_NEXUS_OF_LIGHT("nolCompletion"),
    THE_CANYON_COLOSSUS("tccCompletion"),
    THE_NAMELESS_ANOMALY("tnaCompletion"),
    // Solo Gamemodes
    IRONMAN("ironmanContent"),
    ULTIMATE_IRONMAN("ultimateIronmanContent"),
    HARDCORE("hardcoreContent"),
    CRAFTSMAN("craftsmanContent"),
    HUNTED("huntedContent"),
    // Multi Gamemodes
    HUIC("huicContent"),
    HUICH("huichContent"),
    HICH("hichContent"),
    HIC("hicContent"),
    // Misc
    WARS("warsCompletion");

    private final String key;

    LeaderboardType(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static LeaderboardType fromKey(String key) {
        for (LeaderboardType type : LeaderboardType.values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }

        return null;
    }
}
